package mb.dnm.util;

public class CastUtil {

    public static <T>T simpleTypeCast(Class type, Object value) {
        if (type == boolean.class || type == Boolean.class) {
            return (T) Boolean.valueOf(String.valueOf(value));
        }
        if (type == int.class || type == Integer.class) {
            return (T) Integer.valueOf(String.valueOf(value));
        }
        if (type == long.class || type == Long.class) {
            return (T) Long.valueOf(String.valueOf(value));
        }
        if (type == float.class || type == Float.class) {
            return (T) Float.valueOf(String.valueOf(value));
        }
        if (type == double.class || type == Double.class) {
            return (T) Double.valueOf(String.valueOf(value));
        }
        if (type == String.class) {
            return (T) String.valueOf(value);
        }

        throw new IllegalArgumentException("Not supported type: " + type);
    }

}
