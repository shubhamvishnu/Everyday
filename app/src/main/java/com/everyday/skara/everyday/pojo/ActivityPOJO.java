package com.everyday.skara.everyday.pojo;

import java.io.Serializable;

public class ActivityPOJO implements Serializable{
    String action;
    String time;
    UserInfoPOJO userInfoPOJO;

    public ActivityPOJO(){}

    public ActivityPOJO(String action, String time, UserInfoPOJO userInfoPOJO) {
        this.action = action;
        this.time = time;
        this.userInfoPOJO = userInfoPOJO;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public UserInfoPOJO getUserInfoPOJO() {
        return userInfoPOJO;
    }

    public void setUserInfoPOJO(UserInfoPOJO userInfoPOJO) {
        this.userInfoPOJO = userInfoPOJO;
    }
}
