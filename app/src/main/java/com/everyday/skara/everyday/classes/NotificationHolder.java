package com.everyday.skara.everyday.classes;

public class NotificationHolder {
    String itemKey;
    String title;
    String message;
    String holderKey;
    String mDate;
    String mTime;
    int mDay, mMonth, mYear;
    int mHour;
    int mMinute;
    int intervalType;
    int notificationId;
    int pendingIntentRC;
    int notificationType;
    boolean isActive;

    public NotificationHolder() {
    }

    public NotificationHolder(String itemKey, String title, String message, String holderKey, String mDate, String mTime, int mDay, int mMonth, int mYear, int mHour, int mMinute, int intervalType, int notificationType, boolean isActive) {
        this.itemKey = itemKey;
        this.title = title;
        this.message = message;
        this.holderKey = holderKey;
        this.mDate = mDate;
        this.mTime = mTime;
        this.mDay = mDay;
        this.mMonth = mMonth;
        this.mYear = mYear;
        this.mHour = mHour;
        this.mMinute = mMinute;
        this.intervalType = intervalType;
        this.notificationType = notificationType;
        this.isActive = isActive;
        setIds();
    }

    public String getmDate() {
        return mDate;
    }

    public void setmDate(String mDate) {
        this.mDate = mDate;
    }

    public int getmDay() {
        return mDay;
    }

    public void setmDay(int mDay) {
        this.mDay = mDay;
    }

    public int getmMonth() {
        return mMonth;
    }

    public void setmMonth(int mMonth) {
        this.mMonth = mMonth;
    }

    public int getmYear() {
        return mYear;
    }

    public void setmYear(int mYear) {
        this.mYear = mYear;
    }

    public int getmHour() {
        return mHour;
    }

    public void setmHour(int mHour) {
        this.mHour = mHour;
    }

    public int getmMinute() {
        return mMinute;
    }

    public void setmMinute(int mMinute) {
        this.mMinute = mMinute;
    }

    public String getmTime() {
        return mTime;
    }

    public void setmTime(String mTime) {
        this.mTime = mTime;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }


    public int getNotificationType() {
        return notificationType;
    }

    public void setNotificationType(int notificationType) {
        this.notificationType = notificationType;
    }

    void setIds() {
        int uniqueId = getUniqueNumbericalId();
        this.notificationId = uniqueId;
        this.pendingIntentRC = uniqueId;

    }

    public String getItemKey() {
        return itemKey;
    }

    public void setItemKey(String itemKey) {
        this.itemKey = itemKey;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getHolderKey() {
        return holderKey;
    }

    public void setHolderKey(String holderKey) {
        this.holderKey = holderKey;
    }

    public int getIntervalType() {
        return intervalType;
    }

    public void setIntervalType(int intervalType) {
        this.intervalType = intervalType;
    }

    public int getNotificationId() {
        return notificationId;
    }

    public void setNotificationId(int notificationId) {
        this.notificationId = notificationId;
    }

    public int getPendingIntentRC() {
        return pendingIntentRC;
    }

    public void setPendingIntentRC(int pendingIntentRC) {
        this.pendingIntentRC = pendingIntentRC;
    }

    int getUniqueNumbericalId() {
        int id = 0;
        for (int i = 0; i < itemKey.length(); i++) {
            id += (int) itemKey.charAt(i);
        }
        return id;
    }

    public static int getUniqueNumberIdFromString(String itemKey) {
        int id = 0;
        for (int i = 0; i < itemKey.length(); i++) {
            id += (int) itemKey.charAt(i);
        }
        return id;
    }
}
