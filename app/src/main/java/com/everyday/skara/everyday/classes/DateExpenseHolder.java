package com.everyday.skara.everyday.classes;

import com.everyday.skara.everyday.pojo.FinanceEntryPOJO;

import java.util.ArrayList;

public class DateExpenseHolder {
    String date;
    ArrayList<FinanceEntryPOJO> expensePOJOArrayList;

    public DateExpenseHolder(){}
    public DateExpenseHolder(String date, ArrayList<FinanceEntryPOJO> expensePOJOArrayList) {
        this.date = date;
        this.expensePOJOArrayList = expensePOJOArrayList;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public ArrayList<FinanceEntryPOJO> getExpensePOJOArrayList() {
        return expensePOJOArrayList;
    }

    public void setExpensePOJOArrayList(ArrayList<FinanceEntryPOJO> expensePOJOArrayList) {
        this.expensePOJOArrayList = expensePOJOArrayList;
    }
}
