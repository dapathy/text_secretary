package edu.gonzaga.textsecretary;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

public class DrivingNotification extends IntentService{
	private static final String TAG = "NOTIFICATION";
	private NotificationManager mNotificationManager;
	private Context mContext;

    public DrivingNotification() { 
        super("DrivingNotification"); 
    } 
	
	public void displayNotification(int activityType) {
		Log.d(TAG, "notification");
				
		/* Invoking the default notification service */
		NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(mContext);	
		
		Intent myIntent = new Intent(mContext, Driving_Temp_Off.class);
		PendingIntent pendingIntent = PendingIntent.getBroadcast(mContext, 0, myIntent, PendingIntent.FLAG_CANCEL_CURRENT);
		
		long[] pattern = {0, 100, 1000, 300, 200, 100, 500, 200, 100};
		
		mBuilder.setContentTitle("Text Secretary: Driving")
				.setContentText("Text Secretary detects that you are driving.")
				.setTicker("Text Secretary: Driving")
				.setSmallIcon(R.drawable.ic_action_notification_holo_light)
				.addAction(0, "Not Driving", pendingIntent)
				.setAutoCancel(true)
				.setVibrate(pattern);
	         
		mNotificationManager = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
		
		/* notificationID allows you to update the notification later on. */
		mNotificationManager.notify(11001100 , mBuilder.build());
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		// TODO Auto-generated method stub
		mContext = getApplicationContext();
        Toast toast = Toast.makeText(mContext, "handeling", Toast.LENGTH_LONG);
        toast.show();
		
		displayNotification(0);
	}

}
