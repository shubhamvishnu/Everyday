package com.everyday.skara.everyday.pojo;

import java.io.Serializable;

public class UserInfoPOJO implements Serializable{
    String name;
    String email;
    String profile_url;
    String user_key;
    int month;
    int year;
    int day;
    public UserInfoPOJO(){}

    public UserInfoPOJO(String name, String email, String profile_url, String user_key, int day, int month, int year) {
        this.name = name;
        this.email = email;
        this.profile_url = profile_url;
        this.user_key = user_key;
        this.day = day;
        this.month = month;
        this.year = year;
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

    public int getDay() {
        return day;
    }

    public void setDay(int day) {
        this.day = day;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getProfile_url() {
        return profile_url;
    }

    public void setProfile_url(String profile_url) {
        this.profile_url = profile_url;
    }

    public String getUser_key() {
        return user_key;
    }

    public void setUser_key(String user_key) {
        this.user_key = user_key;
    }
}
