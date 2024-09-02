package com.mb.mapper;

import com.mb.util.CastUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

@Slf4j
public class StatementParser {
    private final String KEY_MAPPER_INDICATOR = "@key_mapper";
    private final String VALUE_MAPPER_INDICATOR = "@value_mapper";
    private final String ID_PROP = "id";
    private final String CREATE_NULL_KEY_PROP = "create_null_key";
    private final String NULL_TO_EMPTY_STRING_PROP = "null_to_empty_string";
    private final String LINE_START_INDICATOR = "#";
    private final String LINE_END_INDICATOR = ";";
    private final String START_STATEMENT = "{";
    private final String END_STATEMENT = "}";
    private final String ARROW = "->";
    private final String DESCRIPTION = "//";
    private String charset = "UTF-8";

    public List<Map<String, Mapper>> parse(String mapperFileLocation) throws IOException {
        List<File> parseTargets = new ArrayList<>();

        String fileLocation = mapperFileLocation.trim();

        Resource[] resources = new PathMatchingResourcePatternResolver().getResources(fileLocation);
        for (Resource resource : resources) {
            File file = resource.getFile();
            if (!file.isDirectory()) {
                parseTargets.add(file);
            }
        }
        if (parseTargets.isEmpty()) {
            log.debug("No mapper files found in {}", mapperFileLocation);
            return null;
        }

        return parseInternal(parseTargets);
    }

    private List<Map<String, Mapper>> parseInternal(List<File> mapperFiles) throws IOException {
        List<Map<String, Mapper>> mappers = new ArrayList<>();
        for (File file : mapperFiles) {
            log.info("Parsing file: {}", file);
            List<Map<String, Mapper>> mapper = parseContent(file.toPath(), Charset.forName(charset));

        }
        return mappers;
    }

    private List<Map<String, Mapper>> parseContent(Path path, Charset charset) throws IOException {
        List<Map<String, Mapper>> mappers = new ArrayList<>();
        try (BufferedReader reader = Files.newBufferedReader(path, charset)) {
            String line = null;
            KeyMapper keyMapper = null;
            ValueMapper valueMapper = null;
            while ((line = reader.readLine()) != null) {
                Map<String, Mapper> map = new HashMap<>();
                line = line.trim();
                if (line.equals("") || line.startsWith(DESCRIPTION))
                    continue;

                if (line.startsWith(KEY_MAPPER_INDICATOR)) {
                    keyMapper = new KeyMapper();
                    setProperties(line.substring(KEY_MAPPER_INDICATOR.length()).replace(" ", ""), keyMapper);
                    readAndSetMap(reader, line.substring(line.lastIndexOf(")")), keyMapper);
                    map.put(keyMapper.getId(), keyMapper);
                    mappers.add(map);

                } else if (line.startsWith(VALUE_MAPPER_INDICATOR)) {
                    valueMapper = new ValueMapper();
                    setProperties(line.substring(VALUE_MAPPER_INDICATOR.length()).replace(" ", ""), valueMapper);
                    readAndSetMap(reader, line.substring(line.lastIndexOf(")")), valueMapper);
                    map.put(valueMapper.getId(), valueMapper);
                    mappers.add(map);

                } else {
                    throw new MapperParsingException("Invalid mapper line. Couldn't parse: " + line);
                }
            }
        }
        return mappers;
    }

    /**
     * Set Mapper's properties
     * */
    private void setProperties(String line, Mapper mapper) throws MapperParsingException {
        String propLine = line.replace(" ", "");
        char startChar = propLine.toCharArray()[0];
        if (startChar != '(')
            throw new MapperParsingException("Invalid character at property line: " + startChar);

        int endIdx = propLine.lastIndexOf(")");
        if (endIdx == -1)
            throw new MapperParsingException("The end property line character ')' is not exist");

        if (endIdx == 1)
            throw new MapperParsingException("The property is empty: " + propLine);

        if (propLine.indexOf("(") != propLine.lastIndexOf("("))
            throw new MapperParsingException("Invalid property line. Duplicated parentheses '('.: " + propLine);

        if (propLine.indexOf(")") != endIdx)
            throw new MapperParsingException("Invalid property line. Duplicated parentheses ')'.: " + propLine);

        propLine = propLine.substring(1, endIdx);
        String[] unparsedProp = propLine.split(",");
        Properties props = new Properties();

        for (String unparsed : unparsedProp) {
            String[] keyValue = unparsed.split("=", 2);
            if (keyValue.length == 1)
                throw new MapperParsingException("Invalid property set: the value of the key '" + keyValue[0] + "' is not specified");
            props.put(keyValue[0], keyValue[1]);
        }

        Enumeration propNames = props.propertyNames();
        while (propNames.hasMoreElements()) {
            String key = String.valueOf(propNames.nextElement());
            String value = props.getProperty(key);
            String setter = "set" + key.substring(0, 1).toUpperCase() + key.substring(1);
            try {

                Method[] methods = mapper.getClass().getMethods();
                boolean methodFound = false;
                for (Method method : methods) {
                    if (method.getName().equals(setter)) {
                        Class paramType = method.getParameterTypes()[0];
                        method.invoke(mapper, CastUtil.simpleTypeCast(paramType, value));
                        methodFound = true;
                        break;
                    }
                }
                if (!methodFound) {
                    throw new NoSuchMethodException("The setter method of the property '" + key + "' does not exist: " + setter);
                }
            } catch (NoSuchMethodException ne) {
                log.error("", ne);
                throw new MapperParsingException("No property '" + key + "' exists for Mapper type:" + mapper.getClass().getName());
            } catch (ReflectiveOperationException iac) {
                log.error("", iac);
                throw new MapperParsingException("Couldn't set the property '" + key + "' for Mapper type:" + mapper.getClass().getName());
            }
        }
    }

    private void readAndSetMap(BufferedReader reader, String beforeLine, Mapper mapper) throws IOException {
        boolean startMapParsing = false;
        boolean endMapParsing = false;
        beforeLine = beforeLine.trim();

        if (beforeLine.startsWith(START_STATEMENT)) {
            startMapParsing = true;
        } else if (beforeLine.startsWith(DESCRIPTION)) {
        } else if (beforeLine.equals("")) {
        } else {
            throw new MapperParsingException("Invalid map line. Couldn't parse: " + beforeLine);
        }
        String line = null;
        if (!startMapParsing) {
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.startsWith(START_STATEMENT)) {
                    startMapParsing = true;
                    break;
                }
            }
            if (!startMapParsing)
                throw new MapperParsingException("Invalid mapper. Mapper statement start indicator '{' does not exist");
        }

        


    }


    public void setCharset(String charset) {
        this.charset = charset;
    }

}
