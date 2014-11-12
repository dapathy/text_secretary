package edu.gonzaga.textsecretary;

import edu.gonzaga.textsecretary.activity_recognition.ActivityRecognizer;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
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
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.RemoteViews;

public class MainActivity extends Activity {
	
	private final static String TAG = "MAIN";
	private Boolean remindToggleDialogue;
	private RelativeLayout lowerBar, lowerHalf, listFragment;
	private ImageButton imageState;
	private Animation out;
	private EditText custom;
	private SharedPreferences settings;
	private RemoteViews remoteViews;
	private ComponentName widget;
	private AppWidgetManager appWidgetManager;
	private FragmentManager fragmentManager;
	private FragmentTransaction fragmentTransaction;
	private ServiceListFragment myFragment;
	private boolean enableButton = false;
	private ProgressBar spinner;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
        settings = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        
        setUpGui();
		setUpWidget();
				
        // get an instance of FragmentTransaction from your Activity
        fragmentManager = getFragmentManager();
        fragmentTransaction = fragmentManager.beginTransaction();

        //add a fragment
        if(savedInstanceState == null){
	        myFragment = new ServiceListFragment();
	        fragmentTransaction.add(R.id.listFragmentLayout, myFragment);
	        fragmentTransaction.commit();
        }
                
        spinner.setVisibility(View.VISIBLE);
        new Thread(checkActivation).start();
        
