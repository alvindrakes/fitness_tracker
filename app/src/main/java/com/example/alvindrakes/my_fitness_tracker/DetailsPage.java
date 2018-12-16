package com.example.alvindrakes.my_fitness_tracker;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import java.util.ArrayList;

public class DetailsPage extends AppCompatActivity {

    private final String tag = "TRACKER DETAILS";

    String runDistance;
    long runTime;

    TextView avgSpeed, tv_totalDistance, tv_timeTaken;
    Context context = this;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details_page);

        avgSpeed = (TextView) findViewById(R.id.AvgSpeed);
        tv_totalDistance = (TextView) findViewById(R.id.TotalDistance);
        tv_timeTaken = (TextView) findViewById(R.id.timeTaken);

        MyDBOpenHelper dbHandler = new MyDBOpenHelper(this, null, null, 1);

        TableLayout tableLayout = findViewById(R.id.tablelayout);

        // add header rows for the table
        addHeaderRow(tableLayout);


        // read data from database and add them into the table
        SQLiteDatabase db = dbHandler.getReadableDatabase();
        db.beginTransaction();

        try {

            //get all logs
            String selectQuery = "SELECT * FROM " + MyDBOpenHelper.TABLE_TRACKERLOG;
            Cursor cursor = db.rawQuery(selectQuery, null);
            if (cursor.getCount() > 0) {
                while (cursor.moveToNext()) {

                    // Read columns data
                   runDistance  = cursor.getString(cursor.getColumnIndex("distance"));
                   runTime = cursor.getLong(cursor.getColumnIndex("time"));

                    //format time to string to be displayed
                    long Seconds = (int) (runTime / 1000);
                    long Minutes = Seconds / 60;
                    Seconds = Seconds % 60;
                    long MilliSeconds = (int) (runTime % 1000);
                    String runTimetxt = "" + Minutes + ":" + String.format("%02d", Seconds) + ":" + String.format("%03d", MilliSeconds);

                    // data rows information
                    TableRow row = new TableRow(context);
                    row.setLayoutParams(new TableLayout.LayoutParams(TableLayout.LayoutParams.MATCH_PARENT,
                            TableLayout.LayoutParams.WRAP_CONTENT));

                    String[] colText = {runDistance, runTimetxt};
                    for (String text : colText) {
                        TextView tv = new TextView(this);
                        tv.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT,
                                TableRow.LayoutParams.WRAP_CONTENT));
                        tv.setGravity(Gravity.CENTER);
                        tv.setTextSize(16);
                        tv.setPadding(5, 5, 5, 5);
                        tv.setText(text);
                        row.addView(tv);
                    }
                    tableLayout.addView(row);
                }

                // set data for cardview
                tv_totalDistance.setText("    Total distance: " + runDistance);
                // avgSpeed.setText("    Average speed: " + avgSpeed(allDistances.get(allDistances.size() - 1), allDurations.get(allDistances.size() - 1)) + " m/s");
                tv_timeTaken.setText("     Time Taken: " + runTime);
            }
            db.setTransactionSuccessful();

        } catch (SQLiteException e) {
            e.printStackTrace();

        } finally {
            db.endTransaction();
            // End the transaction.
            db.close();
            // Close database
        }
    }

    private void addHeaderRow(TableLayout tableLayout) {
        TableRow rowHeader = new TableRow(context);
        rowHeader.setBackgroundColor(Color.parseColor("#5c6298"));
        rowHeader.setLayoutParams(new TableLayout.LayoutParams(TableLayout.LayoutParams.MATCH_PARENT,
                TableLayout.LayoutParams.WRAP_CONTENT));
        String[] headerText = {"Distance", "Average Speed", "Time"};
        for (String c : headerText) {
            TextView tv = new TextView(this);
            tv.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT,
                    TableRow.LayoutParams.WRAP_CONTENT));
            tv.setGravity(Gravity.CENTER);
            tv.setTextSize(18);
            tv.setPadding(5, 8, 5, 8);
            tv.setText(c);
            rowHeader.addView(tv);
        }
        tableLayout.addView(rowHeader);
    }


    //calculate average speed
//    private float avgSpeed(float[] distances,float[] durations) {
//        return totalDistances(distances) / totalDuration(durations);
//    }


    // destroy detailsPage activity and go back to mainPage
    public void backToMain(View v) {
        finish();
    }
}
