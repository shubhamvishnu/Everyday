package com.everyday.skara.everyday.classes;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class DateTimeStamp {
    public static String getDate(){
        String date = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH).format(new Date());
        return date;
    }
}
