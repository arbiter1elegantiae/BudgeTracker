package com.example.senso.budgetracker;

public class planPeriodicExpense extends expense {

    boolean isPlanned;

    public planPeriodicExpense(String name, String category, Float cost,String date, boolean planned) {
        super(name, category, cost, date);
        this.isPlanned = planned;
    }

    public boolean isPlanned() {
        return isPlanned;
    }
}
