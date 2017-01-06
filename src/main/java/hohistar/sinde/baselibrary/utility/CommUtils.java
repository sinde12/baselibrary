package hohistar.sinde.baselibrary.utility;

import android.os.Environment;
import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by Wind on 2014/10/23.
 */
public class CommUtils {

    public static final String FORMAT_YYYYMMddHHmmssSSS = "yyyyMMddHHmmssSSS";
    public static final String FORMAT_YYYY_MM_dd = "yyyy/MM/dd";
//    public static final String FORMAT_YYYYMMddHHmmss = "yyyyMMddHHmmss";
    public static final String FORMAT_YYYY_MM_dd_HH_mm_ss = "yyyy-MM-dd HH:mm:ss";

    public static final String FORMAT_YYYY_MM_dds = "yyyy.MM.dd";

    public static String getBeginDateOfDay(Date date)
    {
        Calendar currentDate = Calendar.getInstance();
        currentDate.setTime(date);
        currentDate.set(Calendar.HOUR_OF_DAY, 0);
        currentDate.set(Calendar.MINUTE, 0);
        currentDate.set(Calendar.SECOND, 0);
        Date time = currentDate.getTime();

        return getDateStr(time,FORMAT_YYYY_MM_dd_HH_mm_ss);
    }

    public static String getEndDateOfDay(Date date)
    {
        Calendar currentDate = Calendar.getInstance();
        currentDate.setTime(date);

        currentDate.set(Calendar.HOUR_OF_DAY, 24);
        currentDate.set(Calendar.MINUTE, 0);
        currentDate.set(Calendar.SECOND, 0);
        Date time = currentDate.getTime();
        long lTime = time.getTime()-1000;
        Date dTime = new Date(lTime);
        return getDateStr(dTime,FORMAT_YYYY_MM_dd_HH_mm_ss);
    }

    public static String getCurrentDate(String format)
    {
        SimpleDateFormat formater = new SimpleDateFormat(format);
        return formater.format(new Date());
    }

    public static String getDateStr(Date date, String format)
    {
        SimpleDateFormat formater = new SimpleDateFormat(format);
        return formater.format(date);
    }

    public static String getDateStr(Date date)
    {
        if(date == null)
        {
            return "null";
        }
        else {
            SimpleDateFormat formater = new SimpleDateFormat(FORMAT_YYYYMMddHHmmssSSS);
            return formater.format(date);
        }
    }


    public static String getCommentDateStr(Date date)
    {
        if(date == null)
        {
            return "未知时间";
        }
        else
        {
            SimpleDateFormat formater = new SimpleDateFormat("HH:mm ,yyyy年MM月dd日");
            return formater.format(date);
        }
    }

    public static final String STD_TIME_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSSZ";

    public static final String STD_TIME_NEWFORMAT="yyyy-MM-dd'T'00:00:00.000Z";

    //TODO 由于显示的问题，导致由.net生成的数据库时间字段格式不能被xUtil实体类识别。顾在此将该日期字段声明为String而非Date，并使用本方法来转换
    private static SimpleDateFormat sDbFormater = new SimpleDateFormat(STD_TIME_FORMAT);
    private static SimpleDateFormat newsDbFormater = new SimpleDateFormat(STD_TIME_NEWFORMAT);
    private static SimpleDateFormat sDbFormaterShort = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    public static Date parseDate(String dateStr)
    {
        Date d = null;
        try {
            d = sDbFormater.parse(dateStr);
        } catch (ParseException e) {

            try {
                d = sDbFormaterShort.parse(dateStr);
            } catch (ParseException e1) {
                e1.printStackTrace();
            }
        }

        if(d == null)
        {
            Calendar x = Calendar.getInstance();
            x.set(1971, Calendar.JANUARY, 1);
            d = x.getTime();
        }

        return d;
    }


    public static String toStanderdDateOnlyString(Date date)
    {
         SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd 00:00:00");
         return sdf.format(date);
    }

