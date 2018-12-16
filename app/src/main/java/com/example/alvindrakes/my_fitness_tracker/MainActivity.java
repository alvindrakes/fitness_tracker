package com.example.alvindrakes.my_fitness_tracker;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.SystemClock;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
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
    private final String tag = "MAIN TRACKER";

    private Intent intent;
    TrackerService trackerService;

    Boolean isBound;
    Boolean isTracking = false;
    Boolean isActive = false;

    private FloatingActionButton startBtn, stopBtn, detailsBtn, pauseBtn;
    TextView tv_Time, tv_Distance;

    Location location, oLocation;
    float distanceTaken, totalDistance = (float) 0.00;


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

    private NotificationManager notificationManager;
    //ServiceConnection for service
    private ServiceConnection myConnection = new ServiceConnection() {
        //Bind Service
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            TrackerService.mBinder binder = (TrackerService.mBinder) service;
            trackerService = binder.getService();
            isBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            isBound = false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            notificationManager = getSystemService(NotificationManager.class); //assign notification manager to system
        }

        startBtn = (FloatingActionButton) findViewById(R.id.start);
        pauseBtn = (FloatingActionButton) findViewById(R.id.pause);
        stopBtn = (FloatingActionButton) findViewById(R.id.stop);
        detailsBtn = (FloatingActionButton) findViewById(R.id.details);
        tv_Time = findViewById(R.id.tv_Time);
        tv_Distance = findViewById(R.id.tv_Distance);

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

                //if not paused
                if (!isActive) {
                    tv_Distance.setText("0.00m"); //if starting new log, reset distance textView
                    isActive = true; //set status to ACTIVE (currently tracking)
                }

                //press start button to connect to the service
                Toast.makeText(getApplicationContext(), "Tracker recording starts", Toast.LENGTH_SHORT).show();
                Log.d(tag, "recording has started");
                createNotification();
                oLocation = location; //set start location to current location
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
               createNotification();

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

                MyDBOpenHelper dbHandler = new MyDBOpenHelper(getBaseContext(), null, null, 1); //call database helper
                TrackerLog trackerLog = new TrackerLog(tv_Distance.getText().toString(), UpdateTime); //create new record TrackerLog
                dbHandler.addLog(trackerLog); //add new log to database
                Log.d(tag, "Log saved");

                // reset variables value
                tv_Time.setText("");
                tv_Distance.setText("");

                MillisecondTime = 0L;
                StartTime = 0L;
                TimeBuff = 0L;
                UpdateTime = 0L;
                Seconds = 0;
                Minutes = 0;
                MilliSeconds = 0;

                //set status to NOT TRACKING and NOT ACTIVE
                isTracking = false;
                isActive = false;
            }
        });

        detailsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent detailsIntent = new Intent(MainActivity.this, DetailsPage.class);
                startActivity(detailsIntent);
            }
        });
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


    //function to send notification
    public void createNotification() {
        Intent intent = getIntent();
        PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), (int) System.currentTimeMillis(), intent, PendingIntent.FLAG_UPDATE_CURRENT);

        //notification channel created to support android SDK 26+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel notificationChannel = new NotificationChannel("runningTracker", "RunningTracker", notificationManager.IMPORTANCE_LOW);
            notificationManager.createNotificationChannel(notificationChannel);
        }

        Notification notif = new NotificationCompat.Builder(this, "runningTracker")
                .setSmallIcon(R.drawable.ic_launcher_background)
                .setContentTitle("RunningTracker")
                .setContentText(isTracking ? "Tracking" : "Paused")
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setContentIntent(pendingIntent)
                .setOngoing(true)
                .build();

        notificationManager.notify(1, notif); //send notification

    }

    @Override
    protected void onStart() {
        super.onStart();

        Log.d(tag, "Starting service");

        //bind service
        intent = new Intent(this, TrackerService.class);
        bindService(intent, myConnection, Context.BIND_AUTO_CREATE);

        //check if location permission is granted
        if (checkLocationPermission()) {

            //Broadcast receiver everytime location is updated
            LocalBroadcastManager.getInstance(this).registerReceiver(
                    new BroadcastReceiver() {
                        @Override
                        public void onReceive(Context context, Intent intent) {
                            location = intent.getExtras().getParcelable("loc");
                            try {
                                //if status is TRACKING, calculate new distance and display
                                if (isTracking) {
                                    distanceTaken = oLocation.distanceTo(location);
                                    oLocation = location;
                                    totalDistance = distanceTaken + totalDistance;
                                    String distance = String.format("%.2f", totalDistance);
                                    tv_Distance.setText(distance + "m");
                                }
                            } catch (Exception e) {
                            }
                        }
                    }
                    , new IntentFilter("LocationBroadcastService"));
            startService(intent);
        }
    }

    // android lifecycle checking
    @Override
    protected void onDestroy() {
        super.onDestroy();

        //stop service when activity is destroyed
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
    protected void onStop() {
        super.onStop();
        Log.d(tag, "onStop");
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

        //do not destroy app when back button is pressed
        moveTaskToBack(true);
    }

}
