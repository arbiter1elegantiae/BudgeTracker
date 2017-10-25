package com.example.senso.budgetracker;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import java.util.Date;

/**
 * AlarmNotification maker
 */

public class AlarmHandler extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {

        //budget periodic alarm to handle
        if (intent.getStringExtra("type").equals("budgetPeriodic")) {

            BudgetHelper bh = new BudgetHelper(context);
            SharedPreferences sp = context.getSharedPreferences("FirstTime", Context.MODE_PRIVATE);

            double budget = getDouble(sp, "budget", 0);
            double currentBudget = bh.getBudget();

            double tillNowExpense = budget - currentBudget;

            //it's monday!
            if (sp.getString("period", null).equals("Settimanale")) {

                //get last week sunday string rappresentation
                Calendar cal = Calendar.getInstance();
                cal.setTime(cal.getTime());
                cal.add(Calendar.DAY_OF_YEAR, -1);


                Date lastSun = cal.getTime();
                SimpleDateFormat formatter = new SimpleDateFormat("dd/MM");
                String lastSunString = formatter.format(lastSun);
                Log.d("alarmHandler:", lastSunString);


                DBHelper dbh = new DBHelper(context);
                if (dbh.insertWeekRecord(lastSunString, tillNowExpense) == -1) {
                    Log.d("alarmHandler :", "Error writing in the db");
                }

            } else {

                //get last month string rappresentation
                Calendar cal = Calendar.getInstance();
                cal.setTime(cal.getTime());
                cal.add(Calendar.MONTH, -1);

                Date lastMonth = cal.getTime();
                SimpleDateFormat formatter = new SimpleDateFormat("MM");
                String lastMonthString = formatter.format(lastMonth);


                DBHelper dbh = new DBHelper(context);
                if (dbh.insertMonthRecord(lastMonthString , tillNowExpense) == -1) {
                    Log.d("Error AlarmHandler :", "Error writing in the db");
                }

                //reset currentBudget

                bh = new BudgetHelper(context);
                bh.resetCurrentBudget(getDouble(sp, "budget", 0));
            }
        } else {

            Log.d("alarmHandler:" ,"siamo dentro la costruzione di notifiche!");
            NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            Intent main_intent = new Intent(context, MainActivity.class);
            main_intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

            PendingIntent pi = PendingIntent.getActivity(context, intent.getIntExtra("id", 0), main_intent, PendingIntent.FLAG_UPDATE_CURRENT);

            //effective build notification
            if (intent.getStringExtra("type").equals("planned")) {

                //case planned notification
                NotificationCompat.Builder builder = new NotificationCompat.Builder(context)
                        .setContentIntent(pi)
                        .setContentTitle("Hai una spesa programmata per domani!")
                        .setContentText(intent.getStringExtra("msg"))
                        .setAutoCancel(true)
                        .setSmallIcon(R.drawable.countdown_expense);
                notificationManager.notify(intent.getIntExtra("id", 0), builder.build());

            } else {

                //case periodic notification
                NotificationCompat.Builder builder = new NotificationCompat.Builder(context)
                        .setContentIntent(pi)
                        .setContentTitle("Hai una spesa periodica per domani!")
                        .setContentText(intent.getStringExtra("msg"))
                        .setAutoCancel(true)
                        .setSmallIcon(R.drawable.ic_calendar_expense);
                notificationManager.notify(intent.getIntExtra("id", 0), builder.build());
            }
        }
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
