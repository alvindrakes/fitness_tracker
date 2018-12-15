package com.example.alvindrakes.my_fitness_tracker;

import android.app.Service;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import com.example.alvindrakes.my_fitness_tracker.ContentProvider.MyProviderContract;

public class TrackerService extends Service {

    Context mContext;
    private SQLiteDatabase db;
    private MyDBOpenHelper myDBHelper;
    private ContentResolver resolver;

    private final String TAG = "TRACKER SERVICE";
    private TrackerBinder binder = new TrackerBinder();

    int lastMarkers = 0;
    private double[] currentLocationData = new double[2];

    LocationManager locationManager;
    MyLocationListener locationListener;

    @Override
    public void onCreate() {

        super.onCreate();

        resolver = this.getContentResolver();

        mContext = TrackerService.this;
        myDBHelper = new MyDBOpenHelper(mContext, "my.db", null, 1);
        db = myDBHelper.getWritableDatabase();

        // activate the location listener when there is movement
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        locationListener = new MyLocationListener();

        try{
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 500, 1, locationListener);
        }catch(SecurityException se) {
            Log.i(TAG, se.toString());
        }

        Log.i(TAG, "onCreate");
    }

    public class TrackerBinder extends Binder {

        public int getLastMarkers() {
            return lastMarkers();
        }

        //get all records in the databas for some period of running
        public Location[] getMarkedLocations(int marker) {

            int i = 0;
            int markedRecord = getCount(marker);
            double[][] locationInfo = new double[markedRecord][2];
            long[] timeStamps = new long[markedRecord];
            Location[] locations = new Location[markedRecord];

            Cursor cursor = resolver.query(MyProviderContract.URI.ID_QUERY, null, "trackerMarker = ?", new String[]{String.valueOf(marker)}, null);
            if(cursor.moveToFirst()) {
                do{
                    //package the latitude, longitude and time into a location object
                    locationInfo[i][0] = cursor.getDouble(cursor.getColumnIndex("trackerLatitude"));
                    locationInfo[i][1] = cursor.getDouble(cursor.getColumnIndex("trackerLongitude"));
                    timeStamps[i] = cursor.getLong(cursor.getColumnIndex("trackerTime"));
                    locations[i] = coordinateToLocation(locationInfo[i][0], locationInfo[i][1], timeStamps[i]);
                    i++;
                }while(cursor.moveToNext());
            }
            cursor.close();

            return locations;
        }

        //calculate distances between two locations sequentially in some period
        public float[] calculateDistances(int marker) {

            Location[] markedLocations = getMarkedLocations(marker);
            float[] distances = new float[markedLocations.length-1];

            for(int i = 0; i < distances.length; i++) {
                distances[i] = getDistance(markedLocations[i], markedLocations[i+1]);
            }

            return distances;
        }

        //calculate time differences between two locations sequentially in some period
        public float[] calculateDurations(int marker) {

            Location[] markedLocations = getMarkedLocations(marker);
            float[] durations = new float[markedLocations.length-1];

            for(int i = 0; i < durations.length; i++) {
                durations[i] = getDuration(markedLocations[i], markedLocations[i+1]);
            }

            return durations;
        }
        //package a location with the saved latitude, longitude and durations
        private Location coordinateToLocation(double latitude, double longitude, long time) {

            Location location = new Location("point");
            location.setLatitude(latitude);
            location.setLongitude(longitude);
            location.setTime(time);

            return location;
        }

        //clear all the record in the databases
        public void DeleteRecords() {

            resolver.delete(MyProviderContract.URI.ID_DELETE, null, null);
            Toast.makeText(getApplicationContext(), "clear all records", Toast.LENGTH_SHORT).show();
        }

        //get the marker of last record in the database
        public int lastMarkers() {

            Cursor cursor = db.rawQuery("select * from tracker order by trackerId desc limit 0,1", null);
            if(cursor.moveToFirst()) {
                lastMarkers = cursor.getInt(cursor.getColumnIndex("trackerMarker"));
            }

            return lastMarkers;
        }

        //get how many records for a period of running marked by the marker
        private int getCount(int marker) {

            int result = 0;
            Cursor cursor = db.rawQuery("SELECT Count (*) FROM tracker where trackerMarker = ?", new String[]{String.valueOf(marker)});
            if(cursor.moveToFirst())
                result = cursor.getInt(0);
            cursor.close();

            return result;
        }

        //get distances between two different locations
        private float getDistance(Location lastLocation, Location curLocation) {
            return curLocation.distanceTo(lastLocation);
        }

        //get durations durations between two different locations
        private float getDuration(Location lastLocation, Location curLocation) {
            return (curLocation.getTime() - lastLocation.getTime()) / 1000;
        }
    }

    //implement the location listener
    private class MyLocationListener implements LocationListener {

        @Override
        public void onLocationChanged(Location location) {

            Intent locationIntent = new Intent();
            locationIntent.setAction("com.example.alvindrakes.fitnesstracker.MY_LOCATION_RECEIVER");

            currentLocationData[0] = location.getLatitude();
            currentLocationData[1] = location.getLongitude();
            long curTime = location.getTime();

            //send the broadcast, current location information included
            locationIntent.putExtra("locationData", currentLocationData);
            sendBroadcast(locationIntent);

            //insert the all new location data into the database
            ContentValues insertValues = new ContentValues();
            insertValues.put("trackerMarker", MainActivity.MARKER);
            insertValues.put("trackerLatitude", currentLocationData[0]);
            insertValues.put("trackerLongitude", currentLocationData[1]);
            insertValues.put("trackerTime", curTime);
            insertValues.put("trackerLocation", location.toString());
            resolver.insert(MyProviderContract.URI.ID_INSERT, insertValues);

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
            locationManager = null;
            locationListener = null;
            Log.d(TAG, "onProviderDisabled: " + provider);
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.d(TAG, "onBind");
        return binder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        super.onUnbind(intent);
        //stop updating when the service is stopped
        locationManager.removeUpdates(locationListener);
        Log.d(TAG, "onUnbind");
        return true;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy");
    }

    @Override
    public void onRebind(Intent intent) {
        super.onRebind(intent);
        Log.d(TAG, "onRebind");
    }
}
