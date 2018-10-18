package com.everyday.skara.everyday.pojo;

public class HabitPOJO {
    String habitEntryKey;
    String title;
    String description;
    String startDate;
    String endDate;
    boolean isForever;
    int mStartDay, mStartMonth, mStartYear;
    int mEndDay, mEndMonth, mEndYear;
    int intervalType;
    String date;
    UserInfoPOJO userInfoPOJO;

    public HabitPOJO() {
    }

    public HabitPOJO(String habitEntryKey, String title, String description, String startDate, String endDate, boolean isForever, int mStartDay, int mStartMonth, int mStartYear, int mEndDay, int mEndMonth, int mEndYear, int intervalType, String date, UserInfoPOJO userInfoPOJO) {
        this.habitEntryKey = habitEntryKey;
        this.title = title;
        this.description = description;
        this.startDate = startDate;
        this.endDate = endDate;
        this.isForever = isForever;
        this.mStartDay = mStartDay;
        this.mStartMonth = mStartMonth;
        this.mStartYear = mStartYear;
        this.mEndDay = mEndDay;
        this.mEndMonth = mEndMonth;
        this.mEndYear = mEndYear;
        this.intervalType = intervalType;
        this.date = date;
        this.userInfoPOJO = userInfoPOJO;
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

    public String getStartDate() {
        return startDate;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    public String getEndDate() {
        return endDate;
    }

    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }

    public boolean isForever() {
        return isForever;
    }

    public void setForever(boolean forever) {
        isForever = forever;
    }

    public int getmStartDay() {
        return mStartDay;
    }

    public void setmStartDay(int mStartDay) {
        this.mStartDay = mStartDay;
    }

    public int getmStartMonth() {
        return mStartMonth;
    }

    public void setmStartMonth(int mStartMonth) {
        this.mStartMonth = mStartMonth;
    }

    public int getmStartYear() {
        return mStartYear;
    }

    public void setmStartYear(int mStartYear) {
        this.mStartYear = mStartYear;
    }

    public int getmEndDay() {
        return mEndDay;
    }

    public void setmEndDay(int mEndDay) {
        this.mEndDay = mEndDay;
    }

    public int getmEndMonth() {
        return mEndMonth;
    }

    public void setmEndMonth(int mEndMonth) {
        this.mEndMonth = mEndMonth;
    }

    public int getmEndYear() {
        return mEndYear;
    }

    public void setmEndYear(int mEndYear) {
        this.mEndYear = mEndYear;
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
