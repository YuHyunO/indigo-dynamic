package mb.dnm.core.dynamic;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class KeywordValidator {

    private final static Map<String, String> AVAILABLE_CLASSES = new HashMap<>();
    private final static Map<String, String> AVAILABLE_PACKAGES = new HashMap<>();
    private final static List<String> PROHIBITED_KEYWORD = new ArrayList<>();

    static {
        PROHIBITED_KEYWORD.add("Tread");
        PROHIBITED_KEYWORD.add("ThreadGroup");
        PROHIBITED_KEYWORD.add("ThreadLocal");
        PROHIBITED_KEYWORD.add("Runnable");
        PROHIBITED_KEYWORD.add("Compiler");
        PROHIBITED_KEYWORD.add("ClassLoader");
        PROHIBITED_KEYWORD.add("InheritableThreadLocal");
        PROHIBITED_KEYWORD.add("Process");
        PROHIBITED_KEYWORD.add("ProcessBuilder");
        PROHIBITED_KEYWORD.add("ProcessHandle");
        PROHIBITED_KEYWORD.add("Runtime");
        PROHIBITED_KEYWORD.add("RuntimePermission");
        PROHIBITED_KEYWORD.add("System");
        PROHIBITED_KEYWORD.add("SecurityManager");
        PROHIBITED_KEYWORD.add("SecurityManager");
        PROHIBITED_KEYWORD.add("Readable");
        PROHIBITED_KEYWORD.add("AutoCloseable");

        AVAILABLE_CLASSES.put("Collection", "java.util.Collection");
        AVAILABLE_CLASSES.put("List", "java.util.List");
        AVAILABLE_CLASSES.put("Map", "java.util.Map");
        AVAILABLE_CLASSES.put("Date", "java.util.Date");
        AVAILABLE_CLASSES.put("Time", "java.util.Time");
        AVAILABLE_CLASSES.put("Timestamp", "java.util.Timestamp");
        AVAILABLE_CLASSES.put("ArrayList", "java.util.ArrayList");
        AVAILABLE_CLASSES.put("HashMap", "java.util.HashMap");
        AVAILABLE_CLASSES.put("LinkedHashMap", "java.util.LinkedHashMap");
        AVAILABLE_CLASSES.put("TreeMap", "java.util.TreeMap");
        AVAILABLE_CLASSES.put("TreeSet", "java.util.TreeSet");
        AVAILABLE_CLASSES.put("LinkedHashSet", "java.util.LinkedHashSet");
        AVAILABLE_CLASSES.put("LinkedList", "java.util.LinkedList");
        AVAILABLE_CLASSES.put("Vector", "java.util.Vector");
        AVAILABLE_CLASSES.put("Set", "java.util.Set");
        AVAILABLE_CLASSES.put("HashSet", "java.util.HashSet");
        AVAILABLE_CLASSES.put("HashTable", "java.util.HashTable");
        AVAILABLE_CLASSES.put("Arrays", "java.util.Arrays");
        AVAILABLE_CLASSES.put("Locale", "java.util.Locale");
        AVAILABLE_CLASSES.put("Scanner", "java.util.Scanner");
        AVAILABLE_CLASSES.put("SimpleDateFormat", "java.util.SimpleDateFormat");
        AVAILABLE_CLASSES.put("Calendar", "java.util.Calendar");
        AVAILABLE_PACKAGES.put("Properties", "java.util.Properties");
        AVAILABLE_PACKAGES.put("UUID", "java.util.UUID");

        AVAILABLE_PACKAGES.put("MessageUtil", " mb.dnm.util");

        AVAILABLE_CLASSES.put("File", "java.io.File");
        AVAILABLE_CLASSES.put("InputStream", "java.io.InputStream");
        AVAILABLE_CLASSES.put("OutputStream", "java.io.OutputStream");
        AVAILABLE_CLASSES.put("FileInputStream", "java.io.FileInputStream");
        AVAILABLE_CLASSES.put("FileOutputStream", "java.io.FileOutputStream");
        AVAILABLE_CLASSES.put("FileReader", "java.io.FileReader");
        AVAILABLE_CLASSES.put("FileWriter", "java.io.FileWriter");
        AVAILABLE_CLASSES.put("BufferedInputStream", "java.io.BufferedInputStream");
        AVAILABLE_CLASSES.put("BufferedOutputStream", "java.io.BufferedOutputStream");
        AVAILABLE_CLASSES.put("BufferedWriter", "java.io.BufferedWriter");
        AVAILABLE_CLASSES.put("BufferedReader", "java.io.BufferedReader");
        AVAILABLE_CLASSES.put("ByteArrayInputStream", "java.io.ByteArrayInputStream");
        AVAILABLE_CLASSES.put("ByteArrayOutputStream", "java.io.ByteArrayOutputStream");
        AVAILABLE_CLASSES.put("IOException", "java.io.IOException");
        AVAILABLE_CLASSES.put("FileNotFoundException", "java.io.FileNotFoundException");

        AVAILABLE_CLASSES.put("Files", "java.nio.file.Files");
        AVAILABLE_CLASSES.put("Path", "java.nio.file.Path");
        AVAILABLE_CLASSES.put("Paths", "java.nio.file.Paths");
        AVAILABLE_CLASSES.put("FileStore", "java.nio.file.FileStore");
        AVAILABLE_CLASSES.put("FileSystem", "java.nio.file.FileSystem");
        AVAILABLE_CLASSES.put("PathMatcher", "java.nio.file.PathMatcher");
        AVAILABLE_CLASSES.put("FileStoreAttributeView", "java.nio.file.attribute.FileStoreAttributeView");
        AVAILABLE_CLASSES.put("FileTime", "java.nio.file.attribute.FileTime");
        AVAILABLE_CLASSES.put("UserPrincipal", "java.nio.file.attribute.UserPrincipal");
        AVAILABLE_CLASSES.put("UserPrincipalLookupService", "java.nio.file.attribute.UserPrincipalLookupService");
        AVAILABLE_CLASSES.put("GroupPrincipal", "java.nio.file.attribute.GroupPrincipal");
        AVAILABLE_CLASSES.put("PosixFilePermission", "java.nio.file.attribute.PosixFilePermission");
        AVAILABLE_CLASSES.put("BasicFileAttributes", "java.nio.file.attribute.BasicFileAttributes");
        AVAILABLE_CLASSES.put("StandardCopyOption", "java.nio.file.StandardCopyOption");
        AVAILABLE_CLASSES.put("StandardOpenOption", "java.nio.file.StandardOpenOption");
        AVAILABLE_CLASSES.put("DirectoryStream", "java.nio.file.DirectoryStream");
        AVAILABLE_CLASSES.put("SeekableByteChannel", "java.nio.channels.SeekableByteChannel");

    }

    private KeywordValidator() {}

    public static void assertProhibited(String dynamicCode) {
        for (String keyword : PROHIBITED_KEYWORD) {
            if (dynamicCode.contains(keyword)) {
                throw new IllegalStateException("Do not use the keyword \"" + keyword + "\" in dynamicCode.");
            }
        }
    }

}
