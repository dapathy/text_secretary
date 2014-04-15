package edu.gonzaga.textsecretary;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;

public class MainActivity extends Activity {
	
	String TAG = "TAG";
	ImageButton button;
	Boolean SMS_Service_State = true;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		final Notification_Service mnotification = new Notification_Service(MainActivity.this);

        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        SMS_Service_State = settings.getBoolean("smsState", true);

		button= (ImageButton)findViewById(R.id.imageButtonState);
		button.setOnClickListener(imgButtonHandler);
		
	      Button startBtn = (Button) findViewById(R.id.button1);
	      startBtn.setOnClickListener(new View.OnClickListener() {
	         public void onClick(View view) {
	            mnotification.displayNotification("9364467121");
	         }
	      });

	}
	
	View.OnClickListener imgButtonHandler = new View.OnClickListener() {
	    public void onClick(View v) {
			if(SMS_Service_State == true){			//if service is on -> turn off
				stopService();
		        button.setImageResource(R.drawable.button_off);
		        SMS_Service_State = false;
			}
			else{						//else service is off -> turn on
				startService();
		        button.setImageResource(R.drawable.button_on);
		        SMS_Service_State = true;
			}

	    }
	};

    @Override
    protected void onStop(){
    	super.onStop();
    	// We need an Editor object to make preference changes.
    	// All objects are from android.context.Context
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
    	SharedPreferences.Editor editor = settings.edit();
    	editor.putBoolean("smsState", SMS_Service_State);

    	// Commit the edits!
    	editor.commit();
    }
    
    @Override
    protected void onResume(){
    	super.onStop();
    	// We need an Editor object to make preference changes.
    	// All objects are from android.context.Context
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
    	SMS_Service_State = settings.getBoolean("smsState", true);
    	if(SMS_Service_State)
	        button.setImageResource(R.drawable.button_on);
    	
    	else
	        button.setImageResource(R.drawable.button_off);    	

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
	
}
