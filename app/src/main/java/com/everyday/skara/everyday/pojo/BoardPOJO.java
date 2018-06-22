package com.everyday.skara.everyday.pojo;

import java.io.Serializable;

public class BoardPOJO implements Serializable{
    String title;
    String date;
    String boardKey;
    UserInfoPOJO createdByProfilePOJO;

    public BoardPOJO(){}

    public BoardPOJO(String title, String date, String boardKey, UserInfoPOJO createdByProfilePOJO) {
        this.title = title;
        this.date = date;
        this.boardKey = boardKey;
        this.createdByProfilePOJO = createdByProfilePOJO;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getBoardKey() {
        return boardKey;
    }

    public void setBoardKey(String boardKey) {
        this.boardKey = boardKey;
    }

    public UserInfoPOJO getCreatedByProfilePOJO() {
        return createdByProfilePOJO;
    }

    public void setCreatedByProfilePOJO(UserInfoPOJO createdByProfilePOJO) {
        this.createdByProfilePOJO = createdByProfilePOJO;
    }
}
