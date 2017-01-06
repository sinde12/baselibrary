package hohistar.sinde.baselibrary.utility;

import android.text.TextUtils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * Created by sinde on 15/11/11.
 */
public class Utility_Date extends Utility {

    public static final String yyyy_MM_dd_HH_mm_ss = "yyyy-MM-dd HH:mm:ss";
    public static final String yyyy_MM_dd__T_HH_mm_ss_SSSZ = "yyyy-MM-dd'T'HH:mm:ss.SSSZ";
    public static final String yyyyMMddHHmmssSSSZ = "yyyyMMddHHmmssSSSZ";
    public static final String yyyyMMddHHmmssSSS = "yyyyMMddHHmmssSSS";

    /**
     * 将字符串转换为Date类型
     *
     * @param date
     *            字符串类型
     * @param pattern
     *            格式
     * @return 日期类型
     * @throws ParseException
     */
    public static Date format(String date, String pattern) throws ParseException {
        if (pattern == null || pattern.equals("") || pattern.equals("null")) {
            pattern = "yyyy-MM-dd HH:mm:ss";
        }
        if (date == null || date.equals("") || date.equals("null")) {
            return new Date();
        }
        Date d = null;
        d = new SimpleDateFormat(pattern).parse(date);

        return d;
    }

    /**
     * 将Date类型转换为字符串
     *
     * @param date
     *            日期类型
     * @param pattern
     *            字符串格式
     * @return 日期字符串
     */
    public static String format(Date date, String pattern) {
        if (date == null) {
            return "null";
        }
        if (pattern == null || pattern.equals("") || pattern.equals("null")) {
            pattern = "yyyy-MM-dd HH:mm:ss";
        }
        return new SimpleDateFormat(pattern).format(date);
    }

    public static Date getDate(int year,int monthOfYear,int day){
        Calendar calendar = Calendar.getInstance();
        calendar.set(year,monthOfYear,day);
        return calendar.getTime();
    }


    // 将格式 yyyy-MM-dd HH:mm:ss 转为 yyyy-MM-dd
    public static String format(String date) {
        if (TextUtils.isEmpty(date)) {
            return null;
        }
        try {
            String pattern = "yyyy-MM-dd HH:mm:ss";
            String pattern1 = "yyyy-MM-dd";
            Date parse = new SimpleDateFormat(pattern).parse(date);
            return new SimpleDateFormat(pattern1).format(parse);
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String formatToMonth(String date) {
        if (TextUtils.isEmpty(date)) {
            return null;
        }
        try {
            String pattern = "yyyy-MM-dd HH:mm:ss";
            String pattern1 = "yyyy年MM月";
            Date parse = new SimpleDateFormat(pattern).parse(date);
            return new SimpleDateFormat(pattern1).format(parse);
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String formatToDay(String date) {
        if (TextUtils.isEmpty(date)) {
            return null;
        }
        try {
            String pattern = "yyyy-MM-dd HH:mm:ss";
            String pattern1 = "yyyy年MM月dd日 EEEE";
            Date parse = new SimpleDateFormat(pattern).parse(date);
            return new SimpleDateFormat(pattern1, Locale.CHINESE).format(parse);
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String formatToMinute(String date) {
        if (TextUtils.isEmpty(date)) {
            return null;
        }
        try {
            String pattern = "yyyy-MM-dd HH:mm:ss";
            String pattern1 = "dd日HH时mm分";
            Date parse = new SimpleDateFormat(pattern).parse(date);
            return new SimpleDateFormat(pattern1).format(parse);
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String formatToMinute1(String date) {
        if (TextUtils.isEmpty(date)) {
            return null;
        }
        try {
            String pattern = "yyyy-MM-dd HH:mm:ss";
            String pattern1 = "HH时mm分";
            Date parse = new SimpleDateFormat(pattern).parse(date);
            return new SimpleDateFormat(pattern1).format(parse);
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String formatToTime(String date) {
        if (TextUtils.isEmpty(date)) {
            return null;
        }
        try {
            String pattern = "yyyy-MM-dd HH:mm:ss";
            String pattern1 = "HH:mm:ss";
            Date parse = new SimpleDateFormat(pattern).parse(date);
            return new SimpleDateFormat(pattern1).format(parse);
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String nowToString(){
        return format(new Date(),"yyyyMMddHHmmss");
    }

    public static String format(long time,String pattern){
        Date date = new Date(time);
        return format(date,pattern);
    }

}
