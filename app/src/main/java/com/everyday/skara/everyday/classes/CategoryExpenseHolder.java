package com.everyday.skara.everyday.classes;

import com.everyday.skara.everyday.pojo.ExpensePOJO;

import java.util.ArrayList;
import java.util.HashMap;

public class CategoryExpenseHolder {
    Double total;
    HashMap<Integer, HashMap<Integer, ArrayList<ExpensePOJO>>> yearMonthHashMap;

    public CategoryExpenseHolder(){}
    public CategoryExpenseHolder(Double total, HashMap<Integer, HashMap<Integer, ArrayList<ExpensePOJO>>> yearMonthHashMap) {
        this.total = total;
        this.yearMonthHashMap = yearMonthHashMap;
    }


    public Double getTotal() {
        return total;
    }

    public void setTotal(Double total) {
        this.total = total;
    }

    public HashMap<Integer, HashMap<Integer, ArrayList<ExpensePOJO>>> getYearMonthHashMap() {
        return yearMonthHashMap;
    }

    public void setYearMonthHashMap(HashMap<Integer, HashMap<Integer, ArrayList<ExpensePOJO>>> yearMonthHashMap) {
        this.yearMonthHashMap = yearMonthHashMap;
    }
}
