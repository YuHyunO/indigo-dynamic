package mb.dnm.util;

import java.io.File;

public class FileUtil {
    private FileUtil(){}

    public static String removeLastPathSeparator(String path) {
        StringBuilder sb = new StringBuilder(path.trim());
        char last;
        int len = 0;

        while (true) {
            len = sb.length();
            if (len == 0 || len == 1)
                return sb.toString();

            last = sb.charAt(len - 1);
            if (last == '/' || last == '\\') {
                sb.setLength(len - 1);
                continue;
            }
            break;
        }
        return sb.toString();
    }

    public static String supposeFileSeparator(String path) {
        boolean slash = path.contains("/");
        boolean backslash = path.contains("\\");
        if ((slash && backslash) || (!slash && !backslash)) {
            return "";
        }else if (slash) {
            return "/";
        }else {
            return "\\";
        }
    }

    public static String replaceSeparator(String path, String separator) {
        if (separator == null || separator.isEmpty() || !(separator.equals("/") || separator.equals("\\"))) {
            throw new IllegalArgumentException("Invalid separator: " + separator);
        }
        String oldSep = supposeFileSeparator(path);
        if (!separator.equals(oldSep)) {
            return path.replace(oldSep, separator);
        }
        return path;
    }

    public static String replaceToOSFileSeparator(String path) {
        char sep = File.separatorChar;
        if (sep == '\\') {
            path = path.replace('/', sep);
        } else {
            path = path.replace('\\', sep);
        }
        return path;
    }
}
