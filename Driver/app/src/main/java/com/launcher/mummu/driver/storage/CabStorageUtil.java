package com.launcher.mummu.driver.storage;

import android.content.Context;

import com.google.android.gms.maps.model.LatLng;
import com.launcher.mummu.driver.activities.MainActivity;

/**
 * Created by muhammed on 2/20/2017.
 */

public class CabStorageUtil {
    public static final String USER_NAME = "username";
    public static final String IS_LOGGED = "is_logged";
    public static final String LOCATION_NAME = "location_name";
    public static final String LOCATION = "location";
    public static final String LOCATION_LAT = "location_lat";
    public static final String LOCATION_LONG = "location_long";
    public static final String LOCATION_LAT_LONG = "location_latlong";
    private static final String IS_TRACKING_ENABLED = "is_tracking_enabled";

    public static void setUsername(Context context, String key, String value) {
        CabStorage.insertStringData(context, key, value);
        setLogging(context, IS_LOGGED, true);

    }

    public static boolean isLogged(Context context, String key) {
        return CabStorage.getBooleanData(context, key);
    }

    public static String getUsername(Context context, String key) {
        return CabStorage.getStringData(context, key);
    }

    public static void setLogging(Context context, String key, boolean value) {
        CabStorage.insertBooleanData(context, key, true);
    }

    public static void storeLocation(Context context, String key, String value) {
        CabStorage.insertStringData(context, key, value);
    }

    public static String getLocation(Context context, String key) {
        return CabStorage.getStringData(context, key);
    }

    public static LatLng getLocationLatLng(Context context) {
        LatLng latLng = null;
        try {
            String locationLat = CabStorageUtil.getLocation(context, CabStorageUtil.LOCATION_LAT);
            String locationLong = CabStorageUtil.getLocation(context, CabStorageUtil.LOCATION_LONG);
            Double lat = Double.parseDouble(locationLat);
            Double longetude = Double.parseDouble(locationLong);
            latLng = new LatLng(lat, longetude);
        } catch (Exception e) {

        }
        return latLng;
    }

    public static void setTrackingEnabled(Context context, boolean value) {
        CabStorage.insertBooleanData(context, IS_TRACKING_ENABLED, value);

    }

    public static boolean isTrackingEnabled(Context context) {
        return CabStorage.getBooleanData(context, IS_TRACKING_ENABLED);
    }

    public static void clearData(Context context) {
        CabStorage.clearData(context);
    }
}
