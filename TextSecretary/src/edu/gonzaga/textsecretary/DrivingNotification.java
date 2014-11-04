package edu.gonzaga.textsecretary;

import android.app.NotificationManager;
import android.content.Context;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

public class DrivingNotification{
	private static final String TAG = "NOTIFICATION";
	private NotificationManager mNotificationManager;
	private Context mContext;
	   
	public DrivingNotification (Context context){
		mContext = context;
	}

	public void displayNotification(int activityType) {
		Log.d(TAG, "notification");
				
		/* Invoking the default notification service */
		NotificationCompat.Builder  mBuilder = new NotificationCompat.Builder(mContext);	
		
		mBuilder.setContentTitle("Text Secretary: Driving");
		mBuilder.setContentText("Activity state: " + activityType);
		mBuilder.setTicker("Text Secretary Driving Update");
		mBuilder.setSmallIcon(R.drawable.ic_action_notification_holo_light);
		mBuilder.setAutoCancel(true);
	         
		mNotificationManager = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
		
		/* notificationID allows you to update the notification later on. */
		mNotificationManager.notify(0 , mBuilder.build());
	}

}
