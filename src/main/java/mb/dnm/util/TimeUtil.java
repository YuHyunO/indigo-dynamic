package mb.dnm.util;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * The type Time util.
 */
public class TimeUtil {
    /**
     * The constant time format yyyy.
     */
    public static final String YYYY = "yyyy";
    /**
     * The constant time format MMdd.
     */
    public static final String MMDD = "MMdd";
    /**
     * The constant time format HHmm.
     */
    public static final String HHmm = "HHmm";
    /**
     * The constant time format mmss.
     */
    public static final String mmss = "mmss";
    /**
     * The constant time format yyyyMM.
     */
    public static final String YYYYMM = "yyyyMM";
    /**
     * The constant time format yyyyMMdd.
     */
    public static final String YYYYMMDD = "yyyyMMdd";
    /**
     * The constant time format yyyyMMddHH.
     */
    public static final String YYYYMMDDHH = "yyyyMMddHH";
    /**
     * The constant time format yyyyMMddHHmm.
     */
    public static final String yyyyMMddHHmm = "yyyyMMddHHmm";
    /**
     * The constant time format yyyyMMddHHmmss.
     */
    public static final String YYYYMMDDHHmmss = "yyyyMMddHHmmss";
    /**
     * The constant time format yyyyMMddHHmmssSSS.
     */
    public static final String YYYYMMDDHHmmssSSS = "yyyyMMddHHmmssSSS";
    /**
     * The constant time format yyyyMMddHHmmssSSS.
     */
    public static final String TIMESTAMP_FORMAT = "yyyyMMddHHmmssSSS";
    /**
     * The constant time format yyyyMMddHHmmss.
     */
    public static final String DATETIME_FORMAT = "yyyyMMddHHmmss";
    /**
     * The constant time format yyyyMMdd.
     */
    public static final String DATE_FORMAT = "yyyyMMdd";
    /**
     * The constant time format HHmmss.
     */
    public static final String HHmmss = "HHmmss";
    /**
     * The constant time format yyyy-MM-dd HH:mm:sss.SSSSSSSSS.
     */
    public static final String JDBC_TIMESTAMP_FORMAT = "yyyy-MM-dd HH:mm:sss.SSSSSSSSS";
    /**
     * The constant time format yyyy-MM-dd HH:mm:ss.
     */
    public static final String JDBC_DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";

    private TimeUtil(){}

    /**
     * Gets formatted time.
     * <br>
     * <pre style="border: 1px solid #ccc; padding: 10px; border-radius: 5px;">
     * TimeUtil.getFormattedTime(new Date(), TimeUtil.DATETIME_FORMAT)</pre>
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
     * <pre style="border: 1px solid #ccc; padding: 10px; border-radius: 5px;">
     *     public static String curTimeStamp() {
     *         return new SimpleDateFormat(TIMESTAMP_FORMAT).format(new Date());
     *     }</pre>
     * @return the string
     */
    public static String curTimeStamp() {
        return new SimpleDateFormat(TIMESTAMP_FORMAT).format(new Date());
    }

    /**
     * Cur date string.
     *<pre style="border: 1px solid #ccc; padding: 10px; border-radius: 5px;">
     *     public static String curDate(String timeFormat) {
     *         return new SimpleDateFormat(timeFormat).format(new Date());
     *     }</pre>
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
     *<pre style="border: 1px solid #ccc; padding: 10px; border-radius: 5px;">
     *     public static long toLongDatetime(Date date) {
     *         return Long.parseLong(new SimpleDateFormat(DATETIME_FORMAT).format(date));
     *     }</pre>
     * @param date the date
     * @return the long
     */
    public static long toLongDatetime(Date date) {
        return Long.parseLong(new SimpleDateFormat(DATETIME_FORMAT).format(date));
    }

}
