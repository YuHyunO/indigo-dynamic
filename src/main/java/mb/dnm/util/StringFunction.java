package mb.dnm.util;

import org.apache.commons.lang3.StringUtils;

public class StringFunction {

    public static String substring(String val, int startIdx) {
        return val.substring(startIdx);
    }

    public static String substring(String val, int startIdx, int endIdx) {
        return val.substring(startIdx, endIdx);
    }

    public static int length(String val) {
        return val.length();
    }

    public static String build(String delimiter, Object ...vals) {
        StringBuilder sb = new StringBuilder();
        for (Object val : vals) {
            sb.append(val);
            sb.append(delimiter);
        }
        if (sb.length() > 0) {
            sb.setLength(sb.length() - delimiter.length());
        }
        return sb.toString();
    }

    public static String toUpperCase(String val) {
        return val.toUpperCase();
    }

    public static String toLowerCase(String val) {
        return val.toLowerCase();
    }

    public static String trim(String val) {
        return val.trim();
    }

    public static String ltrim(String val) {
        char[] chars = val.toCharArray();
        int ltrimIdx = 0;
        for (int i = 0; i < chars.length; i++) {
            if (chars[i] == ' ') {
                ++ltrimIdx;
            } else {
                break;
            }
        }
        return ltrimIdx != 0 ? val.substring(ltrimIdx) : val;
    }

    public static String rtrim(String val) {
        char[] chars = val.toCharArray();
        int rtrimIdx = chars.length - 1;
        for (int i = chars.length - 1; i >= 0; i--) {
            if (chars[i] == ' ') {
                --rtrimIdx;
            } else {
                break;
            }
        }
        return rtrimIdx != chars.length ? val.substring(0, rtrimIdx + 1) : val;
    }

    public static String lpad(String val, int len) {
        return StringUtils.leftPad(val, len);
    }

    public static String lpad(String val, int len, String pad) {
        return StringUtils.leftPad(val, len, pad);
    }

    public static String rpad(String val, int len, String pad) {
        return StringUtils.rightPad(val, len, pad);
    }

    public static String rpad(String val, int len) {
        return StringUtils.rightPad(val, len);
    }


}
