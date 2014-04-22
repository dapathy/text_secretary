package edu.gonzaga.textsecretary;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.ListFragment;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.RemoteViews;

public class MainActivity extends Activity {
	
	String TAG = "TAG";
	String customMessage;
	Boolean SMS_Service_State = true;
	Boolean remindToggleDialogue;
	RelativeLayout lowerBar, lowerHalf, listFragment;
	ImageButton imageState;
	Animation out;
	EditText custom;
	SharedPreferences settings;
	RemoteViews remoteViews;
	ComponentName widget;
	AppWidgetManager appWidgetManager;
	ListFragment serviceListFragment;
	FragmentManager fragmentManager;
	FragmentTransaction fragmentTransaction;
	ServiceListFragment myFragment;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
        settings = PreferenceManager.getDefaultSharedPreferences(getBaseContext());

        SMS_Service_State = settings.getBoolean("smsState", true);
		
        //GUI stuff
		lowerBar = (RelativeLayout) findViewById(R.id.bottomBar);
		lowerHalf = (RelativeLayout) findViewById(R.id.bottomHalf);
		listFragment = (RelativeLayout) findViewById(R.id.listFragmentLayout);
		imageState = (ImageButton) findViewById(R.id.stateImage);
		imageState.setOnClickListener(imgButtonHandler);
		
		//WidgetStuff
        remoteViews = new RemoteViews(getBaseContext().getPackageName(), R.layout.widgetlayout);
        widget = new ComponentName(getBaseContext(), Widget.class);
        appWidgetManager = AppWidgetManager.getInstance(getBaseContext());

        //Set the on click listener for the custom message
		custom = (EditText) findViewById(R.id.customMessage);
		custom.setOnClickListener(new View.OnClickListener() {
	        @Override
	        public void onClick(View v) {
				startActivity(new Intent(getApplicationContext(), SettingsActivity.class));
	        }

	    });

		// Put the custom Message in the Edit Text
        customMessage = settings.getString("custom_message_preference", "You can change custom message in Settings");
        custom.setText(customMessage.toString());
        
        // get an instance of FragmentTransaction from your Activity
        fragmentManager = getFragmentManager();
        fragmentTransaction = fragmentManager.beginTransaction();

        //add a fragment
        myFragment = new ServiceListFragment();
        fragmentTransaction.add(R.id.listFragmentLayout, myFragment);
        fragmentTransaction.commit();

        //Remind users how to use toggle
		remindToggleDialogue = settings.getBoolean("remindToggleDialogue", true);
		if(remindToggleDialogue)
			showToggleDialogue();
	}
	
	
	View.OnClickListener imgButtonHandler = new View.OnClickListener() {
	    @SuppressLint("ResourceAsColor")
		public void onClick(View v) {
			if(SMS_Service_State == true){			//if service is on -> turn off
				stopService();
		        SMS_Service_State = false;
		        lowerBar.setBackgroundResource(R.drawable.lowbaroff);
		        imageState.setImageResource(R.drawable.button_off);
	        	remoteViews.setImageViewResource(R.id.imageview_icon, R.drawable.widgetoff);
		        jiggleLayout(lowerBar);
		        jiggleLayout(lowerHalf);
		        jiggleLayout(listFragment);
		        appWidgetManager.updateAppWidget(widget, remoteViews);
			}
			else{						//else service is off -> turn on
				startService();
		        SMS_Service_State = true;
		        lowerBar.setBackgroundResource(R.drawable.lowbaron);
		        imageState.setImageResource(R.drawable.button_on);
	        	remoteViews.setImageViewResource(R.id.imageview_icon, R.drawable.widgeton);
		        jiggleLayout(lowerBar);
		        jiggleLayout(lowerHalf);
		        jiggleLayout(listFragment);
		        appWidgetManager.updateAppWidget(widget, remoteViews);
			}

	    }
	};

    @Override
    protected void onStop(){
    	super.onStop();
    	// We need an Editor object to make preference changes.
    	// All objects are from android.context.Context
        settings = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
    	SharedPreferences.Editor editor = settings.edit();
    	editor.putBoolean("smsState", SMS_Service_State);

    	// Commit the edits!
    	editor.commit();
    	fragmentTransaction.remove(myFragment);
    }
    
    @Override
    protected void onResume(){
    	super.onStop();
    	// We need an Editor object to make preference changes.
    	// All objects are from android.context.Context
        settings = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
    	SMS_Service_State = settings.getBoolean("smsState", true);
        customMessage = settings.getString("custom_message_preference", "You can change custom message in Settings");
        custom.setText(customMessage.toString());

    	if(SMS_Service_State){
	        imageState.setImageResource(R.drawable.button_on);
	        lowerBar.setBackgroundResource(R.drawable.lowbaron);
    	}
    	
    	else{
	        imageState.setImageResource(R.drawable.button_off);
	        lowerBar.setBackgroundResource(R.drawable.lowbaroff);
    	}
    	
    }
        
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			//fragmentTransaction.remove(serviceListFragment).commit();
			startActivity(new Intent(getApplicationContext(), SettingsActivity.class));
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	public void startService(){
		startService(new Intent(this, SMS_Service.class));
		Log.d(TAG, "Started service");
	}
	
	public void stopService (){
		stopService (new Intent(this, SMS_Service.class));
		Log.d(TAG, "Stoppped service");
	}
	
	public void jiggleLayout(final RelativeLayout l){
		l.clearAnimation(); 
		out = AnimationUtils.loadAnimation(this,R.anim.animationout);
		 Log.d("TAG", "animation");
		 //Handler handler = new Handler();
		 final Runnable r = new Runnable()
		 {
		    public void run() 
		     {
				 l.setAnimation(out);
				 l.animate();
		     }
		 };
		 r.run();


	}	
	
	public void showToggleDialogue(){

		new AlertDialog.Builder(this)
	    .setTitle("How to use Text Secretary")
	    .setMessage("Press the typewriter to toggle the Text Secretary ON/OFF")
	    .setPositiveButton("Dismiss Forever", new DialogInterface.OnClickListener() {
	        public void onClick(DialogInterface dialog, int which) { 
	        	SharedPreferences.Editor editor = settings.edit();
	        	editor.putBoolean("remindToggleDialogue", false);
	        	// Commit the edits!
	        	editor.commit();
	        }
	     })
	    .setNegativeButton("Dismiss", new DialogInterface.OnClickListener() {
	        public void onClick(DialogInterface dialog, int which) { 
	            // do nothing
	        }
	     })
	     .show();
	}
}
