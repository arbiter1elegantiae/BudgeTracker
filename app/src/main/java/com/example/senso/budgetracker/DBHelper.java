package com.example.senso.budgetracker;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.CursorJoiner;
import android.database.DatabaseUtils;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.MatrixCursor;
import android.util.Log;
import android.widget.ArrayAdapter;

import com.google.android.gms.maps.model.LatLng;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * Database Manager
 */

public class DBHelper extends SQLiteOpenHelper{

    private SharedPreferences sp;

    //DB structure
    public static final String DATABASE_NAME = "Expenses.db";

    //table classic expense structure
    public static final String TABLE1_NAME = "Expenses_table";
    public static final String COL_1 = "ID";
    public static final String COL_2 = "NAME";
    public static final String COL_3 = "COST";
    public static final String COL_4 = "CATEGORY";
    public static final String COL_5 = "DESCRIPTION";
    public static final String COL_6 = "DATE";
    public static final String COL_7 = "LAT";
    public static final String COL_8 = "LNG";


    //table periodic expense
    public static final String TABLE3_NAME = "Periodic_expenses_table";

    //table planned expense
    public static final String TABLE4_NAME = "Planned_expenses_table";

    //table categories
    public static final String TABLE2_NAME = "Categories_db";

    //table periodic amount
    public static final String TABLE5_NAME = "Weekly_amount";
    public static final String TABLE6_NAME = "Monthly_amount";


    public DBHelper(Context context) {
        super(context, DATABASE_NAME, null, 1);

        sp = getPrefs(context);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        //creating tables
        String createQuery1 = "CREATE TABLE " + TABLE1_NAME + "(" +
                                                COL_1 + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                                                COL_2 + " TEXT NOT NULL," +
                                                COL_3 + " REAL NOT NULL," +
                                                COL_4 +" TEXT NOT NULL," +
                                                COL_5 + " TEXT," +
                                                COL_6 +" TEXT NOT NULL," +
                                                COL_7 +" REAL," +
                                                COL_8 +" REAL " +
                                                ");";

        String createQuery2 = "CREATE TABLE " + TABLE3_NAME + "(" +
                COL_1 + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                COL_2 + " TEXT NOT NULL," +
                COL_3 + " REAL NOT NULL," +
                COL_4 +" TEXT NOT NULL," +
                COL_5 + " TEXT," +
                COL_6 +" TEXT NOT NULL " +
                ");";

        String createQuery3 = "CREATE TABLE " + TABLE4_NAME + "(" +
                COL_1 + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                COL_2 + " TEXT NOT NULL," +
                COL_3 + " REAL NOT NULL," +
                COL_4 +" TEXT NOT NULL," +
                COL_5 + " TEXT," +
                COL_6 +" TEXT NOT NULL," +
                COL_7 +" REAL," +
                COL_8 +" REAL " +
                ");";


        String createQuery4 = "CREATE TABLE " + TABLE2_NAME + "(" + COL_2 + " STRING PRIMARY KEY NOT NULL,"+ COL_3 + " REAL "+");";

        String createQuery5;
        if (sp.getString("period", null).equals("Mensile")) {

            createQuery5 = "CREATE TABLE " + TABLE6_NAME + "(" + COL_1 + " STRING PRIMARY KEY NOT NULL,"+ COL_3 + " REAL "+");";

        } else {

            createQuery5 = "CREATE TABLE " + TABLE5_NAME + "(" + COL_1 + " STRING PRIMARY KEY NOT NULL,"+ COL_3 + " REAL "+");";

        }

        db.execSQL(createQuery1);
        db.execSQL(createQuery2);
        db.execSQL(createQuery3);
        db.execSQL(createQuery4);
        db.execSQL(createQuery5);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int i, int i1) {

        db.execSQL("DROP TABLE IF EXISTS "+ TABLE1_NAME);
        db.execSQL("DROP TABLE IF EXISTS "+ TABLE2_NAME);
        db.execSQL("DROP TABLE IF EXISTS "+ TABLE3_NAME);
        db.execSQL("DROP TABLE IF EXISTS "+ TABLE4_NAME);
        onCreate(db);
    }


