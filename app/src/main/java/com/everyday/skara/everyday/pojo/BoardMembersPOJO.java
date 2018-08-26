package com.everyday.skara.everyday.pojo;

import java.io.Serializable;

public class BoardMembersPOJO implements Serializable{
    String memberKey;
    String timestamp;
    UserInfoPOJO userInfoPOJO;
    int memberType;

    public BoardMembersPOJO() {
    }

    public BoardMembersPOJO(String memberKey, String timestamp, UserInfoPOJO userInfoPOJO, int memberType) {
        this.memberKey = memberKey;
        this.timestamp = timestamp;
        this.userInfoPOJO = userInfoPOJO;
        this.memberType = memberType;
    }

    public String getMemberKey() {
        return memberKey;
    }

    public void setMemberKey(String memberKey) {
        this.memberKey = memberKey;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public UserInfoPOJO getUserInfoPOJO() {
        return userInfoPOJO;
    }

    public void setUserInfoPOJO(UserInfoPOJO userInfoPOJO) {
        this.userInfoPOJO = userInfoPOJO;
    }

    public int getMemberType() {
        return memberType;
    }

    public void setMemberType(int memberType) {
        this.memberType = memberType;
    }
}
