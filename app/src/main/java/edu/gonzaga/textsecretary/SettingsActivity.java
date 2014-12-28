package edu.gonzaga.textsecretary;

import java.util.concurrent.ExecutionException;

import edu.gonzaga.textsecretary.inapp.IabHelper;
import edu.gonzaga.textsecretary.inapp.IabResult;
import edu.gonzaga.textsecretary.inapp.Inventory;
import edu.gonzaga.textsecretary.inapp.Purchase;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceCategory;
import android.preference.PreferenceFragment;
import android.util.Log;

public class SettingsActivity extends PreferenceActivity {
	
	private static final String TAG = "PURCHASE";
	private IabHelper mHelper;
	private static final String UNLOCK_SKU = "text_secretary_unlock";
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		//create fragment
		getFragmentManager().beginTransaction().replace(android.R.id.content, new PrefFrag()).commit();
		
		String base64EncodedPublicKey = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEApNk2gDi9RLPvIqu/7bHAHglCv31OY+3JoBYNam6ROslAAT2ZC8TVd3obZKaXkZuU8aa+EP3Br1G210vfQsNvb+nb37z10f5sL3HrKLfxUuqZB9p26El36yAN5AuxAyqNJHH5S5AYcaqelYU3xk6Kj5Z6d701xvoF2V1SbWFGCA9cT5FqWiCcTZ8FSUn/BTHa/zVdJ+coWm6d/VuAzlCmvsMt1oUIzyHk/KBo1x/88BQ7yJw7cYBHd1Ge4EGBI3dTlYR0nM3WJyQj/yTO9pOXmxzp6bPEZCGG5tt/ieFhjlbSN9+nXZSiA9NZuwLmkcVvDflzMzjIRpOgka/9R5xidQIDAQAB";
		mHelper = new IabHelper(this, base64EncodedPublicKey);
		
		mHelper.startSetup(new IabHelper.OnIabSetupFinishedListener() {
			   public void onIabSetupFinished(IabResult result) {
			      if (!result.isSuccess()) {
			    	  Log.d(TAG, "Problem setting up In-app Billing: " + result);
			    	  removeUnlockItem();	//remove this because this wouldn't work if in app billing is screwed
			      }
			      
			      else
			    	  isPurchased();	//checks unlock status and removes unlock item if necessary
			   }
			});
	}
	
	@Override
	public void onDestroy() {
	    super.onDestroy();
	    
	    if (mHelper != null)
	    	mHelper.dispose();
	    mHelper = null;
	}
	
	//checks if unlock has already been purchased and remove unlock item if so
	protected void isPurchased(){
		SharedPreferences securePreferences = new SecurePreferences(getApplicationContext());
		String account = UserEmailFetcher.getEmail(getApplicationContext());
		//check shared preferences first
		if(securePreferences.getBoolean(account+"_paid", false))
			removeUnlockItem();
		
		//else query google services
		else{
			mHelper.queryInventoryAsync(
				new IabHelper.QueryInventoryFinishedListener() {
				   public void onQueryInventoryFinished(IabResult result, Inventory inventory)   
				   {
				      if (result.isFailure()) {
				    	 Log.e(TAG, "error while checking purchase");
				       }

				      else if (inventory.hasPurchase(UNLOCK_SKU)){
				    	  Log.d(TAG, "purchased on google play");
				    	  removeUnlockItem();
				       }
				   }});
		}
	}
	
	//remove purchase option from preference fragment
	private void removeUnlockItem() {
		PreferenceFragment preferenceFragment = (PreferenceFragment) getFragmentManager().findFragmentById(android.R.id.content);
		PreferenceCategory activationCategory = (PreferenceCategory) preferenceFragment.findPreference("Activation");
		preferenceFragment.getPreferenceScreen().removePreference(activationCategory);
	}
	
	//purchases unlock
	protected void purchaseUnlock(){
		mHelper.launchPurchaseFlow(this, UNLOCK_SKU, 10001,   
			new IabHelper.OnIabPurchaseFinishedListener() {
			   public void onIabPurchaseFinished(IabResult result, Purchase purchase) 
			   {
			      if (result.isFailure()) {
			         Log.d(TAG, "Error purchasing: " + result);
			         return;
			      }
			      //purchase successful!
			      else if (purchase.getSku().equals(UNLOCK_SKU)) {
			    	  Log.d(TAG, "google play purchase successful");
			    	  storeActivation();
			    	  removeUnlockItem();
			      }
			   }}, UserEmailFetcher.getEmail(getApplicationContext()));
	}
	
	@Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d(TAG, "onActivityResult(" + requestCode + "," + resultCode + "," + data);
        if (mHelper == null) return;

        // Pass on the activity result to the helper for handling
        if (!mHelper.handleActivityResult(requestCode, resultCode, data)) {
            // handling of non in app billing stuff
            super.onActivityResult(requestCode, resultCode, data);
        }
        else {
            Log.d(TAG, "onActivityResult handled by IABUtil.");
        }
    }
	
	//securely stores data locally, then stores on server
	private void storeActivation(){
		//store on server
        Register task = new Register(getApplicationContext());
		task.execute(true);
		
		//retry task until true or 5 times tried
		try {
			int count = 0;
			
			while (!task.get() && count < 5) {
				task = new Register(getApplicationContext());
				task.execute(true);
				count++;
			}
		} catch (InterruptedException | ExecutionException e) {
			e.printStackTrace();
		}
	}
}