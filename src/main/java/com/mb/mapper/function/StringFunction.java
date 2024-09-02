package com.mb.mapper.function;

import lombok.val;
import org.apache.commons.lang3.StringUtils;

import java.lang.reflect.Method;

public class StringFunction {

    public String substring(String val, int startIdx) {
        return val.substring(startIdx);
    }

    public String substring(String val, int startIdx, int endIdx) {
        return val.substring(startIdx, endIdx);
    }

    public int length(String val) {
        return val.length();
    }

    public String build(String delimiter, String ...vals) {
        StringBuilder sb = new StringBuilder();
        for (String val : vals) {
            sb.append(val);
            sb.append(delimiter);
        }
        if (sb.length() > 0) {
            sb.setLength(sb.length() - delimiter.length());
        }
        return sb.toString();
    }

    public String toUpperCase(String val) {
        return val.toUpperCase();
    }

    public String toLowerCase(String val) {
        return val.toLowerCase();
    }

    public String trim(String val) {
        return val.trim();
    }

    public String ltrim(String val) {
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

    public String rtrim(String val) {
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

    public String lpad(String val, int len) {
        return StringUtils.leftPad(val, len);
    }

    public String lpad(String val, int len, String pad) {
        return StringUtils.leftPad(val, len, pad);
    }

    public String rpad(String val, int len, String pad) {
        return StringUtils.rightPad(val, len, pad);
    }

    public String rpad(String val, int len) {
        return StringUtils.rightPad(val, len);
    }


}
