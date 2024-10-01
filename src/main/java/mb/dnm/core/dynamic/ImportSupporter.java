package mb.dnm.core.dynamic;

import java.util.*;

public class ImportSupporter {

    private final static Map<String, String> AUTO_IMPORT_CLASSES = new HashMap<>();
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
        PROHIBITED_KEYWORD.add("Diagnostic");
        PROHIBITED_KEYWORD.add("DiagnosticListener");
        PROHIBITED_KEYWORD.add("FileObject");
        PROHIBITED_KEYWORD.add("JavaCompiler");
        PROHIBITED_KEYWORD.add("JavaFileManager");
        PROHIBITED_KEYWORD.add("StandardJavaFileManager");
        PROHIBITED_KEYWORD.add("ToolProvider");
        PROHIBITED_KEYWORD.add("exit");
        PROHIBITED_KEYWORD.add("run");
        PROHIBITED_KEYWORD.add("println");
        PROHIBITED_KEYWORD.add("printf");
        PROHIBITED_KEYWORD.add("print");
        PROHIBITED_KEYWORD.add("ServiceProcessor");

        AUTO_IMPORT_CLASSES.put("Collection", "java.util.Collection");
        AUTO_IMPORT_CLASSES.put("List", "java.util.List");
        AUTO_IMPORT_CLASSES.put("Map", "java.util.Map");
        AUTO_IMPORT_CLASSES.put("Date", "java.util.Date");
        AUTO_IMPORT_CLASSES.put("Time", "java.util.Time");
        AUTO_IMPORT_CLASSES.put("Timestamp", "java.util.Timestamp");
        AUTO_IMPORT_CLASSES.put("ArrayList", "java.util.ArrayList");
        AUTO_IMPORT_CLASSES.put("HashMap", "java.util.HashMap");
        AUTO_IMPORT_CLASSES.put("LinkedHashMap", "java.util.LinkedHashMap");
        AUTO_IMPORT_CLASSES.put("TreeMap", "java.util.TreeMap");
        AUTO_IMPORT_CLASSES.put("TreeSet", "java.util.TreeSet");
        AUTO_IMPORT_CLASSES.put("LinkedHashSet", "java.util.LinkedHashSet");
        AUTO_IMPORT_CLASSES.put("LinkedList", "java.util.LinkedList");
        AUTO_IMPORT_CLASSES.put("Vector", "java.util.Vector");
        AUTO_IMPORT_CLASSES.put("Set", "java.util.Set");
        AUTO_IMPORT_CLASSES.put("HashSet", "java.util.HashSet");
        AUTO_IMPORT_CLASSES.put("HashTable", "java.util.HashTable");
        AUTO_IMPORT_CLASSES.put("Arrays", "java.util.Arrays");
        AUTO_IMPORT_CLASSES.put("Locale", "java.util.Locale");
        AUTO_IMPORT_CLASSES.put("Scanner", "java.util.Scanner");
        AUTO_IMPORT_CLASSES.put("SimpleDateFormat", "java.util.SimpleDateFormat");
        AUTO_IMPORT_CLASSES.put("Calendar", "java.util.Calendar");
        AUTO_IMPORT_CLASSES.put("Properties", "java.util.Properties");
        AUTO_IMPORT_CLASSES.put("UUID", "java.util.UUID");

        AUTO_IMPORT_CLASSES.put("MessageUtil", " mb.dnm.util.MessageUtil");
        AUTO_IMPORT_CLASSES.put("ServiceContext", "mb.dnm.core.context.ServiceContext");
        AUTO_IMPORT_CLASSES.put("AbstractDynamicCode", "mb.dnm.core.dynamic.AbstractDynamicCode");
        AUTO_IMPORT_CLASSES.put("Logger", "org.slf4j.Logger");
        AUTO_IMPORT_CLASSES.put("LoggerFactory", "org.slf4j.LoggerFactory");
        AUTO_IMPORT_CLASSES.put("Slf4j", "lombok.extern.slf4j.Slf4j");

