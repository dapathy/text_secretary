package edu.gonzaga.textsecretary;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;

import android.app.ListFragment;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class ServiceListFragment extends ListFragment{
      ArrayAdapter<String> adapter;
      
	  @Override
	  public void onActivityCreated(Bundle savedInstanceState) {
	    super.onActivityCreated(savedInstanceState);
	    
	    String[] values = new String[] { "There has been an error"};
	    	
	    values = setString();
	    
	    ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(),
	        android.R.layout.simple_list_item_1, values);
	    
	    setListAdapter(adapter);
	  }
	  
	  @Override
	  public void onListItemClick(ListView l, View v, int position, long id) {
			startActivity(new Intent(getActivity(), SettingsActivity.class));
	  }
	  
	  public void onResume(){
		  super.onResume();
		  //adapter.notifyDataSetChanged();
	  }

	  private String[] setString(){
		  	String[] values;
		    ArrayList<String> settingValues = new ArrayList<String>();
	    	SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
	    	
	    	//Calendar
			if(prefs .getBoolean("calendar_preference", true))
	    		settingValues.add("Calendar ON");
			else
				settingValues.add("Calendar OFF");
			
			//Sleep
			if(prefs .getBoolean("sleep_timer_preference", true)){
	    		settingValues.add("Sleep Timer ON");
	    		settingValues.add("Sleep Length " + prefs.getString("list_preference", "1800000"));
			}
			
			else
				settingValues.add("Sleep Timer OFF");
			
	    	//Notification
			if(prefs .getBoolean("notification_preference", true))
	    		settingValues.add("Notifications ON");
			else
				settingValues.add("Notifications OFF");
			
	    	//Start on Boot
			if(prefs .getBoolean("start_on_boot_preference", true))
	    		settingValues.add("Start Service on Boot ON");
			else
				settingValues.add("Start Service on Boot OFF");
			
			values = settingValues.toArray(new String[settingValues.size()]);
		  	return values;  
	  }
}
