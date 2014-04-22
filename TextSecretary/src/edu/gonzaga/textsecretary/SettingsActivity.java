package edu.gonzaga.textsecretary;

import android.annotation.TargetApi;
import android.app.ListActivity;
import android.app.ListFragment;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceActivity;

@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class SettingsActivity extends PreferenceActivity {
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		getFragmentManager().beginTransaction().replace(android.R.id.content, new PrefFrag()).commit();
	}
	
	@Override
	protected void onStop(){
    	super.onStop();
	}
}