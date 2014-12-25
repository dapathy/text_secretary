package edu.gonzaga.textsecretary;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

public class DrivingNotification{
	private static final String TAG = "NOTIFICATION";
	private Context mContext;

    public DrivingNotification(Context context) { 
        mContext = context;
    } 
	
	public void displayNotification() {
		Log.d(TAG, "notification");
				
		/* Invoking the default notification service */
		NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(mContext);	
		
		Intent myIntent = new Intent(mContext, Driving_Temp_Off.class);
		PendingIntent pendingIntent = PendingIntent.getBroadcast(mContext, 0, myIntent, PendingIntent.FLAG_CANCEL_CURRENT);
		
		long[] pattern = {0, 500};
		
		mBuilder.setContentTitle("Text Secretary: Driving")
				.setContentText("Text Secretary detects that you are driving.")
				.setTicker("Text Secretary: Driving")
				.setSmallIcon(R.drawable.ic_action_notification_holo_light)
				.addAction(0, "Not Driving", pendingIntent)
				.setAutoCancel(false)
				.setVibrate(pattern)
				.setOngoing(true);


        NotificationManager mNotificationManager = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
		
		/* notificationID allows you to update the notification later on. */
		mNotificationManager.notify(11001100 , mBuilder.build());
	}
}
