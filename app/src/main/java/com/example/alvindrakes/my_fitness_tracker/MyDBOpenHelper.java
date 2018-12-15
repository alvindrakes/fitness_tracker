package com.example.alvindrakes.my_fitness_tracker;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.Date;


public class MyDBOpenHelper extends SQLiteOpenHelper {

    private final String tag = "DATABASE HELPER";

    Date date = new Date(System.currentTimeMillis());

    // create a new database
    public MyDBOpenHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, "my.db", null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        db.execSQL("CREATE TABLE tracker(trackerId INTEGER PRIMARY KEY AUTOINCREMENT," +
                "" +
                "trackerMarker INTEGER," +
                "trackerLatitude double," +
                "trackerLongitude double," +
                "trackerTime INTEGER," +
                "trackerLocation VARCHAR(128))");
        Log.d(tag, date.toString());
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
