//package com.example.alvindrakes.my_fitness_tracker;
//
//import android.content.BroadcastReceiver;
//import android.content.Context;
//import android.content.Intent;
//import android.content.IntentFilter;
//import android.os.Bundle;
//import android.support.v4.app.FragmentActivity;
//import android.util.Log;
//
//import com.google.android.gms.maps.CameraUpdateFactory;
//import com.google.android.gms.maps.GoogleMap;
//import com.google.android.gms.maps.OnMapReadyCallback;
//import com.google.android.gms.maps.SupportMapFragment;
//import com.google.android.gms.maps.model.LatLng;
//import com.google.android.gms.maps.model.MarkerOptions;
//import com.google.android.gms.maps.model.PolylineOptions;
//
//public class MapRouteActivity extends FragmentActivity implements OnMapReadyCallback {
//
//    private GoogleMap mMap;
//    private PolylineOptions myLines = new PolylineOptions();
//
//    private final String tag = "map";
//    private double[] locationInfo;
//
//    //set broadcast receiver to get location information
//    BroadcastReceiver mapReceiver = new BroadcastReceiver() {
//        @Override
//        public void onReceive(Context context, Intent intent) {
//            //show the route on the map
//            locationInfo = intent.getDoubleArrayExtra("locationMsg");
//            LatLng myLatLng = new LatLng(locationInfo[0], locationInfo[1]);
//            myLines.add(myLatLng);
//            mMap.clear();
//            mMap.addPolyline(myLines);
//            mMap.moveCamera(CameraUpdateFactory.newLatLng(myLatLng));
//            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(myLatLng, 18));
//            mMap.addMarker(new MarkerOptions().position(myLatLng).title("you are here"));
//            Log.i(tag, "location updated");
//        }
//    };
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_maps);
//        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
//        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
//                .findFragmentById(R.id.map);
//        mapFragment.getMapAsync(this);
//    }
//
//    /**
//     * Manipulates the map once available.
//     * This callback is triggered when the map is ready to be used.
//     * This is where we can add markers or lines, add listeners or move the camera. In this case,
//     * we just add a curMarker near Sydney, Australia.
//     * If Google Play services is not installed on the device, the user will be prompted to install
//     * it inside the SupportMapFragment. This method will only be triggered once the user has
//     * installed Google Play services and returned to the app.
//     */
//    @Override
//    public void onMapReady(GoogleMap googleMap) {
//
//        mMap = googleMap;
//    }
//
//
//
//    @Override
//    public void onResume() {
//        super.onResume();
//        IntentFilter filter = new IntentFilter("com.example.psyrq.runningtracker.MY_MAP_RECEIVER");
//        registerReceiver(mapReceiver, filter);
//    }
//
//    @Override
//    public void onPause() {
//        super.onPause();
//        //unregister the broadcast receiver
//        unregisterReceiver(mapReceiver);
//    }
//
//    @Override
//    public void onDestroy() {
//        super.onDestroy();
//    }
//}
