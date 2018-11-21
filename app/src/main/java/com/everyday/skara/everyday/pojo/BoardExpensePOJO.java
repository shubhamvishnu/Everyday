package com.everyday.skara.everyday.pojo;

import java.io.Serializable;
import java.lang.reflect.Member;
import java.util.ArrayList;

public class BoardExpensePOJO implements Serializable {
    String entryKey;
    Double amount;
    String description;
    String date;
    String note;
    String transactionId;
    int year;
    int month;
    int day;
    Categories categories;
    UserInfoPOJO userInfoPOJO;
    ArrayList<ExpenseMembersInfoPOJO> memberInfoPojoList;
    int splitType; // 1000 - personal; 1001 - Everyone; 1002 - specific
    int splitCount;

    public BoardExpensePOJO(String entryKey, Double amount, String description, String date, String note, String transactionId, int year, int month, int day, Categories categories, UserInfoPOJO userInfoPOJO, ArrayList<ExpenseMembersInfoPOJO> memberInfoPojoList, int splitType) {
        this.entryKey = entryKey;
        this.amount = amount;
        this.description = description;
        this.date = date;
        this.note = note;
        this.transactionId = transactionId;
        this.year = year;
        this.month = month;
        this.day = day;
        this.categories = categories;
        this.userInfoPOJO = userInfoPOJO;
        this.memberInfoPojoList = memberInfoPojoList;
        this.splitType = splitType;
        initSplitCount();
    }

    void initSplitCount() {
        if (this.splitType == 1002) {
            this.splitCount = this.memberInfoPojoList.size();
        } else {
            this.splitCount = 0;
        }
    }

    public String getEntryKey() {
        return entryKey;
    }

    public void setEntryKey(String entryKey) {
        this.entryKey = entryKey;
    }

    public Double getAmount() {
        return amount;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public int getMonth() {
        return month;
    }

    public void setMonth(int month) {
        this.month = month;
    }

    public int getDay() {
        return day;
    }

    public void setDay(int day) {
        this.day = day;
    }

    public Categories getCategories() {
        return categories;
    }

    public void setCategories(Categories categories) {
        this.categories = categories;
    }

    public UserInfoPOJO getUserInfoPOJO() {
        return userInfoPOJO;
    }

    public void setUserInfoPOJO(UserInfoPOJO userInfoPOJO) {
        this.userInfoPOJO = userInfoPOJO;
    }

    public ArrayList<ExpenseMembersInfoPOJO> getMemberInfoPojoList() {
        return memberInfoPojoList;
    }

    public void setMemberInfoPojoList(ArrayList<ExpenseMembersInfoPOJO> memberInfoPojoList) {
        this.memberInfoPojoList = memberInfoPojoList;
    }

    public int getSplitType() {
        return splitType;
    }

    public void setSplitType(int splitType) {
        this.splitType = splitType;
    }

    public int getSplitCount() {
        return splitCount;
    }

    public void setSplitCount(int splitCount) {
        this.splitCount = splitCount;
    }
}