    private static SharedPreferences getPrefs(Context context) {
        return context.getSharedPreferences("FirstTime", Context.MODE_PRIVATE);
    }


    //Add new expense to the db, actually planned and classic expense
    public long addExpense(expense exp, String type) {

        ContentValues values = new ContentValues();

        values.put(COL_2, exp.getName());
        values.put(COL_3, exp.getCost());
        values.put(COL_4, exp.getCategory());
        values.put(COL_5, exp.getDescription());
        values.put(COL_6, exp.getDate());
        if (type.equals("normal") || type.equals("planned")) {

            values.put(COL_7, exp.getLat());
            values.put(COL_8, exp.getLng());
        }
        long code = 0;
        switch (type) {

            case  "normal":

                code = getWritableDatabase().insert(TABLE1_NAME, null, values);
                break;
            case "planned":

                code = getWritableDatabase().insert(TABLE4_NAME, null, values);
                break;
            case "periodic":

                code = getWritableDatabase().insert(TABLE3_NAME, null, values);
                break;

            }

        return code;
    }


    public double insertWeekRecord(String id, double value) {

        //it is time to report week balance

        ContentValues values = new ContentValues();

        values.put(COL_1, id);
        values.put(COL_3, value);

        return getWritableDatabase().insert(TABLE5_NAME, null, values);

    }

    public double insertMonthRecord(String id, double value) {

        //it is time to report month balance

        ContentValues values = new ContentValues();

        values.put(COL_1, id);
        values.put(COL_3, value);

        return getWritableDatabase().insert(TABLE6_NAME, null, values);

    }


    //Add and remove single catogory from db
    public long addCategory(String category) {

        ContentValues values = new ContentValues();
        values.put(COL_2, category);


        long code = getWritableDatabase().insertWithOnConflict(TABLE2_NAME, null, values, SQLiteDatabase.CONFLICT_IGNORE);
        return code;
    }

    //never used ?
    public boolean deleteCategory(String category) {
        return getWritableDatabase().delete(TABLE2_NAME, COL_2 + "= '" + category + "'", null) > 0;
    }


    public List<Pair<String, Double>> retrieveWeekly() {

        List<Pair<String, Double>> weeks = new ArrayList<Pair<String, Double>>();
        String rq = "Select * from Weekly_amount where 1";
        SQLiteDatabase db = getWritableDatabase();

        Cursor c = db.rawQuery(rq, null);
        int i = 0;
        try {
            while (c.moveToNext()) {

                //Pair<String, Integer> tmp_pair = new Pair<>()
                weeks.add(new Pair(c.getString(c.getColumnIndex(COL_1)), c.getDouble(c.getColumnIndex(COL_3))));
            }
        } finally {
            c.close();
        }
            db.close();
            return weeks;
    }

    public List<Pair<String, Double>> retrieveMonthly() {

        List<Pair<String, Double>> months = new ArrayList<Pair<String, Double>>();
        String rq = "Select * from Monthly_amount where 1";
        SQLiteDatabase db = getWritableDatabase();

        Cursor c = db.rawQuery(rq, null);
        int i = 0;
        try {
            while (c.moveToNext()) {

                months.add(new Pair(c.getString(c.getColumnIndex(COL_1)), c.getDouble(c.getColumnIndex(COL_3))));
            }
        } finally {
            c.close();
        }
        db.close();
        return months;
    }






    public void addAmountCategory(String category, float cost){

        SQLiteDatabase db = this.getReadableDatabase();
        String selectQuery = "select COST from Categories_db"
                + " where NAME = "+"'"+category+"'";
        Cursor cursor = db.rawQuery(selectQuery, null);

        Float finalAmount= null;
        if (cursor.moveToFirst()) {

            finalAmount = cursor.getFloat(0) + cost;
        }

        ContentValues cv = new ContentValues();
        cv.put(COL_3, finalAmount);

        db.update("Categories_db", cv, "NAME = ?", new String[] {category});
        db.close();
    }


