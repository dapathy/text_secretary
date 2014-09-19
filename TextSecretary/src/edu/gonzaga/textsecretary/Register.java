package edu.gonzaga.textsecretary;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.util.Log;

public class Register extends AsyncTask<Boolean, Void, Boolean> {
	
	private static final String TAG = "REGISTER";
	private Context mContext;
	private boolean problemHappenned = false;
	
    // JSON parser class
    JSONParser jsonParser = new JSONParser();

    //testing from a real server:
    private static final String LOGIN_URL = "http://gonzagakennelclub.com/textsecretary/register.php";

    //ids
    private static final String TAG_PAID = "paid";
    private static final String TAG_DATE = "trialEND";
	
    public Register (Context context){
         mContext = context;
    }

	@Override
	protected Boolean doInBackground(Boolean... pay) {
		String paid;
		String date = null;
		String userEmail = UserEmailFetcher.getEmail(mContext);
		String serverPay = "0";
		
		if(pay[0]) {
			storeActivation(mContext);	//storing here is probably a little safer
        	paid = "1";
		}
        else
        	paid = "0";
        
        try {
            // Building Parameters
            List<NameValuePair> params = new ArrayList<NameValuePair>();
            params.add(new BasicNameValuePair("userIDD", userEmail));
            params.add(new BasicNameValuePair("just_paid", paid));
            
            Log.d(TAG, "starting");

            //Posting user data to script
            JSONObject json = null;
			json = jsonParser.makeHttpRequest(
				       LOGIN_URL, "POST", params);

            Log.d(TAG, "connected");
            Log.d(TAG, "JSON response" + json.toString());
            
            //retrieve values
        	serverPay = json.getString(TAG_PAID);
        	date = json.getString(TAG_DATE);
        	
        	return checkActivation(date, serverPay);
        
        } catch (JSONException e) {
        	Log.d(TAG, "CATCH");
            e.printStackTrace();
        }
        catch (Exception e){
        	Log.d(TAG, "CATCH httpRequest " + e.toString());
        }
        
        problemHappenned = true;
        return false;
	}
	
	protected void onPostExecute(Boolean result){
		//if not in trial and not paid, then show dialog
	    if(!result) {
	    	if (problemHappenned)
	    		showErrorDialogue();
	    	else
	    		showTrialOver();
	    }
	}
    
    private boolean checkActivation(String date, String serverPay){
    	Log.d(TAG, serverPay);
    	//if not paid
    	if (!serverPay.equals("1")){
    		boolean inTrial = false;
		    if (date != null){
		    	SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
		    	String currentDateandTime = sdf.format(new Date());
		    	Log.d(TAG, date + "  " + currentDateandTime);
		    	inTrial =  isInTrialDate(date, currentDateandTime);
		    	if(inTrial)
		    		storeTrialInfo(date);
		    }
		    
		    return inTrial;
       }
    	//must have paid
    	else{
    		Log.d(TAG, "paid");
    		return true;
    	}
    }
    
    //securely stores trial information
    private void storeTrialInfo(String endTrial){
    	SharedPreferences secureSettings = new SecurePreferences(mContext);
    	String account = UserEmailFetcher.getEmail(mContext);
    	
    	//if date preference doesn't exist, create one
    	if(secureSettings.getLong(account+"_trial", 0) == 0){
    		try{
	    		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
	            Date endDate = sdf.parse(endTrial);
	    		
	    		SharedPreferences.Editor secureEdit = secureSettings.edit();
				secureEdit.putLong(account+"_trial", endDate.getTime());
				secureEdit.commit();
    		}
    		catch(ParseException e){
    			Log.e(TAG, "parse exception");
    			Log.e(TAG, e.getMessage());
    		}
    	}
    }
    
	//securely stores data locally
	private static void storeActivation(Context context){
		//securely store in shared preference
		SharedPreferences secureSettings = new SecurePreferences(context);
		String account = UserEmailFetcher.getEmail(context);
		
		//update preference
		SharedPreferences.Editor secureEdit = secureSettings.edit();
		secureEdit.putBoolean(account+"_paid", true);
		secureEdit.commit();
	}
    
    private static boolean isInTrialDate(String endTrial, String currentDate) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
            Date end = sdf.parse(endTrial);
            Date current = sdf.parse(currentDate);

            if(end.compareTo(current)>0){
                Log.v(TAG,"end is after current");
            	return true;
            }else if(end.compareTo(current)<=0){
                Log.v(TAG,"end is before current");
            	return false;
            }
        } catch(Exception e) {
        	Log.d(TAG, "CATCH compare " + e.toString());
        }
        return false;
    }
    
    //shows error if server is not responding correctly
    private void showErrorDialogue() {
    	try {
	    	new AlertDialog.Builder(mContext)
		    .setTitle("Something unfortunate happenned...")
		    .setMessage("Your device was not able to launch the app correctly.  Please check your internet connection and restart the app.")
		    .setNeutralButton("Exit application", new DialogInterface.OnClickListener() {
		        public void onClick(DialogInterface dialog, int which) {
		        	System.exit(1);
		        }
		     })
		     .show();
    	} catch (Exception e) {
    		//not in main activity, not a problem
			Log.w(TAG, e.getMessage());
    	}
    }

	//This dialogue informs user that they're period is over
	private void showTrialOver(){
		try{
	        Intent serviceIntent = new Intent(mContext, SMS_Service.class);
	        mContext.stopService(serviceIntent);
	
			new AlertDialog.Builder(mContext)
		    .setTitle("End of Trial Period")
		    .setMessage("You 30 day trial period of Text Secretary is over. Please go to the bottom of the settings page to purchase the unlock.")
		    .setPositiveButton("Go to Settings", new DialogInterface.OnClickListener() {
		        public void onClick(DialogInterface dialog, int which) {
		        	//go to settings intent to purchase
		        	Intent intent = new Intent(mContext, SettingsActivity.class);
		        	mContext.startActivity(intent);
		        }
		     })
		    .setNegativeButton("Exit application", new DialogInterface.OnClickListener() {
		        public void onClick(DialogInterface dialog, int which) {
		        	System.exit(1);
		        }
		     })
		     .show();
		} catch(Exception e){
			//not in main activity, not a problem
			Log.w(TAG, e.getMessage());
		}
	}

}