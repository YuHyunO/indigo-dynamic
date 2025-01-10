package mb.dnm.util;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * The type Time util.
 */
public class TimeUtil {
    /**
     * The constant YYYY.
     */
    public static final String YYYY = "yyyy";
    /**
     * The constant MMDD.
     */
    public static final String MMDD = "MMdd";
    /**
     * The constant HHmm.
     */
    public static final String HHmm = "HHmm";
    /**
     * The constant mmss.
     */
    public static final String mmss = "mmss";
    /**
     * The constant YYYYMM.
     */
    public static final String YYYYMM = "yyyyMM";
    /**
     * The constant YYYYMMDD.
     */
    public static final String YYYYMMDD = "yyyyMMdd";
    /**
     * The constant YYYYMMDDHH.
     */
    public static final String YYYYMMDDHH = "yyyyMMddHH";
    /**
     * The constant YYYYMMDDHHmm.
     */
    public static final String YYYYMMDDHHmm = "yyyyMMddHHmm";
    /**
     * The constant YYYYMMDDHHmmss.
     */
    public static final String YYYYMMDDHHmmss = "yyyyMMddHHmmss";
    /**
     * The constant YYYYMMDDHHmmssSSS.
     */
    public static final String YYYYMMDDHHmmssSSS = "yyyyMMddHHmmssSSS";
    /**
     * The constant TIMESTAMP_FORMAT.
     */
    public static final String TIMESTAMP_FORMAT = "yyyyMMddHHmmssSSS";
    /**
     * The constant DATETIME_FORMAT.
     */
    public static final String DATETIME_FORMAT = "yyyyMMddHHmmss";
    /**
     * The constant DATE_FORMAT.
     */
    public static final String DATE_FORMAT = "yyyyMMdd";
    /**
     * The constant HHmmss.
     */
    public static final String HHmmss = "HHmmss";
    /**
     * The constant JDBC_TIMESTAMP_FORMAT.
     */
    public static final String JDBC_TIMESTAMP_FORMAT = "yyyy-MM-dd HH:mm:sss.SSSSSSSSS";
    /**
     * The constant JDBC_DATE_FORMAT.
     */
    public static final String JDBC_DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";

    private TimeUtil(){}

    /**
     * Gets formatted time.
     *
     * @param date   the date
     * @param format the format
     * @return the formatted time
     */
    public static String getFormattedTime(Date date, String format) {
        if (date == null) {
            return null;
        }
        SimpleDateFormat formatter = new SimpleDateFormat(format);
        return formatter.format(date);
    }

    /**
     * Cur time stamp string.
     *
     * @return the string
     */
    public static String curTimeStamp() {
        return new SimpleDateFormat(TIMESTAMP_FORMAT).format(new Date());
    }

    /**
     * Cur date string.
     *
     * @param timeFormat the time format
     * @return the string
     */
    public static String curDate(String timeFormat) {
        return new SimpleDateFormat(timeFormat).format(new Date());
    }

    /**
     * To string date string.
     *
     * @param date       the date
     * @param timeFormat the time format
     * @return the string
     */
    public static String toStringDate(Date date, String timeFormat) {
        return new SimpleDateFormat(timeFormat).format(date);
    }

    /**
     * To long datetime long.
     *
     * @param date the date
     * @return the long
     */
    public static long toLongDatetime(Date date) {
        return Long.parseLong(new SimpleDateFormat(DATETIME_FORMAT).format(date));
    }

}