    public List<Pair<String, Double>> retrieveCategories() {

        List<Pair<String, Double>> categories = new ArrayList<Pair<String, Double>>();
        String rq = "Select * from Categories_db where 1";
        SQLiteDatabase db = getWritableDatabase();

        Cursor c = db.rawQuery(rq, null);
        int i = 0;
        try {
            while (c.moveToNext()) {

                categories.add(new Pair(c.getString(c.getColumnIndex(COL_2)), c.getDouble(c.getColumnIndex(COL_3))));
            }
        } finally {
            c.close();
        }
        db.close();
        return categories;
    }


    //retrive normal expenses
    ArrayList<expense> retrieveNormal () {

        ArrayList<expense> le = new ArrayList<expense> ();
        String query = "SELECT * FROM " + TABLE1_NAME + " WHERE 1";

        Cursor cursor = getWritableDatabase().rawQuery(query, null);
        try {
            while (cursor.moveToNext()) {

                //load expense list
                expense tmp_exp = new expense("","",0,"");
                tmp_exp.setName(cursor.getString(cursor.getColumnIndex(COL_2)));
                tmp_exp.setCost(cursor.getDouble(cursor.getColumnIndex(COL_3)));
                tmp_exp.setCategory(cursor.getString(cursor.getColumnIndex(COL_4)));
                tmp_exp.setDescription(cursor.getString(cursor.getColumnIndex(COL_5)));
                tmp_exp.setDate(cursor.getString(cursor.getColumnIndex(COL_6)));
                Double lat =  Double.parseDouble(cursor.getString(cursor.getColumnIndex(COL_7)));
                Double lng =  Double.parseDouble(cursor.getString(cursor.getColumnIndex(COL_8)));
                tmp_exp.setSpot(new LatLng(lat, lng));

                le.add(tmp_exp);
            }
        } finally {
            cursor.close();
        }
        return le;
    }



    //retrive planned expenses
    ArrayList<expense> retrievePlanned () {

        ArrayList<expense> le = new ArrayList<expense> ();
        String query = "SELECT * FROM " + TABLE4_NAME + " WHERE 1";

        Cursor cursor = getWritableDatabase().rawQuery(query, null);
        try {
            while (cursor.moveToNext()) {

                //load expense list
                expense tmp_exp = new expense("","",0,"");
                tmp_exp.setName(cursor.getString(cursor.getColumnIndex(COL_2)));
                tmp_exp.setCost(cursor.getDouble(cursor.getColumnIndex(COL_3)));
                tmp_exp.setCategory(cursor.getString(cursor.getColumnIndex(COL_4)));
                tmp_exp.setDescription(cursor.getString(cursor.getColumnIndex(COL_5)));
                tmp_exp.setDate(cursor.getString(cursor.getColumnIndex(COL_6)));
                Double lat =  Double.parseDouble(cursor.getString(cursor.getColumnIndex(COL_7)));
                Double lng =  Double.parseDouble(cursor.getString(cursor.getColumnIndex(COL_8)));
                tmp_exp.setSpot(new LatLng(lat, lng));

                le.add(tmp_exp);
                Log.d("retrived:", " " + tmp_exp.toString());
            }
        } finally {
            cursor.close();
        }
        return le;
    }



    //retruve periodic expenses
    ArrayList<expense> retrievePeriodic () {

        ArrayList<expense> le = new ArrayList<expense> ();
        String query = "SELECT * FROM " + TABLE3_NAME + " WHERE 1";

        Cursor cursor = getWritableDatabase().rawQuery(query, null);
        try {
            while (cursor.moveToNext()) {
                //load expense list
                expense tmp_exp = new expense("","",0,"");
                tmp_exp.setName(cursor.getString(cursor.getColumnIndex(COL_2)));
                tmp_exp.setCost(cursor.getDouble(cursor.getColumnIndex(COL_3)));
                tmp_exp.setCategory(cursor.getString(cursor.getColumnIndex(COL_4)));
                tmp_exp.setDescription(cursor.getString(cursor.getColumnIndex(COL_5)));
                tmp_exp.setDate(cursor.getString(cursor.getColumnIndex(COL_6)));

                le.add(tmp_exp);
                Log.d("retrived:", " " + tmp_exp.toString());
            }
        } finally {
            cursor.close();
        }

        return le;
    }


