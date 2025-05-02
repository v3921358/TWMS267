package tools;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

/**
 * Provides a suite of tools for manipulating Korean Timestamps.
 *
 * @author Frz
 * @version 1.0
 * @since Revision 746
 */
public final class DateUtil {

    private final static int ITEM_YEAR2000 = -1085019342;
    private final static long REAL_YEAR2000 = 946681229830L;
    private final static int QUEST_UNIXAGE = 27111908;
    private final static long FT_UT_OFFSET = 116444520000000000L; // 100 nsseconds from 1/1/1601 -> 1/1/1970

    /**
     * Gets a timestamp for item expiration.
     *
     * @param realTimestamp The actual timestamp in milliseconds.
     * @return The Korean timestamp for the real timestamp.
     */
    public static int getItemTimestamp(long realTimestamp) {
        int time = (int) ((realTimestamp - REAL_YEAR2000) / 1000 / 60); // convert to minutes
        return (int) (time * 35.762787) + ITEM_YEAR2000;
    }

    /**
     * Gets a timestamp for quest repetition.
     *
     * @param realTimestamp The actual timestamp in milliseconds.
     * @return The Korean timestamp for the real timestamp.
     */
    public static int getQuestTimestamp(long realTimestamp) {
        int time = (int) (realTimestamp / 1000 / 60); // convert to minutes
        return (int) (time * 0.1396987) + QUEST_UNIXAGE;
    }

    public static boolean isDST() {
        return TimeZone.getDefault().inDaylightTime(new Date());
    }

    /**
     * Converts a Unix Timestamp into File Time
     *
     * @param timeStampinMillis The actual timestamp in milliseconds.
     * @return A 64-bit long giving a filetime timestamp
     */
    public static long getFileTimestamp(long timeStampinMillis) {
        return getFileTimestamp(timeStampinMillis, false);
    }

    public static long getFileTimestamp(long timeStampinMillis, boolean roundToMinutes) {
        if (isDST()) {
            timeStampinMillis -= 3600000L; //60 * 60 * 1000
        }
        timeStampinMillis += 14 * 60 * 60 * 1000;
        long time;
        if (roundToMinutes) {
            time = (timeStampinMillis / 1000 / 60) * 600000000;
        } else {
            time = timeStampinMillis * 10000;
        }
        return time + FT_UT_OFFSET;
    }

    public static int getIntDate() {
        String time = new SimpleDateFormat("yyyy-MM-dd").format(new Date()).replace("-", "");
        return Long.valueOf(time).intValue();
    }

    public static int getTime() {
        String time = new SimpleDateFormat("yyyy-MM-dd-HH").format(new Date()).replace("-", "");
        return Long.valueOf(time).intValue();
    }

