package mb.dnm.util;

import java.text.SimpleDateFormat;
import java.util.Date;

public class TimeUtil {
    public static final String YYYY = "yyyy";
    public static final String MMDD = "MMdd";
    public static final String YYYYMM = "yyyyMM";
    public static final String YYYYMMDD = "yyyyMMdd";
    public static final String YYYYMMDDHHMMSS = "yyyyMMddHHmmss";
    public static final String YYYYMMDDHHMMSSFFF = "yyyyMMddHHmmssSSS";
    public static final String TIMESTAMP_FORMAT = "yyyyMMddHHmmssSSS";
    public static final String DATETIME_FORMAT = "yyyyMMddHHmmss";
    public static final String DATE_FORMAT = "yyyyMMdd";
    public static final String TIME_FORMAT = "HHmmss";
    public static final String JDBC_TIMESTAMP_FORMAT = "yyyy-MM-dd HH:mm:sss.SSSSSSSSS";
    public static final String JDBC_DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";

    private TimeUtil(){}

    public static String getFormattedTime(Date date, String format) {
        if (date == null) {
            return null;
        }
        SimpleDateFormat formatter = new SimpleDateFormat(format);
        return formatter.format(date);
    }

    public static String curTimeStamp() {
        return new SimpleDateFormat(TIMESTAMP_FORMAT).format(new Date());
    }

    public static String curDate(String timeFormat) {
        return new SimpleDateFormat(timeFormat).format(new Date());
    }

    public static String toStringDate(Date date, String timeFormat) {
        return new SimpleDateFormat(timeFormat).format(date);
    }

    public static long toLongDatetime(Date date) {
        return Long.parseLong(new SimpleDateFormat(DATETIME_FORMAT).format(date));
    }

}
