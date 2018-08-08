package com.everyday.skara.everyday.pojo;

import java.util.ArrayList;

public class ChatPOJO {
    String messageKey;
    int messageType;
    UserInfoPOJO senderInfo;
    String messageText;
    String timestamp;
    ArrayList<UserInfoPOJO> userInfoPOJOArrayList;

    public ChatPOJO(){}

    public ChatPOJO(String messageKey, int messageType, UserInfoPOJO senderInfo, String messageText, String timestamp, ArrayList<UserInfoPOJO> userInfoPOJOArrayList) {
        this.messageKey = messageKey;
        this.messageType = messageType;
        this.senderInfo = senderInfo;
        this.messageText = messageText;
        this.timestamp = timestamp;
        this.userInfoPOJOArrayList = userInfoPOJOArrayList;
    }

    public String getMessageKey() {
        return messageKey;
    }

    public void setMessageKey(String messageKey) {
        this.messageKey = messageKey;
    }

    public int getMessageType() {
        return messageType;
    }

    public void setMessageType(int messageType) {
        this.messageType = messageType;
    }

    public UserInfoPOJO getSenderInfo() {
        return senderInfo;
    }

    public void setSenderInfo(UserInfoPOJO senderInfo) {
        this.senderInfo = senderInfo;
    }

    public String getMessageText() {
        return messageText;
    }

    public void setMessageText(String messageText) {
        this.messageText = messageText;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public ArrayList<UserInfoPOJO> getUserInfoPOJOArrayList() {
        return userInfoPOJOArrayList;
    }

    public void setUserInfoPOJOArrayList(ArrayList<UserInfoPOJO> userInfoPOJOArrayList) {
        this.userInfoPOJOArrayList = userInfoPOJOArrayList;
    }
}
