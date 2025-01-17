package com.everyday.skara.everyday.pojo;

import java.io.Serializable;

public class NotePOJO implements Serializable{
    String noteKey;
    String title;
    String content;
    String date;
    UserInfoPOJO userInfoPOJO;

    public NotePOJO(){}

    public NotePOJO(String noteKey, String title, String content, String date, UserInfoPOJO userInfoPOJO) {
        this.noteKey = noteKey;
        this.title = title;
        this.content = content;
        this.date = date;
        this.userInfoPOJO = userInfoPOJO;
    }

    public UserInfoPOJO getUserInfoPOJO() {
        return userInfoPOJO;
    }

    public void setUserInfoPOJO(UserInfoPOJO userInfoPOJO) {
        this.userInfoPOJO = userInfoPOJO;
    }

    public String getNoteKey() {
        return noteKey;
    }

    public void setNoteKey(String noteKey) {
        this.noteKey = noteKey;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }
}
