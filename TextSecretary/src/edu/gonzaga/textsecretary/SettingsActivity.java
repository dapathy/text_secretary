package edu.gonzaga.textsecretary;

import java.util.ArrayList;

import com.android.vending.billing.IInAppBillingService;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.IntentSender.SendIntentException;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.preference.PreferenceActivity;
import android.util.Log;

public class SettingsActivity extends PreferenceActivity {
	
	private final String TAG = "PURCHASE";
	private IInAppBillingService mService;
	private String PACKAGE_NAME;
	private Register task;
	
	ServiceConnection mServiceConn = new ServiceConnection() {
		   @Override
		   public void onServiceDisconnected(ComponentName name) {
		       mService = null;
		   }

		   @Override
		   public void onServiceConnected(ComponentName name, 
		      IBinder service) {
		       mService = IInAppBillingService.Stub.asInterface(service);
		       //create fragment
		       getFragmentManager().beginTransaction().replace(android.R.id.content, new PrefFrag()).commit();
		   }
		};
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		PACKAGE_NAME = getPackageName();
		
		bindService(new 
		        Intent("com.android.vending.billing.InAppBillingService.BIND"),
		                mServiceConn, Context.BIND_AUTO_CREATE);
		Log.d(TAG, "bind");
	}
	
	@Override
	protected void onStop(){
    	super.onStop();
	}
	
	@Override
	public void onDestroy() {
	    super.onDestroy();
	    if (mService != null) {
	    	unbindService(mServiceConn);
	    	Log.d(TAG, "unbind");
	    }   
	}
	
	//checks if unlock has already been purchased
	protected boolean isPurchased(){
		SharedPreferences securePreferences = new SecurePreferences(getApplicationContext());
		String account = UserEmailFetcher.getEmail(getApplicationContext());
		//check shared preferences first
		if(securePreferences.getBoolean(account+"_paid", false))
			return true;
		
		//else query google services
		else{
			try {
				Bundle ownedItems = mService.getPurchases(3, PACKAGE_NAME, "inapp", null);
				Log.d(TAG, "getPurchases");
				int response = ownedItems.getInt("RESPONSE_CODE");
				if (response == 0) {
				   ArrayList<String> ownedSkus = ownedItems.getStringArrayList("INAPP_PURCHASE_ITEM_LIST");

				   if(ownedSkus.isEmpty())	//nothing owned
					   return false;
				   else
					   return true;			//something owned
				}
			} catch (RemoteException e) {
				Log.e(TAG, "query failed");
				Log.e(TAG, e.getMessage());
			}
			return false;
		}
	}
	
	//purchases unlock
	protected void purchaseUnlock(){
		try {
			String payload = UserEmailFetcher.getEmail(getApplicationContext());
			Bundle buyIntentBundle = mService.getBuyIntent(3, PACKAGE_NAME, "text_secretary_unlock", "inapp", payload);
			PendingIntent pendingIntent = buyIntentBundle.getParcelable("BUY_INTENT");
			
			startIntentSenderForResult(pendingIntent.getIntentSender(),
					   1001, new Intent(), Integer.valueOf(0), Integer.valueOf(0),
					   Integer.valueOf(0));
			
			Log.d(TAG, "purchase intent started");
			
		}catch (SendIntentException e) {
			Log.e(TAG, "didn't start activity");
			e.printStackTrace();
		}
		 catch (RemoteException e) {
			Log.e(TAG, "getting intent failed");
			e.printStackTrace();
		}
	}
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {          	        
      Log.d(TAG, "onActivityResult");
		if (resultCode == Activity.RESULT_OK){
         try {
            storeActivation();
            Log.d(TAG, "Purchase completed");
          }
          catch (Exception e) {
             Log.e(TAG, "Failed to parse purchase data.");
             e.printStackTrace();
          }
      }
      else
    	  Log.w(TAG, "purchase didn't happen for some reason");
	}
	
	//securely stores data locally, then stores on server
	private void storeActivation(){
		//store on server
		task = new Register(getApplicationContext());
		task.execute(true);
	}
}