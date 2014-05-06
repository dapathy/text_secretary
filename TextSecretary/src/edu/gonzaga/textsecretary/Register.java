package edu.gonzaga.textsecretary;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

public class Register extends AsyncTask<String, String, String> {
	private Context mContext;
	
    public Register (Context context){
         mContext = context;
    }
	private String user = null;
	private String paid = null;
	private String TAG = "Register";

    // JSON parser class
    JSONParser jsonParser = new JSONParser();

    //testing from a real server:
    private static final String LOGIN_URL = "http://gonzagakennelclub.com/textsecretary/register.php";

    //ids
    private static final String TAG_SUCCESS = "success";
    private static final String TAG_MESSAGE = "message";
    private static final String TAG_PAID = "paid";
    private static final String TAG_DATE = "trialEND";

	boolean failure = false;


	@Override
	protected String doInBackground(String... args) {
        int success;
        user = "9364467121";
        paid = "1";
        
        try {
            // Building Parameters
            List<NameValuePair> params = new ArrayList<NameValuePair>();
            params.add(new BasicNameValuePair("userIDD", user));
            params.add(new BasicNameValuePair("just_paid", paid));
            
            Log.d(TAG, "starting");

            //Posting user data to script
            JSONObject json = null;
			json = jsonParser.makeHttpRequest(
				       LOGIN_URL, "POST", params);


            Log.d(TAG, "connected");

            // full json response
            Log.d(TAG, "JSON response" + json.toString());

            // json success element
            success = json.getInt(TAG_SUCCESS);
            if (success == 1) {
            	//Log.d(TAG, "Success " + json.toString());
            	return (json.getString(TAG_SUCCESS) + " " + json.getString(TAG_PAID) + " " + json.getString(TAG_DATE));
            	}
            else if(success == 2){
            	//Log.d(TAG, "Success " + json.toString());
            	return (json.getString(TAG_SUCCESS) + " " + json.getString(TAG_PAID) + " " + json.getString(TAG_DATE));
           	}
            else{
            	//Log.d(TAG, "Failure " +json.getString(TAG_MESSAGE));
            	return (json.getString(TAG_SUCCESS) + " " + json.getString(TAG_PAID) + " " + json.getString(TAG_DATE));
            }
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
        // dismiss the dialog once product deleted
        if (file_url != null){
        	Log.d(TAG, "onPostExecute: " + file_url);
        	Toast.makeText(mContext, file_url, Toast.LENGTH_LONG).show();
        }

    }


}