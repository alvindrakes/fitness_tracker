package com.example.alvindrakes.my_fitness_tracker;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;


public class TrackerReceiver extends BroadcastReceiver {

    private final String tag = "RECEIVER";

    private final String ACTION_LOCATION_RECEIVER = "com.example.alvindrakes.fitnesstracker.MY_LOCATION_RECEIVER";

    @Override
    public void onReceive(Context context, Intent intent) {

        // get data from tracker service and send to the map route activity
        if(ACTION_LOCATION_RECEIVER.equals(intent.getAction())) {
            double[] locationInfo = intent.getDoubleArrayExtra("locationData");

            Intent mapIntent = new Intent();
            mapIntent.setAction("com.example.alvindrakes.fitnesstracker.MY_MAP_RECEIVER");
            mapIntent.putExtra("locationData", locationInfo);
            context.sendBroadcast(mapIntent);
        }
    }
}
