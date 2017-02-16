package com.launcher.mummu.driver;

import android.app.Application;

import com.google.firebase.database.FirebaseDatabase;

/**
 * Created by muhammed on 2/16/2017.
 */

public class DriverApp extends Application {
    private static FirebaseDatabase database;

    public static FirebaseDatabase getFirebaseInstance() {
        return database = FirebaseDatabase.getInstance();
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

}
