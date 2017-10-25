package com.example.senso.budgetracker;


import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.widget.DatePicker;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;


/**
 *Date dialog
 */

public class DatePickerFragment extends DialogFragment
        implements DatePickerDialog.OnDateSetListener {



    Activity activity;
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        this.activity=activity;
    }


    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        // Use the current date as the default date in the picker
        final Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog pickerDialog = new DatePickerDialog(getActivity(), this, year, month, day);

        //get calendar type
        String type = getArguments().getString("type");
        if (type.equals("planned")) {
            //u cannot plan a past expense
            pickerDialog.getDatePicker().setMinDate(c.getTime().getTime());
        } else if (type.equals("normal")) {
            //u cannot store a future expense
            pickerDialog.getDatePicker().setMaxDate(c.getTime().getTime());
        }
        return pickerDialog;
    }

    public void onDateSet(DatePicker view, int year, int month, int day) {
        //format it properly

        String date = "" + checkDigit(view.getYear()) +"-"+ checkDigit(view.getMonth())+"-"+checkDigit(view.getDayOfMonth());
        ((AddExpense) getActivity()).date = date ;
    }

    public String checkDigit(int number)
    {
        return number<=9?"0"+number:String.valueOf(number);
    }
}

