package com.everyday.skara.everyday.pojo;

import java.io.Serializable;

public class ActivityPOJO implements Serializable{
    String action;
    String time;
    UserProfilePOJO userProfilePOJO;

    public ActivityPOJO(){}

    public ActivityPOJO(String action, String time, UserProfilePOJO userProfilePOJO) {
        this.action = action;
        this.time = time;
        this.userProfilePOJO = userProfilePOJO;
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

    public UserProfilePOJO getUserProfilePOJO() {
        return userProfilePOJO;
    }

    public void setUserProfilePOJO(UserProfilePOJO userProfilePOJO) {
        this.userProfilePOJO = userProfilePOJO;
    }
}
