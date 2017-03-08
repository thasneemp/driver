package com.launcher.mummu.driver.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.launcher.mummu.driver.service.GPSService;

/**
 * Created by muhammed on 3/8/2017.
 */

public class BootReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equalsIgnoreCase(Intent.ACTION_BOOT_COMPLETED)) {
            context.startService(new Intent(context, GPSService.class));
        }
    }
}
