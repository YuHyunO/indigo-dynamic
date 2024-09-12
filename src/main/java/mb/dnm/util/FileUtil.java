package mb.dnm.util;

public class FileUtil {

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
}
