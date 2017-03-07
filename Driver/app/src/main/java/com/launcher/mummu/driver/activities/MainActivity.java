package com.launcher.mummu.driver.activities;

import android.Manifest;
import android.app.FragmentTransaction;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
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
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.launcher.mummu.driver.DriverApp;
import com.launcher.mummu.driver.R;
import com.launcher.mummu.driver.UIUtils.FireBaseUtils;
import com.launcher.mummu.driver.UIUtils.UIUtils;
import com.launcher.mummu.driver.models.LocationModel;
import com.launcher.mummu.driver.service.GPSService;
import com.launcher.mummu.driver.storage.CabStorageUtil;

/**
 * Created by muhammed on 2/16/2017.
 */

public class MainActivity extends Container implements OnMapReadyCallback, GPSService.OnLocationChange, View.OnClickListener {
    private static final int PERMISSION_REQUEST = 100;
    private static final double LAT_START = 10.007151;
    private static final double LONG_START = 76.359910;
    private static final float DEFAULT_BEARING = 30f;
    private MapFragment mMapFragment;
    private GoogleMap googleMap;
    private Button mEnableDisableButton;
    private Toolbar mToolbar;
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

        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        mMapFragment = MapFragment.newInstance();
        FragmentTransaction fragmentTransaction =
                getFragmentManager().beginTransaction();
        fragmentTransaction.add(R.id.container, mMapFragment);
        mEnableDisableButton = (Button) findViewById(R.id.enableDisableButton);
        fragmentTransaction.commit();
        mMapFragment.getMapAsync(this);
        mEnableDisableButton.setOnClickListener(this);


        if (CabStorageUtil.isTrackingEnabled(this)) {
            mEnableDisableButton.setText("Enable");
        } else {
            mEnableDisableButton.setText("Disable");
        }
        onClick(mEnableDisableButton);
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
        try {
            unbindService(mServiceConnection);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }
        super.onPause();
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.googleMap = googleMap;

        googleMap.getUiSettings().setTiltGesturesEnabled(false);


//        googleMap.addMarker(new MarkerOptions())
        enableLocation();

//        googleMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
//            @Override
//            public void onMapClick(LatLng latLng) {
//                sendToServer(latLng);
//            }
//        });
    }

    private void enableLocation() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERMISSION_REQUEST);
            } else {
                googleMap.setMyLocationEnabled(true);
            }
        } else {
            googleMap.setMyLocationEnabled(true);
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (grantResults.length >= 3) {
            enableLocation();
        } else {
            Toast.makeText(this, "Sorry you cannot perform this action with out permissions!", Toast.LENGTH_SHORT).show();
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    public void onLocationChanged(Location location) {
        googleMap.clear();
        if (CabStorageUtil.isTrackingEnabled(this)) {
            sendToServer(true, new LatLng(location.getLatitude(), location.getLongitude()), location.getBearing());
        } else {

            sendToServer(false, new LatLng(LAT_START, LONG_START), DEFAULT_BEARING);
        }

    }

    private void sendToServer(boolean b, LatLng location, float bearing) {
        MarkerOptions here = new MarkerOptions().position(location).title("Here").icon(BitmapDescriptorFactory.fromResource(R.drawable.bus_logo));
        here.rotation(bearing);
        googleMap.addMarker(here);
        FirebaseDatabase firebaseInstance = DriverApp.getFirebaseInstance();
        DatabaseReference referenceFromUrl = firebaseInstance.getReferenceFromUrl(FireBaseUtils.FIREBASE_URL);
        DatabaseReference main = referenceFromUrl.child(FireBaseUtils.MAIN_TAG);
        DatabaseReference locationMain = main.child(FireBaseUtils.LOCATION_TAG);
        LocationModel locationModel = new LocationModel();

        locationModel.setLat(location.latitude);
        locationModel.setLonge(location.longitude);
        locationModel.setBearing(bearing);
        locationModel.setEnable(b);
        locationModel.setMessage("");
        locationMain.setValue(locationModel);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.enableDisableButton:
                if (mEnableDisableButton.getText().toString().equalsIgnoreCase("Enable")) {
                    mEnableDisableButton.setText("Disable");
                    mEnableDisableButton.setBackgroundResource(R.drawable.bus__green_button_selector);
                    CabStorageUtil.setTrackingEnabled(this, true);
                } else {
                    mEnableDisableButton.setText("Enable");
                    mEnableDisableButton.setBackgroundResource(R.drawable.bus_button_selector);
                    CabStorageUtil.setTrackingEnabled(this, false);
                }
                break;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.logoutMenu:
                showConfirmDialog();
                break;
        }
        return super.onOptionsItemSelected(item);

    }

    private void showConfirmDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Are you sure you want to logout?");
        builder.setPositiveButton("Continue", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                FirebaseAuth.getInstance().signOut();
                CabStorageUtil.clearData(MainActivity.this);
                finish();
            }
        });

        builder.setNegativeButton("Cancel", null);
        builder.create().show();
    }
}
