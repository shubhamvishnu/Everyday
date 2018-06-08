package com.everyday.skara.everyday.pojo;

import java.io.Serializable;

public class LinkPOJO implements Serializable{
    String link;
    String title;
    String date;
    String linkKey;
    UserInfoPOJO userInfoPOJO;


    public LinkPOJO(){}

    public LinkPOJO(String link, String title, String date, String linkKey, UserInfoPOJO userInfoPOJO) {
        this.link = link;
        this.title = title;
        this.date = date;
        this.linkKey = linkKey;
        this.userInfoPOJO = userInfoPOJO;
    }

    public UserInfoPOJO getUserInfoPOJO() {
        return userInfoPOJO;
    }

    public void setUserInfoPOJO(UserInfoPOJO userInfoPOJO) {
        this.userInfoPOJO = userInfoPOJO;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
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

    public String getLinkKey() {
        return linkKey;
    }

    public void setLinkKey(String linkKey) {
        this.linkKey = linkKey;
    }
}
