package edu.gonzaga.textsecretary;

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
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

public class Register extends AsyncTask<String, String, String> {
	
	private Context mContext;
	private Boolean mPay;
	private String userEmail = null;
	private String paid = null;
	
    public Register (Context context, Boolean pay){
         mContext = context;
         mPay = pay;
    }
    
	private String TAG = "Register";

    // JSON parser class
    JSONParser jsonParser = new JSONParser();

    //testing from a real server:
    private static final String LOGIN_URL = "http://gonzagakennelclub.com/textsecretary/register.php";

    //ids
    //private static final String TAG_SUCCESS = "success";
    private static final String TAG_PAID = "paid";
    private static final String TAG_DATE = "trialEND";

	private boolean failure = false;


	@Override
	protected String doInBackground(String... args) {
		if(mPay)
        	paid = "1";
        else
        	paid = "0";
        
        userEmail = UserEmailFetcher.getEmail(mContext);
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
        	paid = json.getString(TAG_PAID);
        	return(json.getString(TAG_DATE));
        
        } catch (JSONException e) {
        	Log.d(TAG, "CATCH");
            e.printStackTrace();
        }
        catch (Exception e){
        	Log.d(TAG, "CATCH httpRequest " + e.toString());
        }

        return null;
	}

    protected void onPostExecute(String file_url) {
    	Log.d("SDF", paid);
    	if (!paid.equals("1")){
    		boolean inTrial = true;
		    if (file_url != null){
		    	SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
		    	String currentDateandTime = sdf.format(new Date());
		    	inTrial = isInTrialDate(file_url, currentDateandTime);
		    	Log.d(TAG, "onPostExecute: " + file_url + "  " + currentDateandTime);
		    }
		    
		    if(!inTrial){
		    	failure = true;
		    	showTrialOver();
		    }
       }
    }
    
    private boolean isInTrialDate(String endTrial, String currentDate) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
            Date end = sdf.parse(endTrial);
            Date current = sdf.parse(currentDate);

            if(end.compareTo(current)>0){
                Log.v(TAG,"end is after current");
            	Toast.makeText(mContext, "WITHIN TRIAL DATE", Toast.LENGTH_LONG).show();
            	return true;
            }else if(end.compareTo(current)<=0){
                Log.v(TAG,"end is before current");
            	Toast.makeText(mContext, "TRIAL OVER" , Toast.LENGTH_LONG).show();
            	return false;
            }
        } catch(Exception e) {
        	Log.d(TAG, "CATCH compare " + e.toString());
        }
        return true;
        
    }

	//This dialogue informs user that they're period is over
	private void showTrialOver(){
        Intent serviceIntent = new Intent(mContext, SMS_Service.class);
        mContext.stopService(serviceIntent);
		
		new AlertDialog.Builder(mContext)
	    .setTitle("End of Trial Period")
	    .setMessage("You 30 day trial period of Text Secretary is over. Would you like to unlock for life?")
	    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
	        public void onClick(DialogInterface dialog, int which) {
	        	//go to settings intent to purchase
	        	Intent intent = new Intent(mContext, SettingsActivity.class);
	        	mContext.startActivity(intent);
	        }
	     })
	    .setNegativeButton("No", new DialogInterface.OnClickListener() {
	        public void onClick(DialogInterface dialog, int which) {
	        	System.exit(1);
	        }
	     })
	     .show();
	}

	public boolean isFailure() {
		return failure;
	}

}