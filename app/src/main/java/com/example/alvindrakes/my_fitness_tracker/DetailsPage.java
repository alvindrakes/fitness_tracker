package com.example.alvindrakes.my_fitness_tracker;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
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

    Bundle bundle;
    ArrayList<float[]> allDistances;
    ArrayList<float[]> allDurations;
    long timeTakenNow;
    int numMarkers;

    Context context = this;

    TextView avgSpeed, totalDistance, timeTaken;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details_page);

        bundle = getIntent().getExtras();
        getAllInfo();
        Log.i(tag, "number of markers: " + numMarkers);

        avgSpeed = (TextView) findViewById(R.id.AvgSpeed);
        totalDistance = (TextView) findViewById(R.id.TotalDistance);
       // timeTaken = (TextView) findViewById(R.id.timeTaken);

        MyDBOpenHelper dbHandler = new MyDBOpenHelper(this, null, null, 1);

        TableLayout tableLayout = findViewById(R.id.tablelayout);


        // set data for cardview
        totalDistance.setText("    Total distance: " + totalDistances(allDistances.get(allDistances.size() - 1)) + "m");
        avgSpeed.setText("    Average speed: " + avgSpeed(allDistances.get(allDistances.size() - 1), allDurations.get(allDistances.size() - 1)) + " m/s");
       // timeTaken.setText("     Time Taken: " + timeTakenNow);

        addHeaderRow(tableLayout);

        SQLiteDatabase db = dbHandler.getReadableDatabase();

        Cursor cursor = db.query("tracker", null, null, null, null, null, null, null);


        // So this is the one causing the issue here !!!!!!
        if (cursor.moveToFirst()) {

                    String runDistance = totalDistances(allDistances.get(allDistances.size() - 1)) + "m";
                    String runAvgSpeed = avgSpeed(allDistances.get(allDistances.size() - 1), allDurations.get(allDistances.size() - 1)) + " m/s";
                    String runTime = Long.toString(timeTakenNow);

                    TableRow row = new TableRow(context);
                    row.setLayoutParams(new TableLayout.LayoutParams(TableLayout.LayoutParams.MATCH_PARENT,
                            TableLayout.LayoutParams.WRAP_CONTENT));

                    String[] colText = {runDistance, runAvgSpeed, runTime};
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
                //while (cursor.moveToNext());
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


    //get all information sent from main activity
    private void getAllInfo() {

        numMarkers = bundle.getInt("numMarkers");
        allDistances = new ArrayList<>();
        allDurations = new ArrayList<>();

        for(int i = 0; i < numMarkers; i++) {
            allDistances.add(bundle.getFloatArray("distance " + i));
            allDurations.add(bundle.getFloatArray("duration " + i));
        }
    }

    //calculate average speed
    private float avgSpeed(float[] distances,float[] durations) {
        return totalDistances(distances) / totalDuration(durations);
    }


    //get the total move distance
    private float totalDistances(float[] distances) {

        float totalDistance = 0;

        if(distances != null) {
            for(int i = 0; i < distances.length; i++) {
                totalDistance += distances[i];
            }
        }
        return totalDistance;
    }

    //get speeds at all time stamps
    private float totalDuration(float[] durations) {

        float totalDuration = 0;

        if(durations != null) {
            for(int i = 0; i < durations.length; i++) {
                totalDuration += durations[i];
            }
        }

        return totalDuration;
    }

    // destroy detailsPage activity and go back to mainPage
    public void backToMain(View v) {
        finish();
    }
}
