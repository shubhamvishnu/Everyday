package com.everyday.skara.everyday.classes;

public class NotificationHolder {
    String itemKey;
    String title;
    String message;
    String holderKey;
    String mStartDateValue, mEndDateValue;
    int mStartDay, mStartMonth, mStartYear;
    int mEndDay, mEndMonth, mEndYear;
    String mTime;
    int mHour;
    int mMinute;
    int intervalType;
    int notificationId;
    int pendingIntentRC;
    int notificationType;
    boolean isActive;

    public NotificationHolder() {
    }


    public NotificationHolder(String itemKey, String title, String message, String holderKey, String mStartDateValue, String mEndDateValue, int mStartDay, int mStartMonth, int mStartYear, int mEndDay, int mEndMonth, int mEndYear, String mTime, int mHour, int mMinute, int intervalType, int notificationType, boolean isActive) {
        this.itemKey = itemKey;
        this.title = title;
        this.message = message;
        this.holderKey = holderKey;
        this.mStartDateValue = mStartDateValue;
        this.mEndDateValue = mEndDateValue;
        this.mStartDay = mStartDay;
        this.mStartMonth = mStartMonth;
        this.mStartYear = mStartYear;
        this.mEndDay = mEndDay;
        this.mEndMonth = mEndMonth;
        this.mEndYear = mEndYear;
        this.mTime = mTime;
        this.mHour = mHour;
        this.mMinute = mMinute;
        this.intervalType = intervalType;
        this.notificationType = notificationType;
        this.isActive = isActive;
        setIds();
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

    public String getmStartDateValue() {
        return mStartDateValue;
    }

    public void setmStartDateValue(String mStartDateValue) {
        this.mStartDateValue = mStartDateValue;
    }

    public String getmEndDateValue() {
        return mEndDateValue;
    }

    public void setmEndDateValue(String mEndDateValue) {
        this.mEndDateValue = mEndDateValue;
    }

    public int getmStartDay() {
        return mStartDay;
    }

    public void setmStartDay(int mStartDay) {
        this.mStartDay = mStartDay;
    }

    public int getmStartMonth() {
        return mStartMonth;
    }

    public void setmStartMonth(int mStartMonth) {
        this.mStartMonth = mStartMonth;
    }

    public int getmStartYear() {
        return mStartYear;
    }

    public void setmStartYear(int mStartYear) {
        this.mStartYear = mStartYear;
    }

    public int getmEndDay() {
        return mEndDay;
    }

    public void setmEndDay(int mEndDay) {
        this.mEndDay = mEndDay;
    }

    public int getmEndMonth() {
        return mEndMonth;
    }

    public void setmEndMonth(int mEndMonth) {
        this.mEndMonth = mEndMonth;
    }

    public int getmEndYear() {
        return mEndYear;
    }

    public void setmEndYear(int mEndYear) {
        this.mEndYear = mEndYear;
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
