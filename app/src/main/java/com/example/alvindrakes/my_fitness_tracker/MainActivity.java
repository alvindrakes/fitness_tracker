package com.example.alvindrakes.my_fitness_tracker;

import android.app.Service;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.IBinder;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    //indicate which period of running it is
    public static int MARKER;
    private final String tag = "MAIN TRACKER";

    TrackerService.TrackerBinder binder;

    private Intent intent;
    ServiceConnection trackerConnection = null;

    private FloatingActionButton startBtn, stopBtn, detailsBtn;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        startBtn = (FloatingActionButton) findViewById(R.id.start);
        stopBtn = (FloatingActionButton) findViewById(R.id.stop);
        detailsBtn = (FloatingActionButton) findViewById(R.id.details);
       // mapBtn = (Button)findViewById(R.id.ShowMap);

        intent = new Intent(this, TrackerService.class);

        stopBtn.setEnabled(false);

        startBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                startBtn.setEnabled(false);
                stopBtn.setEnabled(true);

                //press start button to connect to the service
                connectTrackerService();
                Toast.makeText(getApplicationContext(), "Tracker recording starts", Toast.LENGTH_SHORT).show();
                Log.d(tag, "recording has started");
            }
        });

        stopBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startBtn.setEnabled(true);
                stopBtn.setEnabled(false);

                //disconnect to the service
                unbindService(trackerConnection);
                stopService(intent);
                trackerConnection = null;
                //binder = null;
                Toast.makeText(getApplicationContext(), "Tracker recording stops", Toast.LENGTH_SHORT).show();
                Log.d(tag, "recording has stopped, current marker: " + MARKER);
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


        // request for location permission
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            //mMap.setMyLocationEnabled(true);
        } else {
            ActivityCompat.requestPermissions(this,new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        }
    }


    //connect to the tracker service
    private void connectTrackerService() {

        if(trackerConnection == null) {

            trackerConnection = new ServiceConnection() {
                @Override
                public void onServiceConnected(ComponentName name, IBinder service) {
                    binder = (TrackerService.TrackerBinder)service;
                    MARKER = binder.getLastMarkers()+1;
                    Log.d(tag, "Tracker service is connected");
                }

                @Override
                public void onServiceDisconnected(ComponentName name) {
                    Log.d(tag, "Service is disconnected");
                }
            };

            bindService(intent, trackerConnection, Service.BIND_AUTO_CREATE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (permissions.length == 1 &&
                permissions[0] == android.Manifest.permission.ACCESS_FINE_LOCATION &&
                grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            //mMap.setMyLocationEnabled(true);
        } else {
            // Permission was denied. Display an error message.
        }
    }

    // android lifecycle checking
    @Override
    protected void onDestroy() {
        //stop service when activity is destroyed
        unbindService(trackerConnection);
        stopService(intent);
        super.onDestroy();
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
        Log.d(tag, "onStart");
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d(tag, "onStop");
    }
}
