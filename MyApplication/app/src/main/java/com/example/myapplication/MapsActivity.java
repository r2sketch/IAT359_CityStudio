package com.example.myapplication;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.SQLException;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static java.lang.Double.parseDouble;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {
//  Google maps
    private GoogleMap mMap;
//  GPS
    LocationRequest mLocationRequest;
    Location mLastLocation;
    Marker mCurrLocationMarker;
    FusedLocationProviderClient mFusedLocationClient;
//  Latitude longitude for initial map camera position
    LatLng cameraPos = new LatLng(49.27091, -123.1044);
//  Message displayed in dialog box
    String alert1 = "INTRODUCTION";
    String alert2 = "Green infrastructure (GI) is an emerging field and approach to rainwater management that uses both engineered and ecosystem-based practices to protect, restore and mimic the natural water cycle. It uses soils, plants, trees and built structures such as blue-green roofs, swales, rainwater tree trenches and rain gardens to capture, store and clean rainwater before being absorbed in the ground or returning it to our waterways and atmosphere.";
    String alert3 = "As an educational application, this application features an interactable map that displays nearby GI sites and information about those sites. You can also find information on volunteer opportunities and send feedback directly to the city through the functions provided by the application.";
//  Cursor to query SQLite data for the map point placements
    Cursor c = null;
// List of GI type strings for type checking
   String GI_type1 = "Bioretention Bulge";
   String GI_type2 = "Bioretention Cell";
   String GI_type3 = "Bioretention Garden";
   String GI_type4 = "Bioswale";
   String GI_type5 = "Country Lane";
   String GI_type6 = "Infiltration Trench";
   String GI_type7 = "Permeable Concrete Pavers";
   String GI_type8 = "Permeable Rubber";
   String GI_type9 = "Pervious Concrete";
   String GI_type10 = "Porous Asphalt";
   String GI_type11 = "Soil Cell";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
//      Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

//      Instantiate  FusedLocationProviderClient
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

//      Initial pop up for information about the app and GI
        SharedPreferences preferences = getSharedPreferences("PREFS", 0);
        boolean ifShowDialog = preferences.getBoolean("showDialog", true);
//      Only show the dialog if ifShowDialog is true
        if (ifShowDialog){
            showDialog();
        }
    }

    public void onPause() {
        super.onPause();
//      Stop location updates when Activity is no longer active
        if (mFusedLocationClient != null) {
            mFusedLocationClient.removeLocationUpdates(mLocationCallback);
        }
    }

//  Dialog box pop up
    private void showDialog() {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder
                .setCancelable(false)
                .setMessage(alert1 +"\n\n"+ alert2 +"\n\n"+ alert3)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                }
        )       .setNeutralButton("NEVER SHOW AGAIN", new DialogInterface.OnClickListener() {
                     @Override
                     public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
//                      Store in SharedPreferences whether user wants to never see this pop up again
                        SharedPreferences preferences = getSharedPreferences("PREFS", 0);
                        SharedPreferences.Editor editor = preferences.edit();
                        editor.putBoolean("showDialog", false);
                        editor.apply();
                }
        });
        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.setCanceledOnTouchOutside(false);
        alertDialog.show();
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
//        mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);

//      Try to import SQLite database for map points
        DatabaseHelper myDbHelper = new DatabaseHelper(MapsActivity.this);
        try {
            myDbHelper.createDataBase();
        } catch (IOException ioe) {
            throw new Error("Unable to create database");
        }
        try {
            myDbHelper.openDataBase();
        } catch (SQLException sqle) {
            throw sqle;
        }
        Toast.makeText(MapsActivity.this, "Database successfully Imported", Toast.LENGTH_SHORT).show();
//      Cursor query
        c = myDbHelper.query("citystudio_Sheet1", null, null, null, null, null, null);
        if (c.moveToFirst()) {
            do {
//              Create LatLng from query results
                LatLng facdb = new LatLng(c.getDouble(1), c.getDouble(2));
//              Create map marker using query results
                MarkerOptions markerOptions = new MarkerOptions();
                markerOptions.position(facdb);
                markerOptions.title(c.getString(3));
                markerOptions.snippet("Learn more about this type of GI");

//              Check what GI type it is by comparing string values
//              Make marker different based on GI type
                if (c.getString(3).equals(GI_type1)){
                    markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
                } else if (c.getString(3).equals(GI_type2)){
                    markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE));
                } else if (c.getString(3).equals(GI_type3)){
                    markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW));
                } else if (c.getString(3).equals(GI_type4)){
                    markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_VIOLET));
                } else if (c.getString(3).equals(GI_type5)){
                    markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE));
                } else if (c.getString(3).equals(GI_type6)){
                    markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_CYAN));
                } else if (c.getString(3).equals(GI_type7)){
                    markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ROSE));
                }

                mMap.addMarker(markerOptions);

            } while (c.moveToNext());
        }

//      Move the camera
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(cameraPos, 11));

//      Request location
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(30000); // request location in 30 seconds intervals (in milliseconds, inexact)
        mLocationRequest.setFastestInterval(30000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);

//       Check permissions to use location
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) {
//              Location Permission already granted
                mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());
                mMap.setMyLocationEnabled(true);
            } else {
//              Request Location Permission
                checkLocationPermission();
            }
        } else {
            mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());
            mMap.setMyLocationEnabled(true);

        }
    }

//  Update location
    LocationCallback mLocationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(LocationResult locationResult) {
            List<Location> locationList = locationResult.getLocations();
            if (locationList.size() > 0) {
//               The last location in the list is the newest
                Location location = locationList.get(locationList.size() - 1);
                Log.i("MapsActivity", "Location: " + location.getLatitude() + " " + location.getLongitude());
                mLastLocation = location;
                if (mCurrLocationMarker != null) {
                    mCurrLocationMarker.remove();
                }

//              Place current location marker
                LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
            }
        }
    };

    public static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;
    private void checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {

                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
                new AlertDialog.Builder(this)
                        .setTitle("Location Permission Needed")
                        .setMessage("This app needs the Location permission, please accept to use location functionality")
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                //Prompt the user once explanation has been shown
                                ActivityCompat.requestPermissions(MapsActivity.this,
                                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                        MY_PERMISSIONS_REQUEST_LOCATION );
                            }
                        })
                        .create()
                        .show();


            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION );
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // location-related task you need to do.
                    if (ContextCompat.checkSelfPermission(this,
                            Manifest.permission.ACCESS_FINE_LOCATION)
                            == PackageManager.PERMISSION_GRANTED) {

                        mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());
                        mMap.setMyLocationEnabled(true);
                    }

                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    Toast.makeText(this, "permission denied", Toast.LENGTH_LONG).show();
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }
}


/* Code referenced from:
https://developers.google.com/android/reference/com/google/android/gms/location/LocationRequest#setInterval(long)
https://stackoverflow.com/questions/44992014/how-to-get-current-location-in-googlemap-using-fusedlocationproviderclient
https://developers.google.com/android/reference/com/google/android/gms/maps/MapFragment
https://google-developer-training.github.io/android-developer-fundamentals-course-concepts/en/Unit%201/33_c_the_android_support_library.html
https://codinginflow.com/tutorials/android/bottomnavigationview
https://developers.google.com/maps/documentation/android-sdk/marker
https://viden.io/knowledge/read-existing-sqlite-database-in-android-studio/attachment/7831/copydbactivity-java/preview
* */