    public static String toGetTime(Date date){

        return  sDbFormaterShort.format(date);
    }



    public static String toStandardDateString(Date date)
    {
        return sDbFormater.format(date);
    }


    public static String toStandardDateNewString(Date date){
        return newsDbFormater.format(date);
    }

    //添加2个星期
    public static String togetBeforeDate(Date date){
        //sDbFormater
        SimpleDateFormat sDbFormaters = new SimpleDateFormat(STD_TIME_FORMAT);
        Long twoDaysAgoMili=date.getTime()+336*1000*60*60;
        Date twodaysago=new Date(twoDaysAgoMili);
        String twodaysagoString=sDbFormaters.format(twodaysago);

        return twodaysagoString;
    }

    /**
     * 添加两个星期按指定的日期输出
     * @param date
     * @return
     */
    public static String togetBeforeDateByFormat(String date) {

        SimpleDateFormat format=new SimpleDateFormat("yyyy年MM月dd日 HH:mm");
        try {
            Date dd=format.parse(date);
            long time=dd.getTime();
            time+=336*1000*60*60;
            Date timeData=new Date(time);
            return format.format(timeData);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return "";
    }

    /**
     * 将指定的日期装换为年月日小时分
     * @param date
     * @return
     */
    public static String getFormatDataByTime(String date){
        SimpleDateFormat format=new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
        SimpleDateFormat format1=new SimpleDateFormat("yyyy年MM月dd日 HH:mm");
        try {
            Date data1=format.parse(date);
             return format1.format(data1);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return  "";
    }
    public static String getFormatDataByTime2(String date){
       SimpleDateFormat format=new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
        SimpleDateFormat format1=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try {

            return  format1.format(format.parse(date));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return  "";
    }


        public static String togetBeforeMonthDate(Date date){
//            SimpleDateFormat sDbFormaters = new SimpleDateFormat(STD_TIME_FORMAT);
//            long  twoDaysAgoMili=date.getTime()- 1;
//            Date twodaysago=new Date(twoDaysAgoMili);
//            String lastmonth=sDbFormaters.format(twodaysago);

//            int  lastmonth=0;
//            Calendar cal = Calendar.getInstance();
//               //取得系统当前时间所在月第一天时间对象
//            cal.set(Calendar.DAY_OF_MONTH, 1);
//           //日期减一,取得上月最后一天时间对象
//            cal.add(Calendar.DAY_OF_MONTH, -1);
//            lastmonth=cal.get(Calendar.DAY_OF_MONTH);


            Calendar c = Calendar.getInstance();
            c.add(Calendar.MONTH, -1);
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            String preMonth = dateFormat.format(c.getTime());


            String lastmonth =preMonth.toString();
            return  lastmonth;
        }

    public static String toStandardShortDateString(Date date)
    {
        return sDbFormaterShort.format(date);
    }

    public static String makeDurationText(String dbFromStr, String dbToStr)
    {
        Date from = parseDate(dbFromStr);
        Date to = parseDate(dbToStr);
        return makeDurationText(from, to);
    }

    public static String makeDurationText(Date from, Date to)
    {
        String fromStr = "未知";
        String toStr = "未知";

        if(from!=null) {
            fromStr = CommUtils.getDateStr(from, CommUtils.FORMAT_YYYY_MM_dds);
        }
        if(to!=null) {
            toStr = CommUtils.getDateStr(to, CommUtils.FORMAT_YYYY_MM_dds);
        }

        return String.format("%s ~ %s", fromStr, toStr);
    }

    public static boolean hasExternalStorage()
    {
        String status = Environment.getExternalStorageState();
        return status.equals(Environment.MEDIA_MOUNTED);
    }

    public static String getFileName(String absoluteFilePath)
    {
        File f = new File(absoluteFilePath);
        return f.getName();
    }

   //绑定ImageLoader绑定的路径
    public static String getImageloderFile(String url){

       String dataUrl="file://"+url;

        return dataUrl;
    }

}
