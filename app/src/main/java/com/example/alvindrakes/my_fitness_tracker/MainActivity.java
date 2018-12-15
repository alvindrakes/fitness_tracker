package com.example.alvindrakes.my_fitness_tracker;

import android.Manifest;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.SystemClock;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    //indicate which period of running it is
    public static int MARKER;
    private final String tag = "MAIN TRACKER";

    TrackerService.TrackerBinder binder;

    private Intent intent;
    ServiceConnection trackerConnection = null;

    Boolean isBound;
    Boolean isTracking = false;

    private FloatingActionButton startBtn, stopBtn, detailsBtn, pauseBtn;
    TextView tv_Time;

    //time recording runnable process
    long MillisecondTime, StartTime, TimeBuff, UpdateTime = 0L;
    int Seconds, Minutes, MilliSeconds;
    Handler handler;
    public Runnable runnable = new Runnable() {

        public void run() {
            MillisecondTime = SystemClock.uptimeMillis() - StartTime; //update time
            UpdateTime = TimeBuff + MillisecondTime; //update total time
            Seconds = (int) (UpdateTime / 1000);
            Minutes = Seconds / 60;
            Seconds = Seconds % 60;
            MilliSeconds = (int) (UpdateTime % 1000);

            //update time textView
            tv_Time.setText("Time taken: " + Minutes + ":"
                    + String.format("%02d", Seconds) + ":"
                    + String.format("%03d", MilliSeconds));

            handler.postDelayed(this, 0);
        }

    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        startBtn = (FloatingActionButton) findViewById(R.id.start);
        pauseBtn = (FloatingActionButton) findViewById(R.id.pause);
        stopBtn = (FloatingActionButton) findViewById(R.id.stop);
        detailsBtn = (FloatingActionButton) findViewById(R.id.details);
        tv_Time = findViewById(R.id.tv_Time);

        handler = new Handler();
        intent = new Intent(this, TrackerService.class);

        stopBtn.hide();
        pauseBtn.hide();

        startBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                StartTime = SystemClock.uptimeMillis(); //get current system uptime
                handler.postDelayed(runnable, 0);
                isTracking = true;

                startBtn.hide();
                stopBtn.show();
                pauseBtn.show();

                //press start button to connect to the service
                connectTrackerService();
                Toast.makeText(getApplicationContext(), "Tracker recording starts", Toast.LENGTH_SHORT).show();
                Log.d(tag, "recording has started");
            }
        });

        pauseBtn.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View view) {
               startBtn.show();
               stopBtn.show();
               pauseBtn.hide();

               TimeBuff += MillisecondTime; //save time buffer
               handler.removeCallbacks(runnable); //stop runnable

               isTracking = false; //set status to NOT TRACKING

               Toast.makeText(getApplicationContext(), "Tracker recording pauses", Toast.LENGTH_SHORT).show();


           }
        });

        stopBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handler.removeCallbacks(runnable); //stop runnable

                startBtn.show();
                pauseBtn.hide();
                stopBtn.hide();

                //disconnect to the service
                unbindService(trackerConnection);
                stopService(intent);
                trackerConnection = null;
                //binder = null;
                Toast.makeText(getApplicationContext(), "Tracker recording stops", Toast.LENGTH_SHORT).show();
                Log.d(tag, "recording has stopped, current marker: " + MARKER);

                // reset variables value
                tv_Time.setText("");

                MillisecondTime = 0L;
                StartTime = 0L;
                TimeBuff = 0L;
                UpdateTime = 0L;
                Seconds = 0;
                Minutes = 0;
                MilliSeconds = 0;

                //set status to NOT TRACKING and NOT ACTIVE
                isTracking = false;
            }
        });

        detailsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(binder != null) {
                    //send bundle to the details activity
                    int numMarkers = binder.getLastMarkers();
                    Intent detailsIntent = new Intent(MainActivity.this, DetailsPage.class);
                    Bundle bundle = new Bundle();
                    bundle.putInt("numMarkers", numMarkers);
                    Log.i(tag, "current Marker number: " + numMarkers);

                    for(int i = 0; i < numMarkers; i++) {
                        bundle.putFloatArray("distance " + i, binder.calculateDistances(i+1));
                        bundle.putFloatArray("duration " + i, binder.calculateDurations(i+1));
                    }

                    detailsIntent.putExtras(bundle);
                    startActivity(detailsIntent);
                } else {
                    Toast.makeText(MainActivity.this, "No running record yet", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }


    //connect to the tracker service
    private void connectTrackerService() {

        if(trackerConnection == null) {

            trackerConnection = new ServiceConnection() {
                @Override
                public void onServiceConnected(ComponentName name, IBinder service) {
                    binder = (TrackerService.TrackerBinder)service;
                    MARKER = binder.getLastMarkers()+1;
                    isBound = true;
                    Log.d(tag, "Tracker service is connected");
                }

                @Override
                public void onServiceDisconnected(ComponentName name) {
                    isBound = false;
                    Log.d(tag, "Service is disconnected");
                }
            };

            bindService(intent, trackerConnection, Service.BIND_AUTO_CREATE);
        }
    }


    // check whether location permission is granted
    public boolean checkLocationPermission() {
        //if permission not granted
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            //if user already denied permission once before
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {

                //show explanation to user and ask permission again
                new AlertDialog.Builder(this)
                        .setTitle("Location Permission Needed")
                        .setMessage("Location services is needed for this app to work properly. Please allow it")
                        .setCancelable(false)
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                //Prompt the user once explanation has been shown
                                ActivityCompat.requestPermissions(MainActivity.this,
                                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 0);
                            }
                        })
                        .create()
                        .show();
            } else {
                // location permission asked for the 1st time
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        0);
            }
            return false;
        } else {
            //permission already granted
            return true;
        }
    }

    //after permission has been asked
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 0: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted
                    if (ContextCompat.checkSelfPermission(this,
                            Manifest.permission.ACCESS_FINE_LOCATION)
                            == PackageManager.PERMISSION_GRANTED) {
                        Toast.makeText(this, "Location permissions granted", Toast.LENGTH_SHORT).show();

                    }
                                        } else {

                    //permission is denied
                    //explain to user and ask permission again
                    new AlertDialog.Builder(this)
                            .setTitle("Location Permission Needed")
                            .setMessage("Location services is needed for this app to work properly. Please allow it")
                            .setCancelable(false)
                            .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    //Prompt the user once explanation has been shown
                                    ActivityCompat.requestPermissions(MainActivity.this,
                                            new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 0);
                                }
                            })
                            .create()
                            .show();
                }
                return;
            }
        }
    }


    // android lifecycle checking
    @Override
    protected void onDestroy() {
        super.onDestroy();

        //stop service when activity is destroyed
        unbindService(trackerConnection);
        stopService(intent);
        handler.removeCallbacks(runnable);
        Log.d(tag, "onDestroy");
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(tag, "onPause");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(tag, "onResume");
    }

    @Override
    protected void onStart() {
        super.onStart();
        checkLocationPermission();
        Log.d(tag, "onStart");
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d(tag, "onStop");
    }
}
