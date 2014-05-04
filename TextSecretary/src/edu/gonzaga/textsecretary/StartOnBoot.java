package edu.gonzaga.textsecretary;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class StartOnBoot extends BroadcastReceiver{

	@Override
	public void onReceive(Context context, Intent intent) {
		if ("android.intent.action.BOOT_COMPLETED".equals(intent.getAction())) {
			Intent pushIntent = new Intent(context, SMS_Service.class);
			
			SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
	    	SharedPreferences.Editor editor = settings.edit();
			
	    	if(settings.getBoolean("start_on_boot_preference", true)){
				context.startService(pushIntent);
		    	editor.putBoolean("smsState", true);
		    	editor.commit();
	    	}
		}
	}

}
