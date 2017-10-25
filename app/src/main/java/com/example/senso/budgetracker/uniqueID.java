package com.example.senso.budgetracker;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * generate unique ids using timestamp
 */

public class uniqueID {


    public uniqueID(){

    }

    public int getId() {
        Date now = new Date();
        int id = Integer.parseInt(new SimpleDateFormat("ddHHmmss",  Locale.US).format(now));
        return id;
    }
}
