package edu.gonzaga.textsecretary;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import edu.gonzaga.textsecretary.activity_recognition.ActivityRecognizer;

//Receiver
		public class Driving_Temp_Off extends BroadcastReceiver {
	
			@Override
			public void onReceive(Context context, Intent intent) {				
				NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
				manager.cancel(11001100);
				
				Log.d("NOTIFICATION", "BROADCAST RECEIVED");

		        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
		        SharedPreferences.Editor editor = settings.edit();
	        	editor.putBoolean("isPassenger", true);
	        	editor.apply();
			}

	}