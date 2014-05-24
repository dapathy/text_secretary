package edu.gonzaga.textsecretary;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

public class RegCheck {
	
	//checks unlock status
	public static boolean isActivated(Context context){
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
				//if paid or in trial
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
