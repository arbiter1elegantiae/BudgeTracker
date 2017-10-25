package com.example.senso.budgetracker;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.support.annotation.IntDef;
import android.util.Log;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;


public class AlarmPush extends Service {

    private int BUDGET_WEEKLY_ID = 1;
    private int BUDGET_MONTHLY_ID = 2;


    //it is a basic service
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        // pushing alarms from periodic_table and planned_table

        DBHelper dbh = new DBHelper(AlarmPush.this);

        //get unscheduled planned list and unscheduled periodic expense
        ArrayList<expense> plannedEList = dbh.retrievePlanned();
        ArrayList<expense> periodicEList = dbh.retrievePeriodic();

        //id generator
        uniqueID ui = new uniqueID();

        for (int j = 0; j < plannedEList.size(); j++) {
            // create alarm notification one day before the date of the expense foreach plannedexpense retrived

            String expString = (plannedEList.get(j).toString());
            String[] dateofExpe = plannedEList.get(j).getDate().split("-");

            int hour = 12;
            int day = Integer.parseInt(dateofExpe[2]);
            int month = Integer.parseInt(dateofExpe[1]);
            int year = Integer.parseInt(dateofExpe[0]);

            Calendar calendar = Calendar.getInstance();
            calendar.set(year, month , day - 1, hour, 0);



            int UNIQUE_PENDINGINTENT_ID = ui.getId();
            Intent i = new Intent(getApplicationContext(), AlarmHandler.class);
            i.putExtra("msg", expString);
            i.putExtra("id", UNIQUE_PENDINGINTENT_ID);
            i.putExtra("type", "planned");
            PendingIntent pi = PendingIntent.getBroadcast(getApplicationContext(), UNIQUE_PENDINGINTENT_ID, i, PendingIntent.FLAG_UPDATE_CURRENT);

            AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pi);
        }


        for (int j = 0; j < periodicEList.size(); j++) {
            // create alarm notification one day before the date of the expense foreach periodicexpense retrived

            String expString = (periodicEList.get(j).toString());
            String dateofExpe = periodicEList.get(j).getDate();

            //find out if is a months expense or weekly
            if (dateofExpe.matches("[0-9]+")) {

                //monthly one, set properly first next month notification

                int dayOfM = Integer.parseInt(dateofExpe);     //todo add handling for dangerous dates like 29 30 31
                Calendar calendar = Calendar.getInstance();

                if (calendar.get(Calendar.DAY_OF_MONTH) >= dayOfM) {

                    //the day for this month is been already fired
                    calendar.set(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH) + 1, dayOfM -1 , 12, 0);

                } else {

                    calendar.set(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), dayOfM -1 , 12, 0);
                }

                int UNIQUE_PENDINGINTENT_ID = ui.getId();
                Intent i = new Intent(getApplicationContext(), AlarmHandler.class);
                i.putExtra("msg", expString);
                i.putExtra("id", UNIQUE_PENDINGINTENT_ID);
                i.putExtra("type", "periodic");
                i.putExtra("period", "month");
                i.putExtra("day", dayOfM);

                PendingIntent pi = PendingIntent.getBroadcast(getApplicationContext(), UNIQUE_PENDINGINTENT_ID, i, PendingIntent.FLAG_UPDATE_CURRENT);
                AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pi);

            } else {
                //weekly expense

                Calendar cal = Calendar.getInstance();
                //sett calendar at the next week and create new alarm ( have to do the same approach as before: check if already launched or not)
                cal.add(Calendar.DATE, 8);

                int UNIQUE_PENDINGINTENT_ID = ui.getId();
                Intent i = new Intent(getApplicationContext(), AlarmHandler.class);
                i.putExtra("msg", expString);
                i.putExtra("id", UNIQUE_PENDINGINTENT_ID);
                i.putExtra("type", "periodic");
                i.putExtra("period", "weekly");

                PendingIntent pi = PendingIntent.getBroadcast(getApplicationContext(), UNIQUE_PENDINGINTENT_ID, i, PendingIntent.FLAG_UPDATE_CURRENT);
                AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), pi);
            }
        }


        //create budget periodic alarm
        SharedPreferences sp = this.getSharedPreferences("FirstTime", Context.MODE_PRIVATE);
        if (sp.getString("period", null).equals("Settimanale")) {

            //check that today it is not monday because this is the day we want to handle the alarm not to set it
            Calendar cal = Calendar.getInstance();
            cal.setTimeInMillis(System.currentTimeMillis());


            if(cal.get(Calendar.DAY_OF_WEEK) != Calendar.MONDAY) {

                int days = 9 - cal.get(Calendar.DAY_OF_WEEK);
                cal.add(Calendar.DATE, days);

                Intent i = new Intent(getApplicationContext(), AlarmHandler.class);

                int UNIQUE_PENDINGINTENT_ID = BUDGET_WEEKLY_ID;
                i.putExtra("id", UNIQUE_PENDINGINTENT_ID);
                i.putExtra("type", "budgetPeriodic");

                //set the alarm
                PendingIntent pi = PendingIntent.getBroadcast(getApplicationContext(), 0, i, 0);
                AlarmManager alarmManager = (AlarmManager) getApplicationContext().getSystemService(Context.ALARM_SERVICE);
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), pi);
            }

        } else if (sp.getString("period", null).equals("Mensile")) {

            //check that today it is not the first of the month because this is the day we want to handle the alarm not to set it
            Calendar cal = Calendar.getInstance();
            cal.setTime(cal.getTime());

            int day = cal.get(Calendar.DAY_OF_MONTH);
            if ( day != 1) {

                int nextMonth = cal.get(Calendar.MONTH) + 1;
                boolean newYear = false;
                // check if has not exceeded threshold of december
                if(nextMonth > Calendar.DECEMBER) {
                    // alright, reset month to jan and forward year by 1 e.g fro 2013 to 2014
                    nextMonth = Calendar.JANUARY;
                    // Move year ahead as well
                    newYear = true;
                }

                if (newYear) { cal.set(Calendar.YEAR, cal.get(Calendar.YEAR) + 1); }
                else { cal.set(Calendar.YEAR, cal.get(Calendar.YEAR)); }
                cal.set(Calendar.MONTH, nextMonth);
                cal.set(Calendar.DAY_OF_MONTH, 1);

                Intent i = new Intent(getApplicationContext(), AlarmHandler.class);
                int UNIQUE_PENDINGINTENT_ID = BUDGET_MONTHLY_ID;
                i.putExtra("id", UNIQUE_PENDINGINTENT_ID);
                i.putExtra("type", "budgetPeriodic");

                //set the alarm
                PendingIntent pi = PendingIntent.getBroadcast(getApplicationContext(), UNIQUE_PENDINGINTENT_ID, i, PendingIntent.FLAG_UPDATE_CURRENT);
                AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), pi);

                }

            }
        return START_STICKY_COMPATIBILITY;
    }


}
