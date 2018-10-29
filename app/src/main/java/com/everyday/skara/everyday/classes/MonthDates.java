package com.everyday.skara.everyday.classes;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class MonthDates {
    int monthDatesKey; // year+month
    int year;
    int month;
    ArrayList<Date> dates;

    public MonthDates(){}

    public MonthDates(int monthDatesKey, int year, int month, ArrayList<Date> dates) {
        this.monthDatesKey = monthDatesKey;
        this.year = year;
        this.month = month;
        this.dates = dates;
    }

    public int getMonthDatesKey() {
        return monthDatesKey;
    }

    public void setMonthDatesKey(int monthDatesKey) {
        this.monthDatesKey = monthDatesKey;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public int getMonth() {
        return month;
    }

    public void setMonth(int month) {
        this.month = month;
    }

    public ArrayList<Date> getDates() {
        return dates;
    }

    public void setDates(ArrayList<Date> dates) {
        this.dates = dates;
    }
}
