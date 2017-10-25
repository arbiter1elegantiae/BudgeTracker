package com.example.senso.budgetracker;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;

/**
 * As the name suggest, this service removes every planned expenens thas has passed and update the budget
 */

public class removePassedPlanned extends Service {
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        DBHelper dbh = new DBHelper(this);

        dbh.updatePassedPlannedBudget(this);
        dbh.removePassedPlanned();
        return START_NOT_STICKY;
    }

}