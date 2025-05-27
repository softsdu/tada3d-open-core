package com.zlp.platform.util;

import com.nova.frame.utils.StringUtils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by bsh on 2017/4/19.
 */
public class DateUtils {

    public static String getCurrentMonth(){
        Calendar cal = Calendar.getInstance();
        if(cal.get(Calendar.MONTH)+1<10){
            return "0"+(cal.get(Calendar.MONTH)+1);
        }else{
            return ""+(cal.get(Calendar.MONTH)+1);
        }
    }

    public static String getCurrentYear(){
        Calendar cal = Calendar.getInstance();
        return cal.get(Calendar.YEAR)+"";
    }

    /**
     * format:yyyy-MM-dd HH:mm:ss
     * @return
     */
    public static String getCurrentTime(){
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return sdf.format(System.currentTimeMillis());
    }

    /**
     * 判断date1 是否 比date2 日期大
     * @param date1
     * @param date2
     * @return
     */
    public static boolean compareDate(String date1,String date2){
        boolean compare = false;
        SimpleDateFormat sdf =  new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try {
            if(StringUtils.isEmpty(date1)){
                compare = false;
                return compare;
            }
            if(StringUtils.isEmpty(date2)){
                compare = true;
                return compare;
            }
            Date d1_date = sdf.parse(date1);
            Date d2_date = sdf.parse(date2);
            compare = d1_date.getTime()>d2_date.getTime();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return compare;
    }

    public static String calculate_month = "";
    public static String calculate_year = "";

    /**
     * 计算当前月的上个月，并给出所属年份,因为有跨年的情况
     * @return
     */
    public static void lastMonth(){
        Calendar c = Calendar.getInstance();
        //过去一月
        c.setTime(new Date());
        c.add(Calendar.MONTH, -1);
        if(c.get(Calendar.MONTH)+1<10){
            calculate_month = "0"+(c.get(Calendar.MONTH)+1);
        }else{
            calculate_month = ""+(c.get(Calendar.MONTH)+1);
        }
        calculate_year = ""+c.get(Calendar.YEAR);
    }

    /**
     * 计算当年的上一年
     * @return
     */
    public static String lastYear(){
        Calendar c = Calendar.getInstance();
        //过去一年
        c.setTime(new Date());
        c.add(Calendar.YEAR, -1);
        return c.get(Calendar.YEAR)+"";
    }
}
