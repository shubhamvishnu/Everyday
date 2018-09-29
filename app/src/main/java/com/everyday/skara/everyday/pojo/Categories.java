package com.everyday.skara.everyday.pojo;

public class Categories {
    String categoryName;
    String categoryKey;

    public Categories(){

    }
    public Categories(String categoryName, String categoryKey) {
        this.categoryName = categoryName;
        this.categoryKey = categoryKey;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public String getCategoryKey() {
        return categoryKey;
    }

    public void setCategoryKey(String categoryKey) {
        this.categoryKey = categoryKey;
    }
}
