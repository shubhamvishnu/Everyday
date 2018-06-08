package com.everyday.skara.everyday.pojo;

public class TodoPOJO {
    String item;
    String itemKey;
    boolean state;
    String date;
    UserInfoPOJO userInfoPOJO;

    public TodoPOJO(){}

    public TodoPOJO(String item, String itemKey, boolean state, String date, UserInfoPOJO userInfoPOJO) {
        this.item = item;
        this.itemKey = itemKey;
        this.state = state;
        this.date = date;
        this.userInfoPOJO = userInfoPOJO;
    }

    public String getItem() {
        return item;
    }

    public void setItem(String item) {
        this.item = item;
    }

    public String getItemKey() {
        return itemKey;
    }

    public void setItemKey(String itemKey) {
        this.itemKey = itemKey;
    }

    public boolean isState() {
        return state;
    }

    public void setState(boolean state) {
        this.state = state;
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
