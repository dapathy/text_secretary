package edu.gonzaga.textsecretary;

import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceCategory;
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

		//set visibility of unlock
		if(((SettingsActivity) getActivity()).isPurchased()){	//queries purchases
    	   PreferenceCategory preferenceCategory = (PreferenceCategory) findPreference("Activation");
    	   getPreferenceScreen().removePreference(preferenceCategory);
		}
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
		if(key.toString().equals("Unlock For Life This product has a 30 day trial.  Purchase the Unlock here.")){
			((SettingsActivity) getActivity()).purchaseUnlock();
		}
		return false;
	}
	
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key){
	    if (key.equals("calendar_preference")){
	        Preference preference = findPreference("custom_message_preference");
	        preference.setEnabled(!sharedPreferences.getBoolean("calendar_preference", false));
	    }
	}
}
