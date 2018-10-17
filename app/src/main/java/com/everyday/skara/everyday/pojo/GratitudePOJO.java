package com.everyday.skara.everyday.pojo;

public class GratitudePOJO {
    String entryKey;
    String entry1;
    String entry2;
    String entry3;
    int mood;
    String note;
    String date;
    UserInfoPOJO userInfoPOJO;

    public GratitudePOJO(){}
    public GratitudePOJO(String entryKey, String entry1, String entry2, String entry3, int mood, String note, String date, UserInfoPOJO userInfoPOJO) {
        this.entryKey = entryKey;
        this.entry1 = entry1;
        this.entry2 = entry2;
        this.entry3 = entry3;
        this.mood = mood;
        this.note = note;
        this.date = date;
        this.userInfoPOJO = userInfoPOJO;
    }

    public String getEntryKey() {
        return entryKey;
    }

    public void setEntryKey(String entryKey) {
        this.entryKey = entryKey;
    }

    public String getEntry1() {
        return entry1;
    }

    public void setEntry1(String entry1) {
        this.entry1 = entry1;
    }

    public String getEntry2() {
        return entry2;
    }

    public void setEntry2(String entry2) {
        this.entry2 = entry2;
    }

    public String getEntry3() {
        return entry3;
    }

    public void setEntry3(String entry3) {
        this.entry3 = entry3;
    }

    public int getMood() {
        return mood;
    }

    public void setMood(int mood) {
        this.mood = mood;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public UserInfoPOJO getUserInfoPOJO() {
        return userInfoPOJO;
    }

    public void setUserInfoPOJO(UserInfoPOJO userInfoPOJO) {
        this.userInfoPOJO = userInfoPOJO;
    }
}