        //Remind users how to use toggle
		remindToggleDialogue = settings.getBoolean("remindToggleDialogue", true);
		if(remindToggleDialogue)
			showToggleDialogue();
	}
	
	private void setUpGui(){
		 //GUI stuff
		spinner = (ProgressBar) findViewById(R.id.progressBar1);
		lowerBar = (RelativeLayout) findViewById(R.id.bottomBar);
		lowerHalf = (RelativeLayout) findViewById(R.id.bottomHalf);
		listFragment = (RelativeLayout) findViewById(R.id.listFragmentLayout);
		imageState = (ImageButton) findViewById(R.id.stateImage);
		if (!settings.getBoolean("smsState", false)){		//if setting is off, button should be off
			imageState.setImageResource(R.drawable.button_off);
		}
		imageState.setOnClickListener(imgButtonHandler);
		
		//Set the on click listener for the custom message
		custom = (EditText) findViewById(R.id.customMessage);
		custom.setOnClickListener(new View.OnClickListener() {
		        @Override
		        public void onClick(View v) {
					startActivity(new Intent(getApplicationContext(), SettingsActivity.class));
		        }
		    });
		
		setMessage();
		Log.d(TAG, "gui");
	}
	
	private void setUpWidget(){
		//WidgetStuff
        remoteViews = new RemoteViews(getApplicationContext().getPackageName(), R.layout.widgetlayout);
        widget = new ComponentName(getApplicationContext(), Widget.class);
        appWidgetManager = AppWidgetManager.getInstance(getApplicationContext());
		Log.d(TAG, "widget setup");
	}
	
	// Put the custom Message in the Edit Text
	private void setMessage(){
		String customMessage = "You can change custom message in Settings";
		if (settings.getBoolean("calendar_preference", true))
			customMessage = settings.getString("custom_calendar_message_preference", customMessage);
		else
			customMessage = settings.getString("custom_message_preference", customMessage);
        
		custom.setText(customMessage);
	}
	
	View.OnClickListener imgButtonHandler = new View.OnClickListener() {
	    @SuppressLint("ResourceAsColor")
		public void onClick(View v) {
	    	SharedPreferences.Editor editor = settings.edit();
			if(settings.getBoolean("smsState", false) == true){			//if service is on -> turn off
				stopService();
		        editor.putBoolean("smsState", false).commit();
		        changeFragmentTextColor(false);
				custom.setTextColor(getResources().getColor(R.color.lightestgrey));
		        lowerBar.setBackgroundResource(R.drawable.lowbaroff);
		        imageState.setImageResource(R.drawable.button_off);
	        	remoteViews.setImageViewResource(R.id.imageview_icon, R.drawable.widgetoff);
		        jiggleGui();
		        appWidgetManager.updateAppWidget(widget, remoteViews);
			}
			else{						//else service is off -> turn on
				if(enableButton){
					startService();
					editor.putBoolean("smsState", true).commit();
					changeFragmentTextColor(true);
					custom.setTextColor(getResources().getColor(R.color.lightgrey));
			        lowerBar.setBackgroundResource(R.drawable.lowbaron);
			        imageState.setImageResource(R.drawable.button_on);
		        	remoteViews.setImageViewResource(R.id.imageview_icon, R.drawable.widgeton);
			        jiggleGui();
			        appWidgetManager.updateAppWidget(widget, remoteViews);
				}
			}

	    }
	};
	
	private void jiggleGui(){
		jiggleLayout(lowerBar);
        jiggleLayout(lowerHalf);
        jiggleLayout(listFragment);
	}

    @Override
    protected void onResume(){
    	super.onResume();
    	
		setMessage();
        
    	if(settings.getBoolean("smsState", false)){
	        imageState.setImageResource(R.drawable.button_on);
	        lowerBar.setBackgroundResource(R.drawable.lowbaron);
			custom.setTextColor(getResources().getColor(R.color.lightgrey));

    	}
    	
    	else{
	        imageState.setImageResource(R.drawable.button_off);
	        lowerBar.setBackgroundResource(R.drawable.lowbaroff);
			custom.setTextColor(getResources().getColor(R.color.lightestgrey));
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
			startActivity(new Intent(getApplicationContext(), SettingsActivity.class));
			return true;
		}
		else if(id == R.id.action_help){
			startActivity(new Intent(getApplicationContext(), Help_Activity.class));
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	private void startService(){
		startService(new Intent(this, SMS_Service.class));
		Log.d(TAG, "Started service");
		
		//if driving enabled, start that service
		if (settings.getBoolean("driving_preference", false)) {
			ActivityRecognizer.startUpdates(getApplicationContext());
			Log.d(TAG, "driving service started");
		}
	}
	
	private void stopService (){
		stopService (new Intent(this, SMS_Service.class));
		Log.d(TAG, "Stoppped service");
		
		//if driving enabled, turn it off too
		if (settings.getBoolean("driving_preference", false))
			ActivityRecognizer.stopUpdates();
	}
	
	private void jiggleLayout(final RelativeLayout l){
		 l.clearAnimation(); 
		 //load the "bounce" animation
		 out = AnimationUtils.loadAnimation(this,R.anim.animationout);
		 //run the animation on a thread other than the
		 //main UI thread
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
	
	private void changeFragmentTextColor(boolean dark){
        myFragment.changeTextColor(dark);
	}
	
	//This dialogue is here to teach users how to toggle the service on and off
	private void showToggleDialogue(){
		new AlertDialog.Builder(this)
	    .setTitle("How to use Text Secretary")
	    .setMessage("Press the typewriter to toggle the Text Secretary ON/OFF")
	    .setPositiveButton("Dismiss Forever", new DialogInterface.OnClickListener() {
	        public void onClick(DialogInterface dialog, int which) { 
	        	SharedPreferences.Editor editor = settings.edit();
	        	editor.putBoolean("remindToggleDialogue", false);
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
	
	//thread to check activation
	Runnable checkActivation = new Runnable() {
	    @Override
	    public void run() {
	    	//check unlock
	        enableButton = RegCheck.isActivated(MainActivity.this);
	        Log.d(TAG, String.valueOf(enableButton));
	        
	        //remove spinner
	        MainActivity.this.runOnUiThread(new Runnable() {
	            @Override
	            public void run() {
	            	 spinner.setVisibility(View.GONE);
	           }
	       });
	    }
	};
}
