package com.everyday.skara.everyday.pojo;

import java.io.Serializable;
import java.util.ArrayList;

public class ExpensePOJO implements Serializable{
    String entryKey;
    Double amount;
    String description;
    String date;
    int expenseType;
    ArrayList<BoardMembersPOJO> sharedByArrayList;
    String note;
    String transactionId;
    int year;
    int month;
    int day;

    public ExpensePOJO(String entryKey, Double amount, String description, String date, int expenseType, ArrayList<BoardMembersPOJO> sharedByArrayList, String note, String transactionId, int year, int month, int day) {
        this.entryKey = entryKey;
        this.amount = amount;
        this.description = description;
        this.date = date;
        this.expenseType = expenseType;
        this.sharedByArrayList = sharedByArrayList;
        this.note = note;
        this.transactionId = transactionId;
        this.year = year;
        this.month = month;
        this.day = day;
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

    public int getExpenseType() {
        return expenseType;
    }

    public void setExpenseType(int expenseType) {
        this.expenseType = expenseType;
    }

    public ArrayList<BoardMembersPOJO> getSharedByArrayList() {
        return sharedByArrayList;
    }

    public void setSharedByArrayList(ArrayList<BoardMembersPOJO> sharedByArrayList) {
        this.sharedByArrayList = sharedByArrayList;
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
}
