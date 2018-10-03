package com.everyday.skara.everyday.classes;

import com.everyday.skara.everyday.pojo.ExpensePOJO;

import java.util.ArrayList;

public class DateExpenseHolder {
    String date;
    ArrayList<ExpensePOJO> expensePOJOArrayList;

    public DateExpenseHolder(){}
    public DateExpenseHolder(String date, ArrayList<ExpensePOJO> expensePOJOArrayList) {
        this.date = date;
        this.expensePOJOArrayList = expensePOJOArrayList;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public ArrayList<ExpensePOJO> getExpensePOJOArrayList() {
        return expensePOJOArrayList;
    }

    public void setExpensePOJOArrayList(ArrayList<ExpensePOJO> expensePOJOArrayList) {
        this.expensePOJOArrayList = expensePOJOArrayList;
    }
}
