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
			
			SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
			
			//if boot is enabled and product is activated
	    	if(settings.getBoolean("start_on_boot_preference", false) && RegCheck.isActivated(context)){
	    		Intent smsService = new Intent(context, SMS_Service.class);
				context.startService(smsService);
				
				SharedPreferences.Editor editor = settings.edit();
		    	editor.putBoolean("smsState", true);
		    	editor.commit();
	    	}
		}
	}
	


}
