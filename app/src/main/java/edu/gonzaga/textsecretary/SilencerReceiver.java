package edu.gonzaga.textsecretary;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * Created by Tyler on 1/10/2015.
 */
public class SilencerReceiver extends BroadcastReceiver {
    private static final String TAG = "SILENCER";

    @Override
    public void onReceive(Context context, Intent intent) {
        Silencer silencer = Silencer.getInstance(context);

        //don't schedule if silencer is already on
        if (intent.getAction().equals("edu.gonzaga.text_secretary.silencer.CALENDAR_UPDATE") && !silencer.isSilenced()) {
            Log.d(TAG, "UPDATE");
            silencer.scheduleSilencing();
        } else if (intent.getAction().equals("edu.gonzaga.text_secretary.silencer.ENABLE")) {
            Log.d(TAG, "ENABLE");
            silencer.silenceRinger();
        } else if (intent.getAction().equals("edu.gonzaga.text_secretary.silencer.DISABLE")) {
            Log.d(TAG, "DISABLE");
            silencer.restoreRingerMode();
            silencer.scheduleSilencing(); //since we're up, let's do an update
        }
    }
}
