package org.lucas.component.common.core.date;

import cn.hutool.core.date.DateUtil;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.springframework.util.StringUtils;

import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.time.temporal.ChronoField.DAY_OF_WEEK;

public class DateUtils extends DateUtil {

    static final Pattern pattern = Pattern.compile("^\\d{4}-\\d{1,2}-\\d{1,2}$");

    /**
     * 时间格式
     */
    public final static String TIME_FORMAT = "HH:mm:ss:SS";

    /**
     * 缺省短日期格式
     */
    public final static String DEFAULT_SHORT_DATE_FORMAT = "yyyy-MM-dd";

    /**
     * yyyy-MM-dd HH:mm:ss格式数据。
     */
    public final static String DEFAULT_DATE_TIME_FORMAT = "yyyy-MM-dd HH:mm:ss";

    private static DateFormat ddMMyyyySS = new SimpleDateFormat(
            "yyyyMMddHHmmss");
    private static DateFormat zstr = new SimpleDateFormat(
            DEFAULT_DATE_TIME_FORMAT);

    public static final DateTimeFormatter DTF = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public static final DateTimeFormatter DTF1 = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public static final DateTimeFormatter DTF2 = DateTimeFormatter.ofPattern("yyyyMM");

    /**
     * yyyy-MM-dd格式数据。
     */
    public final static String DATE_ONLY_FORMAT = "yyyy-MM-dd";

    public final static String DATE_MONTH_DAY_FORMAT = "yyyyMMdd";
    /**
     * 缺省短日期格式
     */
    public final static String DEFAULT_SHORT_DATE_FORMAT_ZH = "yyyy年M月d日";

    /**
     * 日期字符串（yyyyMMdd HHmmss）
     */
    public static final String YEAR_TO_SEC_UN_LINE = "yyyyMMdd HHmmss";

    /**
     * 缺省长日期格式
     */
    public final static String DEFAULT_LONG_DATE_FORMAT = DEFAULT_SHORT_DATE_FORMAT
            + " " + TIME_FORMAT;

    /**
     * Java能支持的最小日期字符串（yyyy-MM-dd）。
     */
    public final static String JAVA_MIN_SHORT_DATE_STR = "1970-01-01";

    /**
     * Java能支持的最小日期字符串（yyyy-MM-dd HH:mm:ss:SS）。
     */
    public final static String JAVA_MIN_LONG_DATE_STR = "1970-01-01 00:00:00:00";

    /**
     * Java能支持的最小的Timestamp。
     */
    public final static Timestamp JAVA_MIN_TIMESTAMP = convertStrToTimestamp(JAVA_MIN_LONG_DATE_STR);

    /**
     * 获取当前日期的上一周星期一的日期。注意只返回yyyy-MM-dd格式的数据。
     *
     * @return
     */
    public static String getMondayDateForLastWeek() {
        Calendar cal = Calendar.getInstance();
        // n为推迟的周数，1本周，-1向前推迟一周，2下周，依次类推
        int n = -1;
        cal.setFirstDayOfWeek(Calendar.MONDAY);
        cal.add(Calendar.DATE, n * 7);
        // 想周几，这里就传几Calendar.MONDAY（TUESDAY...）
        cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
        return new SimpleDateFormat("yyyy-MM-dd").format(cal.getTime());
    }

