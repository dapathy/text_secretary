package edu.gonzaga.textsecretary;

import java.util.concurrent.ExecutionException;

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
				return task.get();
			} catch (InterruptedException | ExecutionException e) {
				Log.e("SMS", "task.get");
				e.printStackTrace();
			}
		}
		return false;
	}
}
