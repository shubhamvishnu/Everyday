package com.everyday.skara.everyday.pojo;

public class LifeBoardPOJO {
    String key;
    String dateUniqueId;
    String day, month, year;
    String date;
    String rating;
    boolean isYes;

    public LifeBoardPOJO(){}
    public LifeBoardPOJO(String key, String dateUniqueId, String day, String month, String year, String date, String rating, boolean isYes) {
        this.key = key;
        this.dateUniqueId = dateUniqueId;
        this.day = day;
        this.month = month;
        this.year = year;
        this.date = date;
        this.rating = rating;
        this.isYes = isYes;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getDateUniqueId() {
        return dateUniqueId;
    }

    public void setDateUniqueId(String dateUniqueId) {
        this.dateUniqueId = dateUniqueId;
    }

    public String getDay() {
        return day;
    }

    public void setDay(String day) {
        this.day = day;
    }

    public String getMonth() {
        return month;
    }

    public void setMonth(String month) {
        this.month = month;
    }

    public String getYear() {
        return year;
    }

    public void setYear(String year) {
        this.year = year;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getRating() {
        return rating;
    }

    public void setRating(String rating) {
        this.rating = rating;
    }

    public boolean isYes() {
        return isYes;
    }

    public void setYes(boolean yes) {
        isYes = yes;
    }
}
