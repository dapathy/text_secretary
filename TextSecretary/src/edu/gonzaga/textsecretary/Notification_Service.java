package edu.gonzaga.textsecretary;

import edu.gonzaga.textsecretary.R.string;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

public class Notification_Service{
	   private NotificationManager mNotificationManager;
	   Context mContext;
	   
	   @SuppressLint("NewApi")
	public void displayNotification(Context cx, String number) {
		      Log.d("TAG", "notification");

		      /* Invoking the default notification service */
		      NotificationCompat.Builder  mBuilder = 
		      new NotificationCompat.Builder(cx);	
		      	mBuilder.setContentTitle("Auto Replied");
		      	mBuilder.setContentText("Text Secretary auto replied to: " + number);
		      	mBuilder.setTicker("Text Secretary Auto Reply");
		      	mBuilder.setSmallIcon(R.drawable.ic_action_notification_holo_light);
		      	mBuilder.setAutoCancel(true);
		      
		      /* Creates an explicit intent for an Activity in your app */
		      Intent resultIntent = new Intent(Intent.ACTION_VIEW, Uri.fromParts("sms", number, null));

		      TaskStackBuilder stackBuilder = TaskStackBuilder.create(cx);
		      //stackBuilder.addParentStack(NotificationView.class);

		      /* Adds the Intent that starts the Activity to the top of the stack */
		      stackBuilder.addNextIntent(resultIntent);
		      PendingIntent resultPendingIntent =
		         stackBuilder.getPendingIntent(
		            0,
		            PendingIntent.FLAG_UPDATE_CURRENT
		         );
		         

		      mBuilder.setContentIntent(resultPendingIntent);
		      

		      mNotificationManager =
		      (NotificationManager) cx.getSystemService(Context.NOTIFICATION_SERVICE);
		      

		      int notificationID = 100;
			/* notificationID allows you to update the notification later on. */
		      mNotificationManager.notify(notificationID , mBuilder.build());
		   }

}
