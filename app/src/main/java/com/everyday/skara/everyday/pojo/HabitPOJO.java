package com.everyday.skara.everyday.pojo;

public class HabitPOJO {
    String habitEntryKey;
    String title;
    String description;
    String mDate;
    String mTime;
    int mDay, mMonth, mYear;
    int intervalType;
    String date;
    UserInfoPOJO userInfoPOJO;

    public HabitPOJO() {
    }

    public HabitPOJO(String habitEntryKey, String title, String description, String mDate, String mTime, int mDay, int mMonth, int mYear, int intervalType, String date, UserInfoPOJO userInfoPOJO) {
        this.habitEntryKey = habitEntryKey;
        this.title = title;
        this.description = description;
        this.mDate = mDate;
        this.mTime = mTime;
        this.mDay = mDay;
        this.mMonth = mMonth;
        this.mYear = mYear;
        this.intervalType = intervalType;
        this.date = date;
        this.userInfoPOJO = userInfoPOJO;
    }

    public String getmDate() {
        return mDate;
    }

    public void setmDate(String mDate) {
        this.mDate = mDate;
    }

    public String getmTime() {
        return mTime;
    }

    public void setmTime(String mTime) {
        this.mTime = mTime;
    }

    public int getmDay() {
        return mDay;
    }

    public void setmDay(int mDay) {
        this.mDay = mDay;
    }

    public int getmMonth() {
        return mMonth;
    }

    public void setmMonth(int mMonth) {
        this.mMonth = mMonth;
    }

    public int getmYear() {
        return mYear;
    }

    public void setmYear(int mYear) {
        this.mYear = mYear;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getHabitEntryKey() {
        return habitEntryKey;
    }

    public void setHabitEntryKey(String habitEntryKey) {
        this.habitEntryKey = habitEntryKey;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getIntervalType() {
        return intervalType;
    }

    public void setIntervalType(int intervalType) {
        this.intervalType = intervalType;
    }

    public UserInfoPOJO getUserInfoPOJO() {
        return userInfoPOJO;
    }

    public void setUserInfoPOJO(UserInfoPOJO userInfoPOJO) {
        this.userInfoPOJO = userInfoPOJO;
    }
}