    public List<String> readCategories() {
        String selectQuery = "SELECT  * FROM " + TABLE2_NAME;
        List<String> results = new ArrayList<String>();
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                results.add(cursor.getString(0));

            } while (cursor.moveToNext());
        }
        cursor.close();
        return results;
    }



    public void removePassedPlanned() {

        String sql = "DELETE FROM Planned_expenses_table WHERE DATE < date('now','-1 month', '+1 day')";
        SQLiteDatabase db = this.getWritableDatabase();

        db.execSQL(sql);
        db.close();
    }



    public void updatePassedPlannedBudget(Context context) {

        double amount= 0;
        String sql = "SELECT COST FROM Planned_expenses_table WHERE DATE < date('now','-1 month','+1 day')";
        SQLiteDatabase db = this.getWritableDatabase();

        Cursor cursor = db.rawQuery(sql, null);

        if (cursor.moveToFirst()) {
            do {
                amount = amount + cursor.getDouble(0);

            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();

        BudgetHelper bh = new BudgetHelper(context);
        bh.setBudget(amount);
    }


    public Pair<expense, Integer>  fetchExpById(int id, int type) {

        expense exp = new expense("","",0,"");
        SQLiteDatabase db = getWritableDatabase();
        String qry= null;
        Double lat;
        Double lng;
        Cursor c;
        Integer realId = 0;

        switch (type) {

            case 0:

                qry = "SELECT * FROM Planned_expenses_table WHERE 1";
                c = db.rawQuery(qry, null);
                c.moveToPosition(id);

                realId = c.getInt(c.getColumnIndex(COL_1));
                exp.setName(c.getString(c.getColumnIndex(COL_2)));
                exp.setCost(c.getDouble(c.getColumnIndex(COL_3)));
                exp.setCategory(c.getString(c.getColumnIndex(COL_4)));
                exp.setDescription(c.getString(c.getColumnIndex(COL_5)));
                exp.setDate(c.getString(c.getColumnIndex(COL_6)));
                lat =  Double.parseDouble(c.getString(c.getColumnIndex(COL_7)));
                lng =  Double.parseDouble(c.getString(c.getColumnIndex(COL_8)));
                exp.setSpot(new LatLng(lat, lng));

                c.close();
                break;

            case 1:

                qry = "SELECT * FROM Periodic_expenses_table WHERE 1";
                c = db.rawQuery(qry, null);
                c.moveToPosition(id);

                realId = c.getInt(c.getColumnIndex(COL_1));
                exp.setName(c.getString(c.getColumnIndex(COL_2)));
                exp.setCost(c.getDouble(c.getColumnIndex(COL_3)));
                exp.setCategory(c.getString(c.getColumnIndex(COL_4)));
                exp.setDescription(c.getString(c.getColumnIndex(COL_5)));
                exp.setDate(c.getString(c.getColumnIndex(COL_6)));

                c.close();
                break;

            case 2:

                qry = "SELECT * FROM Expenses_table WHERE 1";
                c = db.rawQuery(qry, null);
                c.moveToPosition(id);

                realId = c.getInt(c.getColumnIndex(COL_1));
                exp.setName(c.getString(c.getColumnIndex(COL_2)));
                exp.setCost(c.getDouble(c.getColumnIndex(COL_3)));
                exp.setCategory(c.getString(c.getColumnIndex(COL_4)));
                exp.setDescription(c.getString(c.getColumnIndex(COL_5)));
                exp.setDate(c.getString(c.getColumnIndex(COL_6)));
                lat =  Double.parseDouble(c.getString(c.getColumnIndex(COL_7)));
                lng =  Double.parseDouble(c.getString(c.getColumnIndex(COL_8)));
                exp.setSpot(new LatLng(lat, lng));

                c.close();
                break;

        }

        db.close();

        return new Pair(exp, realId);
    }



    public void removeFromId(int type, int id) {

        SQLiteDatabase db = getWritableDatabase();

        switch(type) {

            case 0:

                db.delete("Planned_expenses_table","ID=?",new String[]{""+id});
                break;

            case 1:

                db.delete("Periodic_expenses_table","ID=?",new String[]{""+id});
                break;

            case 2:

                db.delete("Expenses_table","ID=?",new String[]{""+id});
                break;
        }
        db.close();
    }


    //DBManager support function
    public ArrayList<Cursor> getData(String Query){
        //get writable database
        SQLiteDatabase sqlDB = this.getWritableDatabase();
        String[] columns = new String[] { "message" };
        //an array list of cursor to save two cursors one has results from the query
        //other cursor stores error message if any errors are triggered
        ArrayList<Cursor> alc = new ArrayList<Cursor>(2);
        MatrixCursor Cursor2= new MatrixCursor(columns);
        alc.add(null);
        alc.add(null);

        try{
            String maxQuery = Query ;
            //execute the query results will be save in Cursor c
            Cursor c = sqlDB.rawQuery(maxQuery, null);

            //add value to cursor2
            Cursor2.addRow(new Object[] { "Success" });

            alc.set(1,Cursor2);
            if (null != c && c.getCount() > 0) {

                alc.set(0,c);
                c.moveToFirst();

                return alc ;
            }
            return alc;
        } catch(SQLException sqlEx){
            Log.d("printing exception", sqlEx.getMessage());
            //if any exceptions are triggered save the error message to cursor an return the arraylist
            Cursor2.addRow(new Object[] { ""+sqlEx.getMessage() });
            alc.set(1,Cursor2);
            return alc;
        } catch(Exception ex){
            Log.d("printing exception", ex.getMessage());

            //if any exceptions are triggered save the error message to cursor an return the arraylist
            Cursor2.addRow(new Object[] { ""+ex.getMessage() });
            alc.set(1,Cursor2);
            return alc;
        }
    }


    public List<Pair<LatLng, String>> retrivePositions () {

        List<Pair<LatLng,String>> Locations = new ArrayList<Pair<LatLng, String>>();
        String rawQuery1 = "SELECT LAT, LNG FROM Expenses_table WHERE LAT NOT IN (SELECT LAT FROM Expenses_table WHERE LAT=0 AND LNG=0) AND LNG NOT IN (SELECT LAT FROM Expenses_table WHERE LAT=0 AND LNG=0)";
        String rawQuery2 = "SELECT LAT, LNG FROM Planned_expenses_table WHERE LAT NOT IN (SELECT LAT FROM Planned_expenses_table WHERE LAT=0 AND LNG=0) AND LNG NOT IN (SELECT LAT FROM Planned_expenses_table WHERE LAT=0 AND LNG=0)";

        SQLiteDatabase db = getWritableDatabase();

        Cursor c1 = db.rawQuery(rawQuery1, null);
        Cursor c2 = db.rawQuery(rawQuery2, null);

        Log.d("cursor: ", DatabaseUtils.dumpCursorToString(c1));

        try {
            while (c1.moveToNext()) {

                Double lat =  Double.parseDouble(c1.getString(c1.getColumnIndex(COL_7)));
                Double lng =  Double.parseDouble(c1.getString(c1.getColumnIndex(COL_8)));
                Locations.add(new Pair(new LatLng(lat, lng), "Spesa Normale") );
            }
        } finally {
            c1.close();
        }

        try {
            while (c2.moveToNext()) {

                Double lat =  Double.parseDouble(c2.getString(c2.getColumnIndex(COL_7)));
                Double lng =  Double.parseDouble(c2.getString(c2.getColumnIndex(COL_8)));
                Locations.add(new Pair(new LatLng(lat, lng), "Spesa Pianificata") );
            }
        } finally {
            c2.close();
        }


        return Locations;
    }







}
