package com.everyday.skara.everyday.pojo;

import java.io.Serializable;

public class ActivityPOJO implements Serializable{
    String action;
    String time;
    int actionType;
    UserInfoPOJO userInfoPOJO;

    public ActivityPOJO(){}

    public ActivityPOJO(String action, String time, int actionType, UserInfoPOJO userInfoPOJO) {
        this.action = action;
        this.time = time;
        this.actionType = actionType;
        this.userInfoPOJO = userInfoPOJO;
    }

    public int getActionType() {
        return actionType;
    }

    public void setActionType(int actionType) {
        this.actionType = actionType;
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

    @Override
    public String toString() {
        return "ActivityPOJO{" +
                "action='" + action + '\'' +
                ", time='" + time + '\'' +
                ", userInfoPOJO=" + userInfoPOJO +
                '}';
    }
}
