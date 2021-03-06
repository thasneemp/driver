package com.launcher.mummu.driver.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.view.Window;
import android.view.WindowManager;

import com.launcher.mummu.driver.R;
import com.launcher.mummu.driver.storage.CabStorageUtil;

public class Splash extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (CabStorageUtil.isLogged(Splash.this, CabStorageUtil.IS_LOGGED)) {
                    startActivity(new Intent(Splash.this, MainActivity.class));
                } else {
                    startActivity(new Intent(Splash.this, LoginActivityCompact.class));
                }

                finish();
            }
        }, 1000 * 3);
    }
}
