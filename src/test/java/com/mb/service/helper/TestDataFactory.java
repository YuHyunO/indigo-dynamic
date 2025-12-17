package com.mb.service.helper;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;

/**
 * 테스트용 데이터를 생성하는 팩토리 클래스입니다.
 */
public class TestDataFactory {

    private static final String TEMP_DIR = "src/test/resources/temp";

    /**
     * 테스트용 Map 데이터를 생성합니다.
     */
    public static Map<String, Object> createTestMap(String... keyValuePairs) {
        Map<String, Object> map = new HashMap<>();
        for (int i = 0; i < keyValuePairs.length; i += 2) {
            if (i + 1 < keyValuePairs.length) {
                map.put(keyValuePairs[i], keyValuePairs[i + 1]);
            }
        }
        return map;
    }

    /**
     * 테스트용 List<Map<String, Object>> 데이터를 생성합니다.
     */
    public static List<Map<String, Object>> createTestListMap(Map<String, Object>... maps) {
        List<Map<String, Object>> list = new ArrayList<>();
        Collections.addAll(list, maps);
        return list;
    }

    /**
     * 테스트용 List<Map<String, Object>> 데이터를 생성합니다.
     */
    public static List<Map<String, Object>> createTestListMap(int count) {
        List<Map<String, Object>> list = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            Map<String, Object> map = new HashMap<>();
            map.put("id", i);
            map.put("name", "Test" + i);
            map.put("value", "Value" + i);
            list.add(map);
        }
        return list;
    }

    /**
     * 테스트용 임시 디렉토리를 생성합니다.
     */
    public static Path createTempDirectory(String dirName) throws IOException {
        Path tempDir = Paths.get(TEMP_DIR, dirName);
        Files.createDirectories(tempDir);
        return tempDir;
    }

    /**
     * 테스트용 임시 파일을 생성합니다.
     */
    public static Path createTempFile(String fileName, String content) throws IOException {
        ensureTempDirExists();
        Path filePath = Paths.get(TEMP_DIR, fileName);
        Files.write(filePath, content.getBytes());
        return filePath;
    }

    /**
     * 테스트용 임시 파일을 생성합니다 (바이너리).
     */
    public static Path createTempFile(String fileName, byte[] content) throws IOException {
        ensureTempDirExists();
        Path filePath = Paths.get(TEMP_DIR, fileName);
        Files.write(filePath, content);
        return filePath;
    }

    /**
     * 테스트용 임시 파일을 생성합니다 (지정된 디렉토리).
     */
    public static Path createTempFile(Path directory, String fileName, String content) throws IOException {
        Files.createDirectories(directory);
        Path filePath = directory.resolve(fileName);
        Files.write(filePath, content.getBytes());
        return filePath;
    }

    /**
     * 임시 디렉토리가 존재하는지 확인하고 없으면 생성합니다.
     */
    private static void ensureTempDirExists() throws IOException {
        Path tempDir = Paths.get(TEMP_DIR);
        if (!Files.exists(tempDir)) {
            Files.createDirectories(tempDir);
        }
    }

    /**
     * 테스트용 임시 파일/디렉토리를 삭제합니다.
     */
    public static void cleanupTempFile(Path path) throws IOException {
        if (path != null && Files.exists(path)) {
            if (Files.isDirectory(path)) {
                deleteDirectory(path);
            } else {
                Files.delete(path);
            }
        }
    }

    /**
     * 디렉토리를 재귀적으로 삭제합니다.
     */
    private static void deleteDirectory(Path directory) throws IOException {
        if (Files.exists(directory)) {
            Files.walkFileTree(directory, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    Files.delete(file);
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                    if (exc == null) {
                        Files.delete(dir);
                        return FileVisitResult.CONTINUE;
                    }
                    throw exc;
                }
            });
        }
    }

    /**
     * 테스트용 문자열 데이터를 생성합니다.
     */
    public static String createTestString(int length) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i++) {
            sb.append((char) ('A' + (i % 26)));
        }
        return sb.toString();
    }

    /**
     * 테스트용 바이너리 데이터를 생성합니다.
     */
    public static byte[] createTestBytes(int length) {
        byte[] bytes = new byte[length];
        for (int i = 0; i < length; i++) {
            bytes[i] = (byte) (i % 256);
        }
        return bytes;
    }

    /**
     * 테스트용 CSV 형식 문자열을 생성합니다.
     */
    public static String createCsvContent(String delimiter, String... rows) {
        StringBuilder sb = new StringBuilder();
        for (String row : rows) {
            sb.append(row).append("\n");
        }
        return sb.toString();
    }

    /**
     * 테스트용 파이프 구분 형식 문자열을 생성합니다.
     */
    public static String createPipeDelimitedContent(String... rows) {
        return createCsvContent("|", rows);
    }
}


