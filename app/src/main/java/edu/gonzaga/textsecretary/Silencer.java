package edu.gonzaga.textsecretary;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.media.AudioManager;
import android.os.Build;
import android.util.Log;

import java.util.Calendar;

/**
 * Created by Tyler on 1/1/2015.
 */
public class Silencer {

    private static final int SILENCER_GARBAGE_MODE = 100;   //used for when silencer is not active
    private static final int CALENDAR_POLL_FREQ = 15 * 60 * 1000;   //15 minutes
    private static final String TAG = "SILENCER";

    private static volatile Silencer instance;   //instance of self
    private boolean isSilenced = false;
    private int prevRingerMode = SILENCER_GARBAGE_MODE;
    private Context mContext;
    private Calendar_Service calendarService;
    private AlarmManager alarmManager;
    private PendingIntent enablePendingIntent;
    private PendingIntent disablePendingIntent;

    //do not create an instance
    private Silencer (Context context) {
        mContext = context;
        calendarService = new Calendar_Service(mContext);
        alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        Intent enableIntent = new Intent(mContext, SilencerReceiver.class);
        enableIntent.setAction("edu.gonzaga.text_secretary.silencer.ENABLE");
        enablePendingIntent = PendingIntent.getBroadcast(mContext, 0, enableIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        Intent disableIntent = new Intent(mContext, SilencerReceiver.class);
        disableIntent.setAction("edu.gonzaga.text_secretary.silencer.DISABLE");
        disablePendingIntent = PendingIntent.getBroadcast(mContext, 0, disableIntent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    public static Silencer getInstance(Context context) {
        if (instance == null) {
            synchronized (Silencer.class) {
                if (instance == null) {
                    instance = new Silencer(context);
                }
            }
        }

        return instance;
    }

    //starts periodic check of calendar for times to enable/disable silencer
    public void startSilencerPoller() {
        Intent updateIntent = new Intent(mContext, SilencerReceiver.class);
        updateIntent.setAction("edu.gonzaga.text_secretary.silencer.CALENDAR_UPDATE");
        PendingIntent updatePendingIntent = PendingIntent.getBroadcast(mContext, 0, updateIntent, 0);
        alarmManager.setInexactRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP,System.currentTimeMillis(),CALENDAR_POLL_FREQ,
                updatePendingIntent);

        //if silencer enabled while in event, then silence
        if (calendarService.inEvent()) {
            silenceRinger();
            setExactAlarm(calendarService.getEventEnd(), disablePendingIntent);
        }

        scheduleSilencing();    //schedule immediately
        Log.d(TAG, "started poller");
    }

    //remove all alarms
    public void stopSilencerPoller() {
        Intent updateIntent = new Intent(mContext, SilencerReceiver.class);
        updateIntent.setAction("edu.gonzaga.text_secretary.silencer.CALENDAR_UPDATE");
        PendingIntent updatePendingIntent = PendingIntent.getBroadcast(mContext, 0, updateIntent, 0);

        alarmManager.cancel(updatePendingIntent);
        cancelSilencerAlarms();
        restoreRingerMode();
        Log.d(TAG, "stopped poller");
    }

    private void cancelSilencerAlarms() {
        alarmManager.cancel(enablePendingIntent);
        alarmManager.cancel(disablePendingIntent);
    }

    protected void scheduleSilencing() {
        Cursor cursor = retrieveCalendarEvents();

        //if event exists
        if (cursor.moveToNext()) {
            Log.d(TAG, "event found");
            //get first event info
            long start = cursor.getLong(Calendar_Service.ProjectionAttributes.BEGIN);
            long end = cursor.getLong(Calendar_Service.ProjectionAttributes.END);

            setExactAlarm(start, enablePendingIntent);
            setExactAlarm(end, disablePendingIntent);
        }
        else
            Log.d(TAG, "event not found");
        cursor.close();
    }

    private void setExactAlarm(long time, PendingIntent pendingIntent) {
        //if running 4.4 or higher ensure os uses exact timing
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            alarmManager.setExact(AlarmManager.RTC_WAKEUP,time,pendingIntent);
        }
        //android didn't manage alarms at this point, so use regular method
        else {
            alarmManager.set(AlarmManager.RTC_WAKEUP,time,pendingIntent);
        }
    }

    private Cursor retrieveCalendarEvents() {
        Calendar start = Calendar.getInstance();
        Calendar end = Calendar.getInstance();
        end.add(Calendar.DATE, 1);
        return calendarService.getCursorForDates(start, end);
    }

    public void silenceRinger() {
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

    public void restoreRingerMode() {
        Log.d(TAG, prevRingerMode + " restore");
        //if restore necessary
        if (prevRingerMode != SILENCER_GARBAGE_MODE) {
            AudioManager ringerManager = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);
            ringerManager.setRingerMode(prevRingerMode);
            prevRingerMode = SILENCER_GARBAGE_MODE;   //restore garbage mode
            isSilenced = false;
        }
    }

    public boolean isSilenced() {
        return isSilenced;
    }
}
