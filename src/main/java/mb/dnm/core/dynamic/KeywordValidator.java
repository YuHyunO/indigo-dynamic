package mb.dnm.core.dynamic;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class KeywordValidator {

    private final static Map<String, String> AVAILABLE_CLASSES = new HashMap<>();
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
