package com.mb.mapper;

import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
        mapperFileLocation = mapperFileLocation.trim();

        if (mapperFileLocation.contains("*")) {
            String prefix = mapperFileLocation.substring(mapperFileLocation.lastIndexOf(File.separator) + 1, mapperFileLocation.indexOf("*"));
            String suffix = mapperFileLocation.substring(mapperFileLocation.indexOf("*") + 1);
            File parentDir = new File(mapperFileLocation.substring(0, mapperFileLocation.lastIndexOf(File.separator)));

            if(!parentDir.exists()) {
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
            for (File mapperFile : mapperFiles) {
                log.info("parsing mapper file: " + mapperFile);
            }
        } else {
            File mapperFile = new File(mapperFileLocation);
            if (!mapperFile.exists()) {
                throw new FileNotFoundException("mapper file location not found: " + mapperFile);
            }
            if (!mapperFile.isFile()) {
                throw new FileNotFoundException(mapperFile + "is a directory");
            }
            parseTarget.add(mapperFile);
        }


        return parseInternal(parseTarget);
    }

    private List<Map<String, Mapper>> parseInternal(List<File> mapperFiles) {
        List<Map<String, Mapper>> mappers = new ArrayList<>();

        return mappers;
    }

}
