package com.everyday.skara.everyday.classes;

public class TodoItem {
    public static int LEVEL_MAIN = 1;


    public final static String PARENT_KEY = "NULL";


    public static boolean NOT_CHECKED = false;
    public static boolean CHECKED = true;
    int itemLevel;
    String parentKey;

    public TodoItem(int itemLevel, String parentKey) {
        this.itemLevel = itemLevel;
        this.parentKey = parentKey;
    }

    public int getItemLevel() {
        return itemLevel;
    }

    public void setItemLevel(int itemLevel) {
        this.itemLevel = itemLevel;
    }

    public String getParentKey() {
        return parentKey;
    }

    public void setParentKey(String parentKey) {
        this.parentKey = parentKey;
    }
}
