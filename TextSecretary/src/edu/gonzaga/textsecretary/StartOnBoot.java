package edu.gonzaga.textsecretary;

import edu.gonzaga.textsecretary.activity_recognition.ActivityRecognizer;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

public class StartOnBoot extends BroadcastReceiver{
	
	private static final String TAG = "BOOT";
	
	@Override
	public void onReceive(Context context, Intent intent) {
		if ("android.intent.action.BOOT_COMPLETED".equals(intent.getAction())) {
			Log.d(TAG, "boot completed");
			SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
			SharedPreferences.Editor editor = settings.edit();
			
			//if boot is enabled
	    	if(settings.getBoolean("start_on_boot_preference", false)){
	    		//if activated, start service
	    		if (RegCheck.isActivated(context)) {
	    			Log.d(TAG, "auto start");
		    		Intent smsService = new Intent(context, SMS_Service.class);
					context.startService(smsService);				
			    	editor.putBoolean("smsState", true);
			    	
			    	if (settings.getBoolean("driving_preference", false))
	        			context.stopService(new Intent(context, ActivityRecognizer.class));
	    		}
	    		//otherwise open app to display trial over dialogue
	    		else {
	    			Log.d(TAG, "opening main");
	    			Intent activity = new Intent(context, MainActivity.class);
	    			activity.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
	    	        context.startActivity(activity);
	    			editor.putBoolean("smsState", false);
	    		}
	    	}
	    	else{
	    		editor.putBoolean("smsState", false);
	    	}
	    	editor.commit();
		}
	}
	


}
