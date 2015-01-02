package edu.gonzaga.textsecretary;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.util.Log;

/**
 * Created by Tyler on 1/1/2015.
 */
public class Silencer {

    private static final String TAG = "SILENCER";
    private static int prevRingerMode = 100;	//initialize to garbage mode
    private static Context mContext;

    public static void startSilencerPoller(Context context) {
        mContext = context;
        AlarmManager alarmManager=(AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, SMS_Service.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, 0);
        alarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP,System.currentTimeMillis(),600000,
                pendingIntent);
    }

    public static void stopSilencerPoller() {
        AlarmManager alarmManager=(AlarmManager) mContext.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(mContext, SMS_Service.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(mContext, 0, intent, 0);
        alarmManager.cancel(pendingIntent);
    }

    public static void silenceRinger() {
        AudioManager ringerManager = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);
        int tempRingerMode = ringerManager.getRingerMode();
        //prevents possible conflicts between listeners
        if (tempRingerMode != AudioManager.RINGER_MODE_SILENT) {
            Log.d(TAG, tempRingerMode + " silence");
            ringerManager.setRingerMode(AudioManager.RINGER_MODE_SILENT);
            prevRingerMode = tempRingerMode;	//save current mode
        }
    }

    public static void restoreRingerMode() {
        AudioManager ringerManager = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);
        Log.d(TAG, prevRingerMode + " restore");
        //if restore necessary
        if (prevRingerMode != 100) {
            ringerManager.setRingerMode(prevRingerMode);
            prevRingerMode = 100;   //restore garbage mode
        }
    }


}
