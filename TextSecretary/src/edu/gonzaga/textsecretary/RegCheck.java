package edu.gonzaga.textsecretary;

import java.util.Date;
import java.util.concurrent.ExecutionException;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

public class RegCheck {
	
	//checks unlock status
	public static boolean isActivated(Context context){
		SharedPreferences secureSettings = new SecurePreferences(context);
		String account = UserEmailFetcher.getEmail(context);
		
		//if application paid in shared preferences
		if(secureSettings.getBoolean(account+"_paid", false)){
			return true;
		}
		//if current date is less than date stored
		else if(secureSettings.getLong(account+"_trial", 0) > new Date().getTime()){
			return true;
		}
		//not in shared preferences, so check server
		else{
	        Register task = new Register(context);
	        task.execute(false);
	        try {
				return task.get();
			} catch (InterruptedException | ExecutionException e) {
				Log.e("SMS", "task.get");
				e.printStackTrace();
			}
		}
		return false;
	}
}
