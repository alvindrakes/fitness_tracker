package com.example.alvindrakes.my_fitness_tracker;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

/*
    Service responsible to broadcast updated location info to broadcast receiver
 */
public class TrackerService extends Service implements LocationListener {

    private final String TAG = "TRACKER SERVICE";
    private final Binder mBind = new mBinder();


    public TrackerService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.d(TAG, "onBind");
        return mBind;
    }

    @SuppressLint("MissingPermission")
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        // initialise location manager and listener
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        TrackerService locationListener = new TrackerService();


        try {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                    5, // minimum time interval between updates
                    5, // minimum distance between updates, in metres
                    locationListener);
        } catch (SecurityException e) {
        }
        return START_STICKY;
    }

    public class mBinder extends Binder {
        TrackerService getService() {
            return TrackerService.this;
        }
    }

    @Override
        public void onLocationChanged(Location location) {
            Log.d(TAG, "Location changed");

            // broadcast new location to broadcast receiver in Main Activity
            Intent i = new Intent("LocationBroadcastService");
            i.putExtra("loc", location);
            LocalBroadcastManager.getInstance(this).sendBroadcast(i);
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
            Log.d(TAG, "onStatusChanged: " + provider + " " + status);
        }

        @Override
        public void onProviderEnabled(String provider) {
            Log.d(TAG, "onProviderEnabled: " + provider);
        }

        @Override
        public void onProviderDisabled(String provider) {
            Log.d(TAG, "onProviderDisabled: " + provider);
        }
}