    /**
     * 获取当前日期的上一周星期日的日期。注意只返回yyyy-MM-dd格式的数据。
     *
     * @return
     */
    public static String getSundayDateForLastWeek() {
        Calendar cal = Calendar.getInstance();
        // n为推迟的周数，1本周，-1向前推迟一周，2下周，依次类推
        int n = -1;
        cal.setFirstDayOfWeek(Calendar.MONDAY);
        cal.add(Calendar.DATE, n * 7);
        // 想周几，这里就传几Calendar.MONDAY（TUESDAY...）
        cal.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);
        return new SimpleDateFormat("yyyy-MM-dd").format(cal.getTime());
    }

    public static String formatDate(String dateStr, String formatStyle) {
        Date date = convertStrToDate(dateStr, formatStyle);
        dateStr = convertDateToStr(date, formatStyle);
        return dateStr;
    }

    public static String formatDate(long milliseconds, String pattern) {
        if (milliseconds > 0) {
            if (StringUtils.isEmpty(pattern)) {
                pattern = DatePattern.Y_M_D;
            }
            final org.joda.time.format.DateTimeFormatter formatter = DateTimeFormat.forPattern(pattern);
            return formatter.print(new DateTime(milliseconds));
        }
        return "";
    }

    /**
     * 取得指定日期所在周的第一天
     */
    public static Date getFirstDayOfWeek(Date date) {
        Calendar c = new GregorianCalendar();
        c.setFirstDayOfWeek(Calendar.MONDAY);
        c.setTime(date);
        c.set(Calendar.DAY_OF_WEEK, c.getFirstDayOfWeek()); // Monday
        return c.getTime();
    }

    /**
     * 取得指定日期所在周的最后一天
     */
    public static Date getLastDayOfWeek(Date date) {
        Calendar c = new GregorianCalendar();
        c.setFirstDayOfWeek(Calendar.MONDAY);
        c.setTime(date);
        c.set(Calendar.DAY_OF_WEEK, c.getFirstDayOfWeek() + 6); // Sunday
        return c.getTime();
    }

    /**
     * 获取指定日期在当年中的所在周数。
     *
     * @param dateStr 年月日 时分秒。
     */
    public static int getWeekOfYearByDate(String dateStr) {
        Calendar calendar = Calendar.getInstance();// new GregorianCalendar();
        Date date = convertStrToDate(dateStr,
                DEFAULT_DATE_TIME_FORMAT);
        calendar.setTime(date);
        return calendar.get(Calendar.WEEK_OF_YEAR);
    }

    /**
     * 把字符串转换为Timestamp类型，对于短日期格式，自动把时间设为系统当前时间。
     *
     * @return Timestamp
     * @see #convertStrToTimestamp(String, boolean)
     */
    public static Timestamp convertStrToTimestamp(String dateStr) {
        return convertStrToTimestamp(dateStr, false);
    }

    /**
     * 把字符串转换为Timestamp类型，对于短日期格式，自动把时间设为0。
     *
     * @return Timestamp
     * @see #convertStrToTimestamp(String, boolean)
     */
    public static Timestamp convertStrToTimestampZero(String dateStr) {
        return convertStrToTimestamp(dateStr, true);
    }

    /**
     * 把字符串转换为Timestamp类型。
     *
     * @param dateStr     - 日期字符串，只支持"yyyy-MM-dd"和"yyyy-MM-dd HH:mm:ss:SS"两种格式。
     *                    如果为"yyyy-MM-dd"，系统会自动取得当前时间补上。
     * @param addZeroTime - 当日期字符串为"yyyy-MM-dd"这样的格式时，addZeroTime为true表示
     *                    用0来设置HH:mm:ss:SS，否则用当前Time来设置。
     * @return Timestamp
     */
    private static Timestamp convertStrToTimestamp(String dateStr,
                                                   boolean addZeroTime) {
        if (dateStr == null) {
            return null;
        }

        String dStr = dateStr.trim();
        if (dStr.indexOf(" ") == -1) {
            if (addZeroTime) {
                dStr = dStr + " 00:00:00:00";
            } else {
                dStr = dStr + " " + getCurrDateStr(TIME_FORMAT);
            }
        }

        Date utilDate = null;
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(
                DEFAULT_LONG_DATE_FORMAT);

        try {
            utilDate = simpleDateFormat.parse(dStr);
        } catch (Exception ex) {
            throw new RuntimeException("DateUtil.convertStrToTimestamp(): "
                    + ex.getMessage());
        }

        return new Timestamp(utilDate.getTime());
    }

    /**
     * 得到系统当前时间的Timestamp对象
     *
     * @return 系统当前时间的Timestamp对象
     */
    public static Timestamp getCurrTimestamp() {
        return new Timestamp(System.currentTimeMillis());
    }

    /**
     * <p>
     * 取得当前日期，并将其转换成格式为"dateFormat"的字符串 例子：假如当前日期是 2003-09-24 9:19:10，则：
     * <p>
     * <pre>
     * getCurrDateStr("yyyyMMdd")="20030924"
     * getCurrDateStr("yyyy-MM-dd")="2003-09-24"
     * getCurrDateStr("yyyy-MM-dd HH:mm:ss")="2003-09-24 09:19:10"
     * </pre>
     * <p>
     * </p>
     *
     * @param dateFormat String 日期格式字符串
     * @return String
     */
    public static String getCurrDateStr(String dateFormat) {
        return convertDateToStr(new Date(), dateFormat);
    }

    /**
     * @param date 2013-11-07 14:14:14
     * @return 20131107141414
     */
    public static String formateDate(Date date) {
        String str1 = "";
        try {
            str1 = ddMMyyyySS.format(date);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return str1;
    }

    public static String formateDateStr(Date date) {
        String str1 = "";
        try {
            str1 = zstr.format(date);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return str1;
    }

    /**
     * 将日期类型转换成指定格式的日期字符串
     *
     * @param date       待转换的日期
     * @param dateFormat 日期格式字符串
     * @return String
     */
    public static String convertDateToStr(Date date, String dateFormat) {
        if (date == null) {
            return "";
        }
        SimpleDateFormat sdf = new SimpleDateFormat(dateFormat);
        return sdf.format(date);
    }

    public static Date formatDatePattern(String str, String srcPattern) throws ParseException {
        if (StringUtils.isEmpty(str.trim())) {
            return null;
        } else {
            SimpleDateFormat df = new SimpleDateFormat(srcPattern);
            Date date = df.parse(str);
            return date;
        }
    }

    /**
     * 将指定格式的字符串转换成日期类型
     *
     * @param dateFormat 日期格式字符串
     * @return Date
     */
    public static Date convertStrToDate(String dateStr, String dateFormat) {
        if (dateStr == null || dateStr.equals("")) {
            return null;
        }
        SimpleDateFormat sdf = new SimpleDateFormat(dateFormat);
        try {
            return sdf.parse(dateStr);
        } catch (Exception e) {
            throw new RuntimeException("DateUtil.convertStrToDate():" + e.getMessage());
        }
    }

    /**
     * 计算两个日期之间的相隔的年、月、日。注意：只有计算相隔天数是准确的，相隔年和月都是 近似值，按一年365天，一月30天计算，忽略闰年和闰月的差别。
     *
     * @param datepart  两位的格式字符串，yy表示年，MM表示月，dd表示日
     * @param startdate 开始日期
     * @param enddate   结束日期
     * @return double 如果enddate>startdate，返回一个大于0的实数，否则返回一个小于等于0的实数
     */
    public static double dateDiff(String datepart, Date startdate, Date enddate) {
        if (datepart == null || datepart.equals("")) {
            throw new IllegalArgumentException("DateUtil.dateDiff()方法非法参数值："
                    + datepart);
        }

        double days = (double) (enddate.getTime() - startdate.getTime())
                / (60 * 60 * 24 * 1000);

        if (datepart.equals("yy")) {
            days = days / 365;
        } else if (datepart.equals("MM")) {
            days = days / 30;
        } else if (datepart.equals("dd")) {
            return days;
        } else {
            throw new IllegalArgumentException("DateUtil.dateDiff()方法非法参数值："
                    + datepart);
        }
        return days;
    }

    /**
     * 把日期对象加减年、月、日后得到新的日期对象
     *
     * @param number 如果是 减就 -1
     *               加减因子
     * @param date   需要加减年、月、日的日期对象
     * @return Date 新的日期对象
     */
    public static Date addDate(String datepart, int number, Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        if (datepart.equals("yy")) {
            cal.add(Calendar.YEAR, number);
        } else if (datepart.equals("MM")) {
            cal.add(Calendar.MONTH, number);
        } else if (datepart.equals("dd")) {
            cal.add(Calendar.DATE, number);
        } else if (datepart.equals("HH")) {
            cal.add(Calendar.HOUR, number);
        } else {
            throw new IllegalArgumentException("DateUtil.addDate()方法非法参数值：" + datepart);
        }

        return cal.getTime();
    }

    /**
     * 将普通时间 格式的字符串转化成unix时间戳
     *
     * @param dateStr
     * @param dateFormat
     * @return
     * @version 1.0
     */
    public static long convertDateStrToUnixTimeStamp(String dateStr, String dateFormat) {
        long timeStamp = convertStrToDate(dateStr, dateFormat).getTime() / 1000;
        return timeStamp;
    }

    /**
     * 将unix时间戳转化成普通时间 格式的字符串
     *
     * @param timeStamp
     * @param dateFormat
     * @return
     * @version 1.0
     */
    public static String convertUnixTimeStampToDateStr(long timeStamp, String dateFormat) {
        String dateStr = "";
        if (timeStamp != 0) {
            Long timestamp = Long.parseLong(timeStamp + "") * 1000;
            dateStr = convertDateToStr(new Date(timestamp), dateFormat);
        }
        return dateStr;
    }

    /**
     * 获取当前unix时间的秒数。
     *
     * @return
     * @version 1.0
     * @data 2013-8-9 上午9:50:43
     */
    public static long getCurrentUnixTimeSecond() {
        return getCurrTimestamp().getTime() / 1000;
    }

    /**
     * 获取当前unix时间的毫秒数。
     *
     * @return
     * @version 1.0
     * @data 2013-8-9 上午9:50:43
     */
    public static long getCurrentTimeMillis() {
        return getCurrTimestamp().getTime();
    }

    /**
     * 将日期转换成毫秒数
     *
     * @param dateStr
     * @return
     */
    public static long getTimeByFormatDate(String dateStr) {
        try {
            return new SimpleDateFormat(DEFAULT_DATE_TIME_FORMAT).parse(dateStr).getTime();
        } catch (ParseException e) {
            return 0;
        }
    }

    /**
     * 判断一个日期是否超过了给定的日期
     *
     * @return
     */
    public static boolean calcMillisTime(String dateStr, long differTime) {
        long dateMillesTime = getTimeByFormatDate(dateStr);
        long currentMullesTime = getCurrentTimeMillis();
        return currentMullesTime - dateMillesTime > differTime;
    }

    /**
     * @param str
     * @return void
     * @throws ParseException
     * @Title: formatString
     * @Description: 20131010121212 这个格式转换成 yyyy-MM-dd HH:mm:ss
     * @author dao
     * @date 2013年10月31日 下午7:57:23
     */
    public static String formateStirng(String str) {
        String str1 = "";
        try {
            DateFormat format = new SimpleDateFormat("yyyyMMddHHmmss");
            DateFormat format2 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

            Date dDate = format.parse(str);
            str1 = format2.format(dDate);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return str1;
    }

    /**
     * 如果该日期字符串是有效的返回true
     *
     * @param date
     * @return
     */
    public static boolean isLagelDateOfString(String date) {
        Matcher match = pattern.matcher(date);
        return match.matches();
    }

    /**
     * @param @param  nowDate 当前日期
     * @param @param  oneDate 把日期往后增加一天.整数往后推,负数往前移动
     * @param @return 设定文件
     * @return String    返回类型
     * @throws
     * @Title: getOneDateStr
     * @Description: 获取某一天的日期
     * @author xiaoheliu
     * @date 2016年8月17日 下午5:03:32
     */
    public static String getOneDateStr(Date nowDate, int oneDate) {
        Calendar calendar = new GregorianCalendar();
        calendar.setTime(nowDate);
        calendar.add(Calendar.DATE, oneDate);//把日期往后增加一天.整数往后推,负数往前移动
        nowDate = calendar.getTime(); //这个时间就是日期往后推一天的结果
        SimpleDateFormat formatter = new SimpleDateFormat(DEFAULT_SHORT_DATE_FORMAT);
        return formatter.format(nowDate);
    }

    /**
     * @param @param  smdate
     * @param @param  bdate
     * @param @return
     * @param @throws ParseException    设定文件
     * @return int    返回类型
     * @throws
     * @Title: daysBetween
     * @Description: 计算两个日期之间的天数
     * @author xiaoheliu
     * @date 2016年8月18日 上午11:51:26
     */
    public static int daysBetween(String smdate, String bdate) {
        SimpleDateFormat sdf = new SimpleDateFormat(DEFAULT_SHORT_DATE_FORMAT);
        Calendar cal = Calendar.getInstance();
        try {
            cal.setTime(sdf.parse(smdate));
        } catch (ParseException e) {
            return -1;
        }
        long time1 = cal.getTimeInMillis();
        try {
            cal.setTime(sdf.parse(bdate));
        } catch (ParseException e) {
            return -1;
        }
        long time2 = cal.getTimeInMillis();
        long between_days = (time2 - time1) / (1000 * 3600 * 24);
        return Integer.parseInt(String.valueOf(between_days));
    }

    /**
     * 计算两个日期之间的秒数
     *
     * @param smdate
     * @param bdate
     * @return
     */
    public static int secondBetween(String smdate, String bdate) {
        SimpleDateFormat sdf = new SimpleDateFormat(DEFAULT_DATE_TIME_FORMAT);
        Calendar cal = Calendar.getInstance();
        try {
            cal.setTime(sdf.parse(smdate));
        } catch (ParseException e) {
            return -1;
        }
        long time1 = cal.getTimeInMillis();
        try {
            cal.setTime(sdf.parse(bdate));
        } catch (ParseException e) {
            return -1;
        }
        long time2 = cal.getTimeInMillis();
        long between_days = (time2 - time1) / 1000;
        return Integer.parseInt(String.valueOf(between_days));
    }

    /**
     * 获取当前月的某一天
     *
     * @param day
     * @return
     */
    public static Date getWhenDayOfMonth(int day) {
        String sdf = "yyyy-MM-dd";
        Calendar lastDate = Calendar.getInstance();
        lastDate.set(Calendar.DATE, day);//设为当前月的1 号
        String dateStr = convertDateToStr(lastDate.getTime(), sdf);
        return convertStrToDate(dateStr, sdf);
    }

    /**
     * 获取上月的第一天
     *
     * @return
     */
    public static Date getPreviousMonthFirst() {
        String sdf = "yyyy-MM-dd";
        Calendar lastDate = Calendar.getInstance();
        lastDate.set(Calendar.DATE, 1);//设为当前月的1 号
        lastDate.add(Calendar.MONTH, -1);//减一个月，变为下月的1 号
        String dateStr = convertDateToStr(lastDate.getTime(), sdf);
        return convertStrToDate(dateStr, sdf);
    }

    /**
     * 获取下月的第一天
     *
     * @return
     */
    public static Date getNextMonthFirst() {
        String sdf = "yyyy-MM-dd";
        Calendar lastDate = Calendar.getInstance();
        lastDate.set(Calendar.DATE, 1);//设为当前月的1 号
        lastDate.add(Calendar.MONTH, 1);//减一个月，变为下月的1 号
        String dateStr = convertDateToStr(lastDate.getTime(), sdf);
        return convertStrToDate(dateStr, sdf);
    }

    /**
     * 获取当前月第一天
     *
     * @return
     */
    public Date getCurrentMonthFirst() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Calendar lastDate = Calendar.getInstance();
        lastDate.set(Calendar.DATE, 1);//设为当前月的1 号
        return lastDate.getTime();
    }

    /**
     * 获取当前月第一天
     *
     * @return
     */
    public static Date getStaticCurrentMonthFirst() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Calendar lastDate = Calendar.getInstance();
        lastDate.set(Calendar.DATE, 1);//设为当前月的1 号
        return lastDate.getTime();
    }

    /**
     * 根据两个时间间隔，生成指定天数.比如：2018-07-08 到2018-07-11
     * 生成
     * 2018-07-08
     * 2018-07-09
     * 2018-07-10
     * 2018-07-11
     *
     * @param startDate
     * @param endDate
     * @return
     */
    public static List<String> generateDateInterval(String startDate, String endDate) {
        List list = null;

        if (!StringUtils.isEmpty(startDate) && !StringUtils.isEmpty(endDate)) {
            LocalDate startLocalDate = LocalDate.parse(startDate);
            LocalDate endLocalDate = LocalDate.parse(endDate);
            //相差天数
            Long sub = endLocalDate.toEpochDay() - startLocalDate.toEpochDay();
            //转为int
            int i = sub.intValue();

            if (i >= 0) {
                //计算出具体容量
                list = new ArrayList(i + 1);
            } else {
                return null;
            }

            do {
                list.add(startLocalDate.format(DTF1));
                startLocalDate = startLocalDate.plusDays(1);
            } while (startLocalDate.toEpochDay() <= endLocalDate.toEpochDay());
        } else if (startDate == null && endDate == null) {
            list = new ArrayList(1);
            list.add(LocalDate.now().minusDays(1).format(DTF1));
        }
        return list;
    }

    /**
     * 计算两个时间 相差的天数
     *
     * @param startDate
     * @param endDate
     * @return
     */
    public static long getDifferDays(String startDate, String endDate) {
        LocalDate s1 = LocalDate.parse(startDate, DTF1);
        LocalDate s2 = LocalDate.parse(endDate, DTF1);
        return s2.toEpochDay() - s1.toEpochDay();
    }

    /**
     * 0 当天 1 本周 2 本月 3 本年 4 昨天 5 上月
     *
     * @param i
     * @return
     */
    public static Map<String, String> getCycleDate(int i, String format) {
        Map<String, String> map = new HashMap<>();
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern(format);
        String startTime = null;
        String endTime = null;
        //当前时间
        LocalDateTime currentTime = LocalDateTime.now();
        if (i == 0) {
            startTime = dateTimeFormatter.format(currentTime.withHour(0).withMinute(0).withSecond(0));
            endTime = dateTimeFormatter.format(currentTime.withHour(23).withMinute(59).withSecond(59));
        } else if (i == 1) {
            startTime = dateTimeFormatter.format(currentTime.with((temporal) -> temporal.with(DAY_OF_WEEK, 1)).withHour(0).withMinute(0).withSecond(0));
            endTime = dateTimeFormatter.format(currentTime.with((temporal) -> temporal.with(DAY_OF_WEEK, 7)).withHour(23).withMinute(59).withSecond(59));
        } else if (i == 2) {
            startTime = dateTimeFormatter.format(currentTime.with(TemporalAdjusters.firstDayOfMonth()).withHour(0).withMinute(0).withSecond(0));
            endTime = dateTimeFormatter.format(currentTime.with(TemporalAdjusters.lastDayOfMonth()).withHour(23).withMinute(59).withSecond(59));
        } else if (i == 3) {
            startTime = dateTimeFormatter.format(currentTime.with(TemporalAdjusters.firstDayOfYear()).withHour(0).withMinute(0).withSecond(0));
            endTime = dateTimeFormatter.format(currentTime.with(TemporalAdjusters.lastDayOfYear()).withHour(23).withMinute(59).withSecond(59));
        } else if (i == 4) {
            LocalDateTime yesterdayTime = currentTime.minusDays(1);
            startTime = dateTimeFormatter.format(yesterdayTime.withHour(0).withMinute(0).withSecond(0));
            endTime = dateTimeFormatter.format(yesterdayTime.withHour(23).withMinute(59).withSecond(59));
        } else if (i == 5) {
            LocalDateTime prevTime = currentTime.minusMonths(1);
            startTime = dateTimeFormatter.format(prevTime.with(TemporalAdjusters.firstDayOfMonth()).withHour(0).withMinute(0).withSecond(0));
            endTime = dateTimeFormatter.format(prevTime.with(TemporalAdjusters.lastDayOfMonth()).withHour(23).withMinute(59).withSecond(59));
        }
        map.put("startTime", startTime);
        map.put("endTime", endTime);
        return map;
    }

    /**
     * 获取当前日期
     *
     * @return int
     */
    public static int getDay() {
        Calendar cal = Calendar.getInstance();
        int day = cal.get(Calendar.DATE);//获取日
        return day;
    }
}
