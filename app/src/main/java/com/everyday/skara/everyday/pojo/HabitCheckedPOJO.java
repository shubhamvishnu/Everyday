package com.everyday.skara.everyday.pojo;

public class HabitCheckedPOJO {
    String dateCheckedKey;
    String date;
    boolean state;

    public HabitCheckedPOJO() {
    }

    public HabitCheckedPOJO(String dateCheckedKey, String date, boolean state) {
        this.dateCheckedKey = dateCheckedKey;
        this.date = date;
        this.state = state;
    }

    public String getDateCheckedKey() {
        return dateCheckedKey;
    }

    public void setDateCheckedKey(String dateCheckedKey) {
        this.dateCheckedKey = dateCheckedKey;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public boolean isState() {
        return state;
    }

    public void setState(boolean state) {
        this.state = state;
    }
}
