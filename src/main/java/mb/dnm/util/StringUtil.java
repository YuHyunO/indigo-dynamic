package mb.dnm.util;

import org.apache.commons.lang3.StringUtils;

/**
 * The type String util.
 */
public class StringUtil {

    /**
     * Substring string.
     *
     * @param val      the val
     * @param startIdx the start idx
     * @return the string
     */
    public static String substring(String val, int startIdx) {
        return val.substring(startIdx);
    }

    /**
     * Substring string.
     *
     * @param val      the val
     * @param startIdx the start idx
     * @param endIdx   the end idx
     * @return the string
     */
    public static String substring(String val, int startIdx, int endIdx) {
        return val.substring(startIdx, endIdx);
    }

    /**
     * Length int.
     *
     * @param val the val
     * @return the int
     */
    public static int length(String val) {
        return val.length();
    }

    /**
     * Build string.
     *
     * @param delimiter the delimiter
     * @param vals      the vals
     * @return the string
     */
    public static String build(String delimiter, Object ...vals) {
        StringBuilder sb = new StringBuilder();
        if (vals == null) {
            return "";
        }
        for (Object val : vals) {
            sb.append(val);
            sb.append(delimiter);
        }
        if (sb.length() > 0) {
            sb.setLength(sb.length() - delimiter.length());
        }
        return sb.toString();
    }

    /**
     * To upper case string.
     *
     * @param valObj the val obj
     * @return the string
     */
    public static String toUpperCase(Object valObj) {
        if (valObj == null)
            return null;

        return String.valueOf(valObj).toUpperCase();
    }

    /**
     * To lower case string.
     *
     * @param valObj the val obj
     * @return the string
     */
    public static String toLowerCase(Object valObj) {
        if (valObj == null)
            return null;

        return String.valueOf(valObj).toLowerCase();
    }

    /**
     * Trim string.
     *
     * @param valObj the val obj
     * @return the string
     */
    public static String trim(Object valObj) {
        if (valObj == null)
            valObj = "";

        return String.valueOf(valObj).trim();
    }

    /**
     * Left trim string.
     *
     * @param valObj the val obj
     * @return the string
     */
    public static String ltrim(Object valObj) {
        if (valObj == null)
            valObj = "";
        String val = String.valueOf(valObj);
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

    /**
     * Right trim string.
     *
     * @param valObj the val obj
     * @return the string
     */
    public static String rtrim(Object valObj) {
        if (valObj == null)
            valObj = "";
        String val = String.valueOf(valObj);
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

    /**
     * Left pad string.
     *
     * @param valObj the val obj
     * @param len    the len
     * @return the string
     */
    public static String lpad(Object valObj, int len) {
        if (valObj == null)
            valObj = "";
        return StringUtils.leftPad(String.valueOf(valObj), len);
    }

    /**
     * Left pad string.
     *
     * @param valObj the val obj
     * @param len    the len
     * @param pad    the pad
     * @return the string
     */
    public static String lpad(Object valObj, int len, Object pad) {
        if (valObj == null)
            valObj = "";
            //return null;
        if (pad == null)
            return StringUtils.leftPad(String.valueOf(valObj), len);
        return StringUtils.leftPad(String.valueOf(valObj), len, String.valueOf(pad));
    }

    /**
     * Right pad string.
     *
     * @param valObj the val obj
     * @param len    the len
     * @param pad    the pad
     * @return the string
     */
    public static String rpad(Object valObj, int len, Object pad) {
        if (valObj == null)
            valObj = "";
        if (pad == null)
            return StringUtils.leftPad(String.valueOf(valObj), len);
        return StringUtils.rightPad(String.valueOf(valObj), len, String.valueOf(pad));
    }

    /**
     * Right pad string.
     *
     * @param valObj the val obj
     * @param len    the len
     * @return the string
     */
    public static String rpad(Object valObj, int len) {
        if (valObj == null)
            valObj = "";
        return StringUtils.rightPad(String.valueOf(valObj), len);
    }


}
