package deprecated;

import mb.dnm.util.CastUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

@Deprecated
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
    private int lineNum = 0;

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
            lineNum = 0;
            List<Map<String, Mapper>> mapper = parseContent(file.toPath(), Charset.forName(charset));
            mapper.addAll(mapper);
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
                ++lineNum;
                Map<String, Mapper> map = new HashMap<>();
                line = line.trim();
                if (line.equals(""))
                    continue;

                int descIdx = line.indexOf(DESCRIPTION);
                if (descIdx != -1)
                    line = line.substring(0, descIdx).trim();

                if (line.startsWith(KEY_MAPPER_INDICATOR)) {
                    keyMapper = new KeyMapper();
                    setProperties(line.substring(KEY_MAPPER_INDICATOR.length()).replace(" ", ""), keyMapper);
                    readAndSetMap(reader, keyMapper);
                    map.put(keyMapper.getId(), keyMapper);
                    mappers.add(map);

                } else if (line.startsWith(VALUE_MAPPER_INDICATOR)) {
                    valueMapper = new ValueMapper();
                    setProperties(line.substring(VALUE_MAPPER_INDICATOR.length()).replace(" ", ""), valueMapper);
                    readAndSetMap(reader, valueMapper);
                    map.put(valueMapper.getId(), valueMapper);
                    mappers.add(map);

                } else {
                    throw new MapperParsingException("Line(" + lineNum + "). Invalid mapper line. Couldn't parse: " + line);
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
            throw new MapperParsingException("Line(" + lineNum + "). Invalid character at property line: " + startChar);

        int endIdx = propLine.lastIndexOf(")");
        if (endIdx == -1)
            throw new MapperParsingException("Line(" + lineNum + "). The end property line character ')' is not exist");

        if (endIdx == 1)
            throw new MapperParsingException("Line(" + lineNum + "). The property is empty: " + propLine);

        if (propLine.indexOf("(") != propLine.lastIndexOf("("))
            throw new MapperParsingException("Line(" + lineNum + "). Invalid property line. Duplicated parentheses '('.: " + propLine);

        if (propLine.indexOf(")") != endIdx)
            throw new MapperParsingException("Line(" + lineNum + "). Invalid property line. Duplicated parentheses ')'.: " + propLine);

        String endOfLine = line.substring(endIdx + 1).trim();
        if (!endOfLine.startsWith(START_STATEMENT))
            throw new MapperParsingException("Line(" + lineNum + "). The property line must be ended with '" + START_STATEMENT + "'");

        /*String descriptionChk = endOfLine.substring(1).trim();
        if (!(descriptionChk.startsWith(DESCRIPTION) || descriptionChk.length() == 0))
            throw new MapperParsingException("Invalid end of the property line: " + descriptionChk);*/

        propLine = propLine.substring(1, endIdx);
        String[] unparsedProp = propLine.split(",");
        Properties props = new Properties();

        for (String unparsed : unparsedProp) {
            String[] keyValue = unparsed.split("=", 2);
            if (keyValue.length == 1)
                throw new MapperParsingException("Line(" + lineNum + "). Invalid property set: the value of the key '" + keyValue[0] + "' is not specified");
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
                    throw new NoSuchMethodException("Line(" + lineNum + "). The setter method of the property '" + key + "' does not exist: " + setter);
                }
            } catch (NoSuchMethodException ne) {
                log.error("", ne);
                throw new MapperParsingException("Line(" + lineNum + "). No property '" + key + "' exists for Mapper type:" + mapper.getClass().getName());
            } catch (ReflectiveOperationException iac) {
                log.error("", iac);
                throw new MapperParsingException("Line(" + lineNum + "). Couldn't set the property '" + key + "' for Mapper type:" + mapper.getClass().getName());
            }
        }
    }

    private void readAndSetMap(BufferedReader reader, Mapper mapper) throws IOException {

        String line = null;
        while((line = reader.readLine()) != null) {
            ++lineNum;
            line = line.trim();
            int descIdx = line.indexOf(DESCRIPTION);
            if (descIdx != -1) {
                line = line.substring(0, descIdx).trim();
            }

            if (line.isEmpty()) {
                continue;
            } else if (line.startsWith(LINE_START_INDICATOR)) {
                if (!line.endsWith(LINE_END_INDICATOR))
                    throw new MapperParsingException("Line(" + lineNum + "). A map statement's line must be ended with semicolon ';'");
                line = line.substring(1, line.length() - 1).trim();

                String[] keyValue = line.split(ARROW, 2);
                if (keyValue.length != 2)
                    throw new MapperParsingException("Line(" + lineNum + "). Mapping key and value is not a pair: " + line);




            } else {
                throw new MapperParsingException("Line(" + lineNum + "). Invalid map statement line: " + line);
            }
        }

    }


    public void setCharset(String charset) {
        this.charset = charset;
    }

}
