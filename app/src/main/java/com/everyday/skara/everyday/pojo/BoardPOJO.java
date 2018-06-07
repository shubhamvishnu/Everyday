package com.everyday.skara.everyday.pojo;

public class BoardPOJO {
    String title;
    String date;
    String boardKey;
    UserProfilePOJO createdByProfilePOJO;

    public BoardPOJO(){}

    public BoardPOJO(String title, String date, String boardKey, UserProfilePOJO createdByProfilePOJO) {
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

    public UserProfilePOJO getCreatedByProfilePOJO() {
        return createdByProfilePOJO;
    }

    public void setCreatedByProfilePOJO(UserProfilePOJO createdByProfilePOJO) {
        this.createdByProfilePOJO = createdByProfilePOJO;
    }
}
