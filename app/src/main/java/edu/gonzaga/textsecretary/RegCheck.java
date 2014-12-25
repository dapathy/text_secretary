package edu.gonzaga.textsecretary;

import java.util.Date;
import java.util.concurrent.ExecutionException;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

public class RegCheck {
	
	private final static String TAG = "REGCHECK";
    private final static long NUM_MILLIS_IN_YEAR = 31556952000L;
	
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
				Log.e(TAG, "task.get failed");
				e.printStackTrace();
			}
		}
		return false;
	}

    public static int getTrialDaysRemaining(Context context) {
        SharedPreferences secureSettings = new SecurePreferences(context);
        String account = UserEmailFetcher.getEmail(context);
        long millisLeft = secureSettings.getLong(account+"_trial", 0) - new Date().getTime();   //difference equals millis left in trial
        int daysLeft = (int)(millisLeft / NUM_MILLIS_IN_YEAR);    //divide by number of millis in a year
        //if negative number, then trial over so return a 0
        if (daysLeft < 0)
            return 0;
        else
            return daysLeft;
    }
}
