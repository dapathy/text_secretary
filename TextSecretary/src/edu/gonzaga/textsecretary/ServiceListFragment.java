package edu.gonzaga.textsecretary;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import android.app.ListFragment;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.ListView;
import android.widget.SimpleAdapter;

public class ServiceListFragment extends ListFragment{
	  private SimpleAdapter adapter;
      private ArrayList<Map<String, String>> list;

      
	  @Override
	  public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		buildData();
		
		String[] from = { "name", "purpose" };
		int[] to = { android.R.id.text1, android.R.id.text2 };
		
		adapter = new SimpleAdapter(getActivity(), list,
		        //android.R.layout.simple_list_item_2, from, to);
				R.layout.list_fragment_layout, from, to);
		setListAdapter(adapter);
		Log.d("TAG" , "set adapter");
	  }
	  
	  private ArrayList<Map<String, String>> buildData() {
	    	SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());	  
	        list = new ArrayList<Map<String, String>>();
        	
	        //respond to
	        String respondTo = prefs.getString("respond_to_preference", "2");
	        switch(respondTo){
	        case "0":
	        	list.add(putData("Replying to", "Texts"));
	        	break;
        	case "1":
        		list.add(putData("Replying to", "Calls"));
        		break;
        	case "2":
        		list.add(putData("Replying to", "Calls"));
        		break;
        	case "3":
        		list.add(putData("Replying to", "Texts & Calls"));
        		break;
        	case "4":
        		list.add(putData("Replying to", "Texts & Calls"));
        		break;
	        }
	        
	    	
		    //Calendar
		    if(prefs.getBoolean("calendar_preference", true))
		    	list.add(putData("Calendar", "ON"));
		    else
		    	list.add(putData("Calendar", "OFF"));
		    
		    //Start on Boot
			if(prefs.getBoolean("start_on_boot_preference", false))
				list.add(putData("Start on Boot", "ON"));
			else
				list.add(putData("Start on Boot", "OFF"));

	    	//Notification
			if(prefs.getBoolean("notification_preference", true))
				list.add(putData("Notifications", "ON"));
			else
				list.add(putData("Notifications", "OFF"));
			
			//Sleep
			if(prefs.getBoolean("sleep_timer_preference", true)){
				list.add(putData("Sleep Timer", "ON"));
				list.add(putData("Sleep Length", Long.valueOf(prefs.getString("list_preference", "1800000"))/60000 + " minutes"));
				
				if(prefs.getBoolean("smart_sent_message", true))
					list.add(putData("Smart Sent Message", "ON"));
				else
					list.add(putData("Smart Sent Message", "OFF"));
			}
			else
				list.add(putData("Sleep", "OFF"));
			
		    return list;
		  }

	  private HashMap<String, String> putData(String name, String purpose) {
	    HashMap<String, String> item = new HashMap<String, String>();
	    item.put("name", name);
	    item.put("purpose", purpose);
	    return item;
	  }
		  
	  @Override
	  public void onListItemClick(ListView l, View v, int position, long id) {
			startActivity(new Intent(getActivity(), SettingsActivity.class));
	  }
	  
	  @Override
	  public void onResume(){
		  super.onResume();
		  
		  String[] from = { "name", "purpose" };
		  int[] to = { android.R.id.text1, android.R.id.text2 };
		  list.clear();
		  Log.d("TAG", "cleared adapter");
		  buildData();
		  Log.d("TAG", "setValues");
	    
		  adapter = new SimpleAdapter(getActivity(), list,
		            R.layout.list_fragment_layout, from, to);
		    
		  setListAdapter(adapter);
	  }

}
