package com.mb.mapper;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.IOException;
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
    private final String ARROW = "->";

    public List<Map<String, Mapper>> parse(String mapperFileLocation) throws IOException {
        List<File> parseTarget = new ArrayList<>();

        String fileLocation = mapperFileLocation.trim();

        fileLocation.lastIndexOf(File.separator);
        File classpath = new ClassPathResource(".cpindicator").getFile().getParentFile();
        if (fileLocation.startsWith("classpath:")) {
            fileLocation = fileLocation.replace("classpath:", classpath.getPath() + File.separator);
        } else if (fileLocation.lastIndexOf(File.separator) == -1) {
            fileLocation = classpath.getPath() + File.separator + fileLocation;
        }

        if (fileLocation.contains("*")) {
            String prefix = fileLocation.substring(fileLocation.lastIndexOf(File.separator) + 1, fileLocation.indexOf("*"));
            String suffix = fileLocation.substring(fileLocation.indexOf("*") + 1);
            File parentDir = new File(fileLocation).getParentFile();

            if(!parentDir.exists() || !parentDir.isDirectory()) {
                throw new FileNotFoundException("mapper file location directory not found: " + parentDir);
            }

            class WildCardFileNameFilter implements FilenameFilter {
                final String prefix;
                final String suffix;
                WildCardFileNameFilter(String prefix, String suffix) {
                    this.prefix = prefix;
                    this.suffix = suffix;
                }

                @Override
                public boolean accept(File dir, String name) {
                    if (name.startsWith(prefix) && name.endsWith(suffix)) {
                        return true;
                    }
                    return false;
                }
            }

            File[] mapperFiles = parentDir.listFiles(new WildCardFileNameFilter(prefix, suffix));
            if (mapperFiles != null || mapperFiles.length > 0) {
                parseTarget = Arrays.asList(mapperFiles);
            } else {
                log.info("No mapper files found in " + parentDir);
                return null;
            }
        } else {
            File mapperFile = null;
            mapperFile = new File(fileLocation);

            if (!mapperFile.exists()) {
                log.info("No mapper file found in " + fileLocation);
                return null;
            }
            if (!mapperFile.isFile()) {
                throw new FileNotFoundException(mapperFile + " is a directory");
            }
            parseTarget.add(mapperFile);
        }


        return parseInternal(parseTarget);
    }

    private List<Map<String, Mapper>> parseInternal(List<File> mapperFiles) {
        List<Map<String, Mapper>> mappers = new ArrayList<>();
        for (File file : mapperFiles) {
            log.info("Parsing file: {}", file);
        }
        return mappers;
    }


}
