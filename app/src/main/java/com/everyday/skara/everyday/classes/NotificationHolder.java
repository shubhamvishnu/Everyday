package com.everyday.skara.everyday.classes;

public class NotificationHolder {
    String itemKey;
    String title;
    String message;
    String holderKey;
    int day;
    int month;
    int year;
    int hours;
    int minutes;
    int intervalType;
    int notificationId;
    int pendingIntentRC;
    int notificationType;
    boolean isActive;

    public NotificationHolder() {
    }


    public NotificationHolder(String itemKey, String title, String message, String holderKey, int day, int month, int year, int hours, int minutes, int intervalType, int notificationType, boolean isActive) {
        this.itemKey = itemKey;
        this.title = title;
        this.message = message;
        this.holderKey = holderKey;
        this.day = day;
        this.month = month;
        this.year = year;
        this.hours = hours;
        this.minutes = minutes;
        this.intervalType = intervalType;
        this.notificationType = notificationType;
        this.isActive = isActive;
        setIds();
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public int getHours() {
        return hours;
    }

    public void setHours(int hours) {
        this.hours = hours;
    }

    public int getMinutes() {
        return minutes;
    }

    public void setMinutes(int minutes) {
        this.minutes = minutes;
    }

    public int getDay() {
        return day;
    }

    public void setDay(int day) {
        this.day = day;
    }

    public int getMonth() {
        return month;
    }

    public void setMonth(int month) {
        this.month = month;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public int getNotificationType() {
        return notificationType;
    }

    public void setNotificationType(int notificationType) {
        this.notificationType = notificationType;
    }

    void setIds(){
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

    int getUniqueNumbericalId(){
        int id = 0;
        for(int i = 0; i < itemKey.length(); i++){
            id += (int) itemKey.charAt(i);
        }
        return id;
    }
    public static int getUniqueNumberIdFromString(String itemKey){
        int id = 0;
        for(int i = 0; i < itemKey.length(); i++){
            id += (int) itemKey.charAt(i);
        }
        return id;
    }
}
