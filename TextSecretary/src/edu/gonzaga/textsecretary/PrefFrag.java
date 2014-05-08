package edu.gonzaga.textsecretary;

import org.json.JSONException;
import org.json.JSONObject;

import com.android.vending.billing.IInAppBillingService;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender.SendIntentException;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;
import android.util.Log;

public class PrefFrag extends PreferenceFragment implements OnSharedPreferenceChangeListener{
	
	IInAppBillingService mService;
	String PACKAGE_NAME;

	ServiceConnection mServiceConn = new ServiceConnection() {
	   @Override
	   public void onServiceDisconnected(ComponentName name) {
	       mService = null;
	   }

	   @Override
	   public void onServiceConnected(ComponentName name, 
	      IBinder service) {
	       mService = IInAppBillingService.Stub.asInterface(service);
	   }
	};
	
	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.preferences);
		
		if(getPreferenceManager().getSharedPreferences().getBoolean("calendar_preference", true)){
			Preference preference = findPreference("custom_message_preference");
	        preference.setEnabled(false);
		}
		
		PACKAGE_NAME = getActivity().getPackageName();
		getActivity().bindService(new 
		        Intent("com.android.vending.billing.InAppBillingService.BIND"),
		                mServiceConn, Context.BIND_AUTO_CREATE);
	}
	
	@Override
	public void onResume(){
	    super.onResume();
	    getPreferenceManager().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
	}
	@Override
	public void onPause(){
	    super.onPause();
	    getPreferenceManager().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
	}
	
	@Override
	public void onDestroy() {
	    super.onDestroy();
	    if (mService != null) {
	    	getActivity().unbindService(mServiceConn);
	    }   
	}
	
	public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference key){
		if(key.toString().equals("Activate Text Secretary")){
			doPurchaseStuff();
		}
		return false;
	}
	
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key)
	{
	    if (key.equals("calendar_preference")){
	        Preference preference = findPreference("custom_message_preference");
	        preference.setEnabled(!sharedPreferences.getBoolean("calendar_preference", false));
	    }
	}
	
	//TODO: add payload string to identify user
	private void doPurchaseStuff(){
		try {
			Bundle buyIntentBundle = mService.getBuyIntent(3, PACKAGE_NAME, "text_secretary_unlock", "inapp", null);
			PendingIntent pendingIntent = buyIntentBundle.getParcelable("BUY_INTENT");
			
			getActivity().startIntentSenderForResult(pendingIntent.getIntentSender(),
					   1001, new Intent(), Integer.valueOf(0), Integer.valueOf(0),
					   Integer.valueOf(0));
				
		}catch (SendIntentException e) {
			Log.e("PURCHASE", "didn't start activity");
			e.printStackTrace();
		}
		 catch (RemoteException e) {
			Log.e("PURCHASE", "getting intent failed");
			e.printStackTrace();
		}
	}
	
	@Override
	//I'm not sure why this is useful
	public void onActivityResult(int requestCode, int resultCode, Intent data) { 
	   if (requestCode == 1001) {           
	      int responseCode = data.getIntExtra("RESPONSE_CODE", 0);
	      String purchaseData = data.getStringExtra("INAPP_PURCHASE_DATA");
	      String dataSignature = data.getStringExtra("INAPP_DATA_SIGNATURE");
	        
	      if (resultCode == Activity.RESULT_OK) {
	         try {
	            JSONObject jo = new JSONObject(purchaseData);
	            String sku = jo.getString("productId");
	            Log.d("PURCHASE", "You have bought the " + sku + ". Excellent choice, adventurer!");
	          }
	          catch (JSONException e) {
	             Log.e("PURCHASE", "Failed to parse purchase data.");
	             e.printStackTrace();
	          }
	      }
	   }
	}
}
