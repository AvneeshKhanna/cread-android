package com.thetestament.cread.utils;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

public class TimeUtils {

    /**
     * Method to return list which contains time and data.
     * <p>data is stored in form of mm:dd:yyyy;hh:mm</p>
     * <p>i.e at index[0] month -mm</p>
     * <p>at index[1] date -dd</p>
     * <p>at index[2] year -yyyy</p>
     * <p>at index[3] time in hours and minute -hh:mm</p>
     *
     * @param utcTime String from which date and time to be extracted
     */
    public static List<String> getCustomTime(String utcTime) {

        //Locale time
        //Mon Jun 26 05:34:14 UTC 2017
        String localTime = getDate(utcTime);
        //UTC time
        //2017-06-26T05:34:14.000z
        List<String> list = new ArrayList();
        list.add(0, localTime.substring(4, 7));//Add month
        list.add(1, localTime.substring(8, 10));//Add date
        list.add(2, localTime.substring(24, 28));//Add year
        list.add(3, getTime(utcTime));// Add time
        return list;
    }

    /**
     * Method to convert UTC time to locale time.
     *
     * @param date UTC date i.e 2017-06-26T05:34:14.000z
     * @return locate time i.e Mon Jun 26 05:34:14 UTC 2017
     */
    private static String getDate(String date) {
        Date fDate = null;
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        simpleDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        try {
            fDate = simpleDateFormat.parse(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return fDate.toString();
    }

    /**
     * Method to return time from the data in 12 hours format.
     */
    private static String getTime(String date) {
        Date fDate = null;
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        simpleDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        try {
            fDate = simpleDateFormat.parse(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        // Get time from date
        SimpleDateFormat timeFormatter = new SimpleDateFormat("h:mm a");
        return timeFormatter.format(fDate);
    }

    /**
     * Return an ISO 8601 combined date and time string for specified date/time
     *
     * @param date
     *            Date
     * @return String with format "yyyy-MM-dd'T'HH:mm:ss'Z'"
     */
    public static String getISO8601StringForDate(Date date) {
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US);
        dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        return dateFormat.format(date);
    }



}
