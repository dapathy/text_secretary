package edu.gonzaga.textsecretary;

import edu.gonzaga.textsecretary.activity_recognition.ActivityRecognizer;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;

public class PrefFrag extends PreferenceFragment implements OnSharedPreferenceChangeListener{
		
	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.preferences);
		
		//set message disability based on calendar selection
		if(getPreferenceManager().getSharedPreferences().getBoolean("calendar_preference", true)){
			Preference messagePreference = findPreference("custom_message_preference");
	        messagePreference.setEnabled(false);
		}

        //change unlock message text
        Preference unlockPreference = findPreference("unlock");
        unlockPreference.setSummary("There are " + RegCheck.getTrialDaysRemaining(getActivity().getApplicationContext()) + " days remaining in your trial. After the trial, a tagline will be appended to every auto-reply. Purchase the Unlock here to remove the tagline.");
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
	
	public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference key){
		//this is ridiculous
		if(key.toString().equals("This product has a 30 day trial. After the trial, a tagline will be appended to every auto-reply. Purchase the Unlock here to remove the tagline.")){
			((SettingsActivity) getActivity()).purchaseUnlock();
		}
		return false;
	}
	
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key){
	    if (key.equals("calendar_preference")){
	        Preference messagePreference = findPreference("custom_message_preference");
	        messagePreference.setEnabled(!sharedPreferences.getBoolean("calendar_preference", false));
	    }
	    else if (key.equals("driving_preference")){
	    	//starts or stops services when selected or deselected on preference screen and sms service is already enabled
	    	if (sharedPreferences.getBoolean("smsState", false)) {
	    		if (sharedPreferences.getBoolean("driving_preference", false))
	    			ActivityRecognizer.startUpdates(getActivity().getApplicationContext());
	    		else
	    			ActivityRecognizer.stopUpdates();
	    	}	    		
	    }
	}
}
