package com.everyday.skara.everyday.pojo;

public class FeedbackPOJO {
    UserProfilePOJO userProfilePOJO;
    String note;
    int emoji;

    public FeedbackPOJO(){}

    public FeedbackPOJO(UserProfilePOJO userProfilePOJO, String note, int emoji) {
        this.userProfilePOJO = userProfilePOJO;
        this.note = note;
        this.emoji = emoji;
    }

    public UserProfilePOJO getUserProfilePOJO() {
        return userProfilePOJO;
    }

    public void setUserProfilePOJO(UserProfilePOJO userProfilePOJO) {
        this.userProfilePOJO = userProfilePOJO;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public int getEmoji() {
        return emoji;
    }

    public void setEmoji(int emoji) {
        this.emoji = emoji;
    }
}