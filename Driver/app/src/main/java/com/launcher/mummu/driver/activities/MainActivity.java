package com.launcher.mummu.driver.activities;

import android.Manifest;
import android.app.FragmentTransaction;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.launcher.mummu.driver.DriverApp;
import com.launcher.mummu.driver.R;
import com.launcher.mummu.driver.UIUtils.UIUtils;
import com.launcher.mummu.driver.service.GPSService;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by muhammed on 2/16/2017.
 */

public class MainActivity extends Container implements OnMapReadyCallback, GPSService.OnLocationChange {
    private static final int PERMISSION_REQUEST = 100;
    private MapFragment mMapFragment;
    private GoogleMap googleMap;

    private ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            GPSService.MyBinder myBinder = (GPSService.MyBinder) service;
            GPSService gpsService = myBinder.getService();
            gpsService.setOnLocationListener(MainActivity.this);

        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };
    private LocationManager locationManager;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setUI();
    }

    private void setUI() {
        mMapFragment = MapFragment.newInstance();
        FragmentTransaction fragmentTransaction =
                getFragmentManager().beginTransaction();
        fragmentTransaction.add(R.id.container, mMapFragment);
        fragmentTransaction.commit();
        mMapFragment.getMapAsync(this);
    }

    @Override
    protected void onResume() {
        //check GPS
        Intent service = new Intent(this, GPSService.class);
        startService(service);
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            UIUtils.showGPSDisabledAlertToUser(this);
        } else {
            Intent intent = new Intent(this, GPSService.class);
            bindService(intent, mServiceConnection,
                    Context.BIND_AUTO_CREATE);
        }
        super.onResume();
    }

    @Override
    protected void onPause() {
        unbindService(mServiceConnection);
        super.onPause();
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.googleMap = googleMap;

//        googleMap.addMarker(new MarkerOptions())
        enableLocation();
    }

    private void enableLocation() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                googleMap.setMyLocationEnabled(true);
                return;
            } else {
                requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, PERMISSION_REQUEST);
            }
        } else {
            googleMap.setMyLocationEnabled(true);
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (grantResults.length >= 2) {
            enableLocation();
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    public void onLocationChanged(Location location) {
        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
        MarkerOptions here = new MarkerOptions().position(latLng).title("Here").icon(BitmapDescriptorFactory.defaultMarker());
        googleMap.addMarker(here);

        sendToServer(location);
    }

    private void sendToServer(Location location) {
        FirebaseDatabase firebaseInstance = DriverApp.getFirebaseInstance();
        DatabaseReference referenceFromUrl = firebaseInstance.getReference("main");
        JSONObject mainObject = new JSONObject();
        JSONObject locationJsonObject = new JSONObject();
        try {
            locationJsonObject.put("lat", location.getLatitude());
            locationJsonObject.put("long", location.getLongitude());
            mainObject.put("location", locationJsonObject);
            mainObject.put("time", System.currentTimeMillis());
        } catch (JSONException e) {
            e.printStackTrace();
        }

        referenceFromUrl.setValue(mainObject);
    }
}
