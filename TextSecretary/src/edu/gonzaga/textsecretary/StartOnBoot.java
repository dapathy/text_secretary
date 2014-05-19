package edu.gonzaga.textsecretary;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

public class StartOnBoot extends BroadcastReceiver{
	
	@Override
	public void onReceive(Context context, Intent intent) {
		if ("android.intent.action.BOOT_COMPLETED".equals(intent.getAction())) {
			
			SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
			
			//if boot is enabled and product is activated
	    	if(settings.getBoolean("start_on_boot_preference", false) && isActivated(context)){
	    		Intent smsService = new Intent(context, SMS_Service.class);
				context.startService(smsService);
				
				SharedPreferences.Editor editor = settings.edit();
		    	editor.putBoolean("smsState", true);
		    	editor.commit();
	    	}
		}
	}
	
	//checks unlock status then registers receiver
	private boolean isActivated(Context context){
		SharedPreferences secureSettings = new SecurePreferences(context);
		String account = UserEmailFetcher.getEmail(context);
		
		//if application not paid in shared preferences
		if(secureSettings.getBoolean(account+"_paid", false)){
			return true;
		}
		//not in shared preferences, so check server
		else{
	        Register task = new Register(context, false);
	        task.execute();
	        try {
				task.get(1000, TimeUnit.MILLISECONDS);	//wait for async to finish
				//if paid or in trial, start service
				if(task.isInTrial())
					return true;
			} catch (InterruptedException | ExecutionException
					| TimeoutException e) {
				Log.e("SMS", "task.get");
				e.printStackTrace();
			}
		}
		return false;
	}

}
