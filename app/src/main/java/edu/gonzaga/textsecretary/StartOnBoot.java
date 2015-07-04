package edu.gonzaga.textsecretary;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

public class StartOnBoot extends BroadcastReceiver {

    private static final String TAG = "BOOT";

    @Override
    public void onReceive(Context context, Intent intent) {
        if ("android.intent.action.BOOT_COMPLETED".equals(intent.getAction())) {
            Log.d(TAG, "boot completed");
            SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
            SharedPreferences.Editor editor = settings.edit();

            //if boot is enabled, start service
            if (settings.getBoolean("start_on_boot_preference", false)) {
                Log.d(TAG, "auto start");
                Intent smsService = new Intent(context, SMS_Service.class);
                context.startService(smsService);
                editor.putBoolean("smsState", true);
            } else {
                editor.putBoolean("smsState", false);
            }
            editor.apply();
        }
    }
}
