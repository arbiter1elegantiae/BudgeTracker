package com.example.senso.budgetracker;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Handling on budget operations
 */

public class BudgetHelper {

    private double Budget;
    private SharedPreferences sharedPref;



    public BudgetHelper(Context context) {

        this.sharedPref = context.getSharedPreferences("FirstTime", Context.MODE_PRIVATE);
        Budget = this.getDouble(this.sharedPref, "currentBudget", 0);
    }



    public double getBudget() {
        return Budget;
    }


    public void setBudget(double budget) {

        SharedPreferences.Editor editor = sharedPref.edit();
        putDouble(editor, "currentBudget", (getBudget() - budget));
        editor.apply();
    }


    public void setBackBudget(double budget) {

        SharedPreferences.Editor editor = sharedPref.edit();
        putDouble(editor, "currentBudget", (getBudget() + budget));
        editor.apply();
    }

    public void resetCurrentBudget(double newBudget) {

        SharedPreferences.Editor editor = sharedPref.edit();
        putDouble(editor, "currentBudget", newBudget);
        editor.apply();
    }



    //create putDouble and getDouble methods for sharedpref
    SharedPreferences.Editor putDouble(final SharedPreferences.Editor edit, final String key, final double value) {
        return edit.putLong(key, Double.doubleToRawLongBits(value));
    }


    double getDouble(final SharedPreferences prefs, final String key, final double defaultValue) {
        if ( !prefs.contains(key))
            return defaultValue;

        return Double.longBitsToDouble(prefs.getLong(key, 0));
    }

}
