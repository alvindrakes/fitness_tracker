package com.example.alvindrakes.my_fitness_tracker;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import java.util.ArrayList;

public class DetailsPage extends AppCompatActivity {

    private final String tag = "TRACKER DETAILS";

    Bundle bundle;
    ArrayList<float[]> allDistances;
    ArrayList<float[]> allDurations;
    int numMarkers;

    TextView avgSpeed, totalDistance, lastDistance, lastAvgSpeed;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details_page);

        bundle = getIntent().getExtras();
        getAllInfo();
        Log.i(tag,"number of markers: " + numMarkers);

        avgSpeed = (TextView)findViewById(R.id.AvgSpeed);
        totalDistance = (TextView)findViewById(R.id.TotalDistance);

        lastAvgSpeed = (TextView)findViewById(R.id.LastAvgSpeed);
        lastDistance = (TextView)findViewById(R.id.LastDistance);


        //if only one record in the database, there will be no last record
        if(numMarkers <= 1 ) {
            lastDistance.setText("    Total distance: No record");
            lastAvgSpeed.setText("    Average speed: No record");

        }
        else {

            lastDistance.setText("    Total distance: " + totalDistances(allDistances.get(allDistances.size()-2)) + " m");
            lastAvgSpeed.setText("    Average speed: " + avgSpeed(allDistances.get(allDistances.size()-2), allDurations.get(allDurations.size()-2)) + " m/s");
        }

        totalDistance.setText("    Total distance: " + totalDistances(allDistances.get(allDistances.size()-1)) + "m");
        avgSpeed.setText("    Average speed: " + avgSpeed(allDistances.get(allDistances.size()-1), allDurations.get(allDistances.size()-1)) + " m/s");
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
