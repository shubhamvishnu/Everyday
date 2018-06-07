package com.everyday.skara.everyday.pojo;

import java.io.Serializable;

public class UserProfilePOJO implements Serializable{
    String name;
    String email;
    String profile_url;
    String user_key;
    String login_type;
    int user_account_type;

    public UserProfilePOJO() {
    }

    public UserProfilePOJO(String name, String email, String profile_url, String user_key, String login_type, int user_account_type) {
        this.name = name;
        this.email = email;
        this.profile_url = profile_url;
        this.user_key = user_key;
        this.login_type = login_type;
        this.user_account_type = user_account_type;
    }

    public int getUser_account_type() {
        return user_account_type;
    }

    public void setUser_account_type(int user_account_type) {
        this.user_account_type = user_account_type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getProfile_url() {
        return profile_url;
    }

    public void setProfile_url(String profile_url) {
        this.profile_url = profile_url;
    }

    public String getUser_key() {
        return user_key;
    }

    public void setUser_key(String user_key) {
        this.user_key = user_key;
    }

    public String getLogin_type() {
        return login_type;
    }

    public void setLogin_type(String login_type) {
        this.login_type = login_type;
    }
}