package com.everyday.skara.everyday.pojo;

public class LifeBoardPOJO {
    String key;
    int dateUniqueId;
    int day, month, year;
    String date;
    String rating;
    int choice;
    UserInfoPOJO userInfoPOJO;

    public LifeBoardPOJO(){}
    public LifeBoardPOJO(String key, int dateUniqueId, int day, int month, int year, String date, String rating, int choice,    UserInfoPOJO userInfoPOJO) {
        this.key = key;
        this.dateUniqueId = dateUniqueId;
        this.day = day;
        this.month = month;
        this.year = year;
        this.date = date;
        this.rating = rating;
        this.choice = choice;
        this.userInfoPOJO = userInfoPOJO;
    }

    public UserInfoPOJO getUserInfoPOJO() {
        return userInfoPOJO;
    }

    public void setUserInfoPOJO(UserInfoPOJO userInfoPOJO) {
        this.userInfoPOJO = userInfoPOJO;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public int getDateUniqueId() {
        return dateUniqueId;
    }

    public void setDateUniqueId(int dateUniqueId) {
        this.dateUniqueId = dateUniqueId;
    }

    public int getDay() {
        return day;
    }

    public void setDay(int day) {
        this.day = day;
    }

    public int getMonth() {
        return month;
    }

    public void setMonth(int month) {
        this.month = month;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
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

    public int getChoice() {
        return choice;
    }

    public void setChoice(int choice) {
        this.choice = choice;
    }

}
