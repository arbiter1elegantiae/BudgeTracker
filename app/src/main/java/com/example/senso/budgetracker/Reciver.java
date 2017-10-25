package com.example.senso.budgetracker;

import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import static android.content.Context.ALARM_SERVICE;

/**
 * Reciver class that starts services for notifaction and for planned expense deleting, on boot reciver
 */

public class Reciver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {

        if (intent.getAction().equals("android.intent.action.BOOT_COMPLETED")) {

            Intent alarmIntent = new Intent(context, AlarmPush.class);
            Intent clearPassedPlannedExpense = new Intent(context, removePassedPlanned.class);

            context.startService(alarmIntent);
            context.startService(clearPassedPlannedExpense);
        }
    }



}
