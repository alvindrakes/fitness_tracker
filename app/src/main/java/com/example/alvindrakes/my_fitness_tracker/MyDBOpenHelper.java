package com.example.alvindrakes.my_fitness_tracker;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.example.alvindrakes.my_fitness_tracker.ContentProvider.MyContentProvider;

public class MyDBOpenHelper extends SQLiteOpenHelper {

    public static final String TAG = "TrackerLogDB";

    public static final String TABLE_TRACKERLOG = "trackerlog";
    public static final String COLUMN_SPEED = "speed";
    public static final String COLUMN_DISTANCE = "distance";
    public static final String COLUMN_TIME = "time";
    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "trackerlogDB.db";
    private ContentResolver myCR;

    public MyDBOpenHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, DATABASE_NAME, factory, DATABASE_VERSION);
        myCR = context.getContentResolver();
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_TABLE_TRACKERLOG = "CREATE TABLE " +
                TABLE_TRACKERLOG + "("
                + COLUMN_DISTANCE
                + " TEXT,"
                + COLUMN_SPEED
                + " TEXT,"
                + COLUMN_TIME + " REAL" + ")";
        db.execSQL(CREATE_TABLE_TRACKERLOG);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int i, int i1) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_TRACKERLOG);
        onCreate(db);
    }

    public void addLog(TrackerLog trackerlog) {
        ContentValues values = new ContentValues();
        values.put(COLUMN_DISTANCE, trackerlog.getDistance());
        values.put(COLUMN_SPEED, trackerlog.getSpeed());
        values.put(COLUMN_TIME, trackerlog.getTime());
        myCR.insert(MyContentProvider.CONTENT_URI, values);
        Log.d(TAG, "New log added");
    }
}
