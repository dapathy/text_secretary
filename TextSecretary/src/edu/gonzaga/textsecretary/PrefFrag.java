package edu.gonzaga.textsecretary;

import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;

public class PrefFrag extends PreferenceFragment implements OnSharedPreferenceChangeListener{
	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.preferences);
		
		if(getPreferenceManager().getSharedPreferences().getBoolean("calendar_preference", true)){
			Preference preference = findPreference("custom_message_preference");
	        preference.setEnabled(false);
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
	
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key)
	{
	    if (key.equals("calendar_preference")){
	        Preference preference = findPreference("custom_message_preference");
	        preference.setEnabled(!sharedPreferences.getBoolean("calendar_preference", false));
	    }
	}
}
