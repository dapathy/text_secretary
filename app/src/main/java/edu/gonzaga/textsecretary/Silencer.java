package edu.gonzaga.textsecretary;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.media.AudioManager;
import android.util.Log;

import java.util.Calendar;

/**
 * Created by Tyler on 1/1/2015.
 */
public class Silencer extends BroadcastReceiver {

    private static final int SILENCER_GARBAGE_MODE = 100;   //used for when silencer is not active
    private static final int CALENDAR_POLL_FREQ = 15 * 60 * 1000;   //15 minutes

    private static final String TAG = "SILENCER";
    private static boolean isSilenced = false;
    private static int prevRingerMode = SILENCER_GARBAGE_MODE;
    private static Context mContext;
    private static Calendar_Service calendarService;

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals("edu.gonzaga.text_secretary.silencer.CALENDAR_UPDATE") && !isSilenced) {
            scheduleSilencing(retrieveCalendarEvents());
        }
        else if (intent.getAction().equals("edu.gonzaga.text_secretary.silencer.ENABLE")) {
            silenceRinger();
        }
        else if (intent.getAction().equals("edu.gonzaga.text_secretary.silencer.DISABLE")) {
            restoreRingerMode();
        }
    }

    //starts periodic check of calendar for times to enable/disable silencer
    public static void startSilencerPoller(Context context) {
        mContext = context;
        calendarService = new Calendar_Service(mContext);
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, Silencer.class);
        intent.setAction("edu.gonzaga.text_secretary.silencer.CALENDAR_UPDATE");
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, 0);
        alarmManager.setInexactRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP,System.currentTimeMillis(),CALENDAR_POLL_FREQ,
                pendingIntent);
    }

    public static void stopSilencerPoller() {
        AlarmManager alarmManager = (AlarmManager) mContext.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(mContext, Silencer.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(mContext, 0, intent, 0);
        alarmManager.cancel(pendingIntent);

        restoreRingerMode();
    }

    private void scheduleSilencing(Cursor cursor) {
        //if event exists
        if (cursor.moveToNext()) {
            //get first event info
            long start = cursor.getLong(Calendar_Service.ProjectionAttributes.BEGIN);
            long end = cursor.getLong(Calendar_Service.ProjectionAttributes.END);
            AlarmManager alarmManager = (AlarmManager) mContext.getSystemService(Context.ALARM_SERVICE);

            //schedule silencer enable at start of event
            Intent enableIntent = new Intent(mContext, Silencer.class);
            enableIntent.setAction("edu.gonzaga.text_secretary.silencer.ENABLE");
            PendingIntent enablePendingIntent = PendingIntent.getBroadcast(mContext, 0, enableIntent, 0);
            alarmManager.set(AlarmManager.RTC_WAKEUP,start,enablePendingIntent);

            //schedule silencer shut off at end of event
            Intent disableIntent = new Intent(mContext, Silencer.class);
            enableIntent.setAction("edu.gonzaga.text_secretary.silencer.DISABLE");
            PendingIntent disablePendingIntent = PendingIntent.getBroadcast(mContext, 0, disableIntent, 0);
            alarmManager.set(AlarmManager.RTC_WAKEUP,end,disablePendingIntent);
        }
        cursor.close();
    }

    private Cursor retrieveCalendarEvents() {
        Calendar start = Calendar.getInstance();
        Calendar end = Calendar.getInstance();
        end.add(Calendar.MINUTE, 30);
        return calendarService.getCursorForDates(start, end);
    }

    public static void silenceRinger() {
        AudioManager ringerManager = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);
        int tempRingerMode = ringerManager.getRingerMode();
        //prevents possible conflicts between listeners
        if (tempRingerMode != AudioManager.RINGER_MODE_SILENT) {
            isSilenced = true;
            Log.d(TAG, tempRingerMode + " silence");
            ringerManager.setRingerMode(AudioManager.RINGER_MODE_SILENT);
            prevRingerMode = tempRingerMode;	//save current mode
        }
    }

    public static void restoreRingerMode() {
        Log.d(TAG, prevRingerMode + " restore");
        //if restore necessary
        if (prevRingerMode != SILENCER_GARBAGE_MODE) {
            AudioManager ringerManager = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);
            ringerManager.setRingerMode(prevRingerMode);
            prevRingerMode = SILENCER_GARBAGE_MODE;   //restore garbage mode
            isSilenced = false;
        }
    }


}
