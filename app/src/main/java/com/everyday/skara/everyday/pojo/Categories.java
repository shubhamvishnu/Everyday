package com.everyday.skara.everyday.pojo;

public class Categories {
    String categoryName;
    String categoryKey;
    int categoryIconId;
    int colorId;
    double limitAmount;

    public Categories(){

    }
    public Categories(String categoryName, String categoryKey, int categoryIconId, int colorId, double limitAmount) {
        this.categoryName = categoryName;
        this.categoryKey = categoryKey;
        this.categoryIconId = categoryIconId;
        this.colorId = colorId;
        this.limitAmount = limitAmount;
    }

    public int getCategoryIconId() {
        return categoryIconId;
    }

    public void setCategoryIconId(int categoryIconId) {
        this.categoryIconId = categoryIconId;
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

    public int getColorId() {
        return colorId;
    }

    public void setColorId(int colorId) {
        this.colorId = colorId;
    }
}
