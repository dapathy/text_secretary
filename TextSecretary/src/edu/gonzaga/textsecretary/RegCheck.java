package edu.gonzaga.textsecretary;

import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.ExecutionException;

import com.android.vending.billing.IInAppBillingService;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

public class RegCheck {
	
	private static final String TAG = "REGCHECK";
	
	private IInAppBillingService mService;
	private ServiceConnection mServiceConn = new ServiceConnection() {
		
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
	
	
	//checks unlock status
	private boolean isActivatedHelper(Context context){
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
				boolean serverActivation = task.get();
				boolean googlePurchase = isPurchasedGoogle(context);
				
				if (!serverActivation && googlePurchase)
					new Register(context).execute(true);	//purchased in Google, so ensure purchased on server
				
				return googlePurchase;
			} catch (InterruptedException | ExecutionException e) {
				Log.e("SMS", "task.get");
				e.printStackTrace();
			}
		}
		return false;
	}
	
	private boolean isPurchasedGoogle(Context context) {
		String packageName = context.getPackageName();
		
		context.bindService(new 
		        Intent("com.android.vending.billing.InAppBillingService.BIND"),
		                mServiceConn, Context.BIND_AUTO_CREATE);
		
		try {
			Bundle ownedItems = mService.getPurchases(3, packageName, "inapp", null);
			Log.d(TAG, "getPurchases");
			int response = ownedItems.getInt("RESPONSE_CODE");
			if (response == 0) {
			   ArrayList<String> ownedSkus = ownedItems.getStringArrayList("INAPP_PURCHASE_ITEM_LIST");

			   return !ownedSkus.isEmpty();		//true if not empty (something owned)
			}
		} catch (RemoteException e) {
			Log.e(TAG, "query failed");
			Log.e(TAG, e.getMessage());
		}
		return false;
	}
	
	public static boolean isActivated(Context context) {
		RegCheck checker = new RegCheck();
		return checker.isActivatedHelper(context);
	}
}