    public static int getTime(long realTimestamp) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyMMddHHmm");
        return Long.valueOf(sdf.format(realTimestamp)).intValue();
    }

    public static long getKoreanTimestamp(long realTimestamp) {
        return realTimestamp * 10000 + 116444592000000000L;
    }

    public static String getNowTime() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy年MM月dd日HH時mm分ss秒");
        return sdf.format(new Date());
    }

    /**
     * 獲取當前時間 HH:mm:ss.fff
     *
     * @return
     */
    public static int getSpecialNowiTime() {
        return (int) (System.currentTimeMillis() % 100000000);
    }
    /*
     * --------------------------------------------------------------------------------------------
     * 時間幫助類
     * --------------------------------------------------------------------------------------------
     */

    /**
     * 得到當前的時間，時間格式yyyy-MM-dd
     *
     * @return
     */
    public static String getCurrentDate() {
        return getCurrentDate("yyyy-MM-dd");
    }

    /**
     * 得到當前的時間,自定義時間格式
     * y 年 M 月 d 日 H 時 m 分 s 秒
     *
     * @param dateFormat 輸出顯示的時間格式
     * @return
     */
    public static String getCurrentDate(String dateFormat) {
        return getFormatDate(new Date(), dateFormat);
    }

    /**
     * 日期格式化，默認日期格式yyyy-MM-dd
     *
     * @param date
     * @return
     */
    public static String getFormatDate(Object date) {
        return getFormatDate(date, "yyyy-MM-dd");
    }

    /**
     * 日期格式化，自定義輸出日期格式
     *
     * @param date
     * @param dateFormat
     * @return
     */
    public static String getFormatDate(Object date, String dateFormat) {
        assert date instanceof Date || date instanceof Number;
        SimpleDateFormat sdf = new SimpleDateFormat(dateFormat);
        return sdf.format(date);
    }

    /**
     * 返回當前日期的前一個時間日期，amount為正數 當前時間後的時間 為負數 當前時間前的時間
     * 默認日期格式yyyy-MM-dd
     *
     * @param field  日曆字段
     *               y 年 M 月 d 日 H 時 m 分 s 秒
     * @param amount 數量
     * @return 一個日期
     */
    public static String getPreDate(String field, int amount) {
        return getPreDate(new Date(), field, amount);
    }

    /**
     * 某一個日期的前一個日期
     *
     * @param d,某一個日期
     * @param field   日曆字段
     *                y 年 M 月 d 日 H 時 m 分 s 秒
     * @param amount  數量
     * @return 一個日期
     */
    public static String getPreDate(Date d, String field, int amount) {
        String result = getPreTime(d, field, amount);
        if (result == null) {
            return null;
        }
        return result.split(" ")[0].replace("/", "-");
    }

    public static String getPreTime(String field, int amount) {
        return getPreTime(new Date(), field, amount);
    }

    /**
     * 某一個時間的前一個時間
     *
     * @param d,某一個日期
     * @param field   日曆字段
     *                y 年 M 月 d 日 H 時 m 分 s 秒
     * @param amount  數量
     * @return 一個日期
     */
    public static String getPreTime(Date d, String field, int amount) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(d);
        if (field != null && !field.equals("")) {
            switch (field) {
                case "y":
                    calendar.add(Calendar.YEAR, amount);
                    break;
                case "M":
                    calendar.add(Calendar.MONTH, amount);
                    break;
                case "d":
                    calendar.add(Calendar.DAY_OF_MONTH, amount);
                    break;
                case "H":
                    calendar.add(Calendar.HOUR, amount);
                    break;
                case "m":
                    calendar.add(Calendar.MINUTE, amount);
                    break;
                case "s":
                    calendar.add(Calendar.SECOND, amount);
                    break;
            }
        } else {
            return null;
        }
        return getFormatDate(calendar.getTime(), "yyyy/MM/dd HH:mm:ss");
    }

    /**
     * 某一個時間的前一個時間
     *
     * @param date
     * @return
     * @throws ParseException
     */
    public static String getPreDate(String date) throws ParseException {
        Date d = new SimpleDateFormat().parse(date);
        String preD = getPreDate(d, "d", 1);
        Date preDate = new SimpleDateFormat().parse(preD);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        return sdf.format(preDate);
    }

    /**
     * 將字符串時間轉換成 long
     * 注意 要轉換的時間長度必須為 12位 yyyyMMddhhmm
     *
     * @param dateString
     * @return
     */
    public static long getStringToTime(String dateString) {
        SimpleDateFormat df = new SimpleDateFormat("yyyyMMddhhmm");
        try {
            Date date = df.parse(dateString);
            return date.getTime();
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        }
        return -1;
    }

    public static long getStringToTime(String dateString, String format) {
        SimpleDateFormat df = new SimpleDateFormat(format);
        try {
            Date date = df.parse(dateString);
            return date.getTime();
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        }
        return -1;
    }

    /**
     * 獲得當前時間與第二天0點0分0秒時間
     *
     * @param day
     * @return
     */
    public static long getNextDayTime(int day) {
        Calendar date = Calendar.getInstance();
        date.set(date.get(Calendar.YEAR), date.get(Calendar.MONTH), date.get(Calendar.DATE) + day, 0, 0, 0);
        return date.getTime().getTime();
    }

    /**
     * 獲得當前時間與第二天0點0分0秒相差多少毫秒
     *
     * @param day
     * @return
     */
    public static long getNextDayDiff(int day) {
        Calendar date = Calendar.getInstance();
        date.set(date.get(Calendar.YEAR), date.get(Calendar.MONTH), date.get(Calendar.DATE) + day, 0, 0);
        return date.getTime().getTime() - System.currentTimeMillis();
    }

    public static String getDayInt(int day) {
        if (day == 1) {
            return "SUN";
        } else if (day == 2) {
            return "MON";
        } else if (day == 3) {
            return "TUE";
        } else if (day == 4) {
            return "WED";
        } else if (day == 5) {
            return "THU";
        } else if (day == 6) {
            return "FRI";
        } else if (day == 7) {
            return "SAT";
        }
        return null;
    }

    public static int getDate() {
        final String q = getTimeNow();
        return Integer.parseInt(q.substring(0, 4) + q.substring(5, 7) + q.substring(8, 10));
    }

    public static String getTimeNow() {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Calendar.getInstance().getTime());
    }

    public static long getLastTimeOfMonth() {
        Calendar date = Calendar.getInstance();
        date.add(Calendar.MONTH, 1);
        date.set(Calendar.DAY_OF_MONTH, 1);
        date.set(Calendar.HOUR_OF_DAY, 6);
        date.set(Calendar.MINUTE, 0);
        date.set(Calendar.SECOND, 0);
        date.set(Calendar.MILLISECOND, 0);
        return date.getTimeInMillis();
    }
}
