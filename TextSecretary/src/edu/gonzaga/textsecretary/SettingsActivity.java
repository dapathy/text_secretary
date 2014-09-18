package edu.gonzaga.textsecretary;

import java.util.concurrent.ExecutionException;

import edu.gonzaga.textsecretary.inapp.IabHelper;
import edu.gonzaga.textsecretary.inapp.IabResult;
import edu.gonzaga.textsecretary.inapp.Inventory;
import edu.gonzaga.textsecretary.inapp.Purchase;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.util.Log;

public class SettingsActivity extends PreferenceActivity {
	
	private static final String TAG = "PURCHASE";
	private IabHelper mHelper;
	private Register task;
	private static final String UNLOCK_SKU = "text_secretary_unlock";
	private boolean isUnlocked = false;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		String base64EncodedPublicKey = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEApNk2gDi9RLPvIqu/7bHAHglCv31OY+3JoBYNam6ROslAAT2ZC8TVd3obZKaXkZuU8aa+EP3Br1G210vfQsNvb+nb37z10f5sL3HrKLfxUuqZB9p26El36yAN5AuxAyqNJHH5S5AYcaqelYU3xk6Kj5Z6d701xvoF2V1SbWFGCA9cT5FqWiCcTZ8FSUn/BTHa/zVdJ+coWm6d/VuAzlCmvsMt1oUIzyHk/KBo1x/88BQ7yJw7cYBHd1Ge4EGBI3dTlYR0nM3WJyQj/yTO9pOXmxzp6bPEZCGG5tt/ieFhjlbSN9+nXZSiA9NZuwLmkcVvDflzMzjIRpOgka/9R5xidQIDAQAB";
		
		mHelper = new IabHelper(this, base64EncodedPublicKey);
		
		mHelper.startSetup(new IabHelper.OnIabSetupFinishedListener() {
			   public void onIabSetupFinished(IabResult result) {
			      if (!result.isSuccess()) {
			         Log.d(TAG, "Problem setting up In-app Billing: " + result);
			      }
			      //create fragment if no problems
			      else
			    	  getFragmentManager().beginTransaction().replace(android.R.id.content, new PrefFrag()).commit();
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
	
	//checks if unlock has already been purchased
	protected boolean isPurchased(){
		SharedPreferences securePreferences = new SecurePreferences(getApplicationContext());
		String account = UserEmailFetcher.getEmail(getApplicationContext());
		//check shared preferences first
		if(securePreferences.getBoolean(account+"_paid", false))
			return true;
		
		//else query google services
		else{
			mHelper.queryInventoryAsync(
				new IabHelper.QueryInventoryFinishedListener() {
				   public void onQueryInventoryFinished(IabResult result, Inventory inventory)   
				   {
				      if (result.isFailure()) {
				         // handle error
				    	  Log.e(TAG, "error while checking purchase");
				         return;
				       }

				       if(inventory.hasPurchase(UNLOCK_SKU)) {
				    	   isUnlocked = true;
				    	   return;
				       }
				   }});
			
			return isUnlocked;
		}
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
			         storeActivation();
			      }
			   }}, UserEmailFetcher.getEmail(getApplicationContext()));
	}
	
	//securely stores data locally, then stores on server
	private void storeActivation(){
		//store on server
		task = new Register(getApplicationContext());
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