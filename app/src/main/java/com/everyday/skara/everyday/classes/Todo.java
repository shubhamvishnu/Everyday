package com.everyday.skara.everyday.classes;

import com.everyday.skara.everyday.pojo.TodoInfoPOJO;
import com.everyday.skara.everyday.pojo.TodoPOJO;

import java.util.ArrayList;

public class Todo {
    String todoKey;
    String date;
    TodoInfoPOJO todoInfoPOJO;
    ArrayList<TodoPOJO> todoPOJOArrayList;

    public Todo(){}

    public Todo(String todoKey, String date, TodoInfoPOJO todoInfoPOJO, ArrayList<TodoPOJO> todoPOJOArrayList) {
        this.todoKey = todoKey;
        this.date = date;
        this.todoInfoPOJO = todoInfoPOJO;
        this.todoPOJOArrayList = todoPOJOArrayList;
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

    public TodoInfoPOJO getTodoInfoPOJO() {
        return todoInfoPOJO;
    }

    public void setTodoInfoPOJO(TodoInfoPOJO todoInfoPOJO) {
        this.todoInfoPOJO = todoInfoPOJO;
    }

    public ArrayList<TodoPOJO> getTodoPOJOArrayList() {
        return todoPOJOArrayList;
    }

    public void setTodoPOJOArrayList(ArrayList<TodoPOJO> todoPOJOArrayList) {
        this.todoPOJOArrayList = todoPOJOArrayList;
    }
}
