package com.everyday.skara.everyday.classes;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class TimeDateStamp {
    public static Calendar getCalendar(String pattern, String dateString) {
        Calendar calendar = Calendar.getInstance();
        try {
            Date date = new SimpleDateFormat(pattern).parse(dateString);
            calendar.setTime(date);
        } catch (ParseException e) {
        }

        return calendar;
    }
}