        AUTO_IMPORT_CLASSES.put("File", "java.io.File");
        AUTO_IMPORT_CLASSES.put("InputStream", "java.io.InputStream");
        AUTO_IMPORT_CLASSES.put("OutputStream", "java.io.OutputStream");
        AUTO_IMPORT_CLASSES.put("FileInputStream", "java.io.FileInputStream");
        AUTO_IMPORT_CLASSES.put("FileOutputStream", "java.io.FileOutputStream");
        AUTO_IMPORT_CLASSES.put("FileReader", "java.io.FileReader");
        AUTO_IMPORT_CLASSES.put("FileWriter", "java.io.FileWriter");
        AUTO_IMPORT_CLASSES.put("BufferedInputStream", "java.io.BufferedInputStream");
        AUTO_IMPORT_CLASSES.put("BufferedOutputStream", "java.io.BufferedOutputStream");
        AUTO_IMPORT_CLASSES.put("BufferedWriter", "java.io.BufferedWriter");
        AUTO_IMPORT_CLASSES.put("BufferedReader", "java.io.BufferedReader");
        AUTO_IMPORT_CLASSES.put("ByteArrayInputStream", "java.io.ByteArrayInputStream");
        AUTO_IMPORT_CLASSES.put("ByteArrayOutputStream", "java.io.ByteArrayOutputStream");
        AUTO_IMPORT_CLASSES.put("IOException", "java.io.IOException");
        AUTO_IMPORT_CLASSES.put("FileNotFoundException", "java.io.FileNotFoundException");

        AUTO_IMPORT_CLASSES.put("Files", "java.nio.file.Files");
        AUTO_IMPORT_CLASSES.put("Path", "java.nio.file.Path");
        AUTO_IMPORT_CLASSES.put("Paths", "java.nio.file.Paths");
        AUTO_IMPORT_CLASSES.put("FileStore", "java.nio.file.FileStore");
        AUTO_IMPORT_CLASSES.put("FileSystem", "java.nio.file.FileSystem");
        AUTO_IMPORT_CLASSES.put("PathMatcher", "java.nio.file.PathMatcher");
        AUTO_IMPORT_CLASSES.put("FileStoreAttributeView", "java.nio.file.attribute.FileStoreAttributeView");
        AUTO_IMPORT_CLASSES.put("FileTime", "java.nio.file.attribute.FileTime");
        AUTO_IMPORT_CLASSES.put("UserPrincipal", "java.nio.file.attribute.UserPrincipal");
        AUTO_IMPORT_CLASSES.put("UserPrincipalLookupService", "java.nio.file.attribute.UserPrincipalLookupService");
        AUTO_IMPORT_CLASSES.put("GroupPrincipal", "java.nio.file.attribute.GroupPrincipal");
        AUTO_IMPORT_CLASSES.put("PosixFilePermission", "java.nio.file.attribute.PosixFilePermission");
        AUTO_IMPORT_CLASSES.put("BasicFileAttributes", "java.nio.file.attribute.BasicFileAttributes");
        AUTO_IMPORT_CLASSES.put("StandardCopyOption", "java.nio.file.StandardCopyOption");
        AUTO_IMPORT_CLASSES.put("StandardOpenOption", "java.nio.file.StandardOpenOption");
        AUTO_IMPORT_CLASSES.put("DirectoryStream", "java.nio.file.DirectoryStream");
        AUTO_IMPORT_CLASSES.put("SeekableByteChannel", "java.nio.channels.SeekableByteChannel");



    }

    private ImportSupporter() {}

    public static void assertProhibited(String source) {
        for (String keyword : PROHIBITED_KEYWORD) {
            if (source.contains(keyword)) {
                throw new IllegalStateException("Do not use the prohibited keyword '" + keyword + "' in dynamic code.");
            }
        }
    }
    
    public static Set<Class> retrieveAutoImportClasses(String source) {
        Set<Class> classes = new LinkedHashSet<>();
        for (Map.Entry<String, String> autoImport : AUTO_IMPORT_CLASSES.entrySet()) {
            String className = autoImport.getKey();
            String fullName = autoImport.getValue();
            if (source.contains(className)) {
                try {
                    classes.add(Class.forName(fullName));
                } catch (ClassNotFoundException ce) {}
            }
        }
        return classes;
    }

}
