package com.group16.eventplaza.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DateUtil {
    /**
     * timestamp to date
     *
     * @param seconds seconds
     * @param pattern pattern
     * @return result
     */
    public static String getDateToString(long seconds, String pattern) {
        Date date = new Date(seconds * 1000);
        SimpleDateFormat format = new SimpleDateFormat(pattern);
        return format.format(date);
    }

    /**
     * timestamp to date
     *
     * @param seconds seconds
     * @return result
     */
    public static String getDateToString(long seconds) {
        Date date = new Date(seconds * 1000);
        String pattern = "yyyy-MM-dd HH:mm:ss";
        SimpleDateFormat format = new SimpleDateFormat(pattern);
        return format.format(date);
    }

    /**
     * date to timestamp
     *
     * @param dateString dateString
     * @param pattern pattern
     * @return result
     */
    public static long getStringToDate(String dateString, String pattern) {
        SimpleDateFormat dateFormat = new SimpleDateFormat(pattern);
        Date date = new Date();
        try{
            date = dateFormat.parse(dateString);
        } catch(ParseException e) {
            e.printStackTrace();
        }
        return date.getTime();
    }


    public static Date convertToDate(String dateString,String pattern) throws ParseException {
        SimpleDateFormat databaseDate = new SimpleDateFormat(pattern);
        return databaseDate.parse(dateString);
    }

    public static  String convertToString(Date date,String pattern){
        SimpleDateFormat dateFormat = new SimpleDateFormat(pattern);
        return dateFormat.format(date);
    }
}
