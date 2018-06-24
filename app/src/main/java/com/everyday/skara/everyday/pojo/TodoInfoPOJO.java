package com.everyday.skara.everyday.pojo;

public class TodoInfoPOJO {
    String title;
    String date;
    String todoKey;
    String lastModified;
    public TodoInfoPOJO(){}
    public TodoInfoPOJO(String title, String date, String todoKey, String lastModified) {
        this.title = title;
        this.date = date;
        this.todoKey = todoKey;
        this.lastModified = lastModified;
    }

    public String getLastModified() {
        return lastModified;
    }

    public void setLastModified(String lastModified) {
        this.lastModified = lastModified;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getTodoKey() {
        return todoKey;
    }

    public void setTodoKey(String todoKey) {
        this.todoKey = todoKey;
    }
}
