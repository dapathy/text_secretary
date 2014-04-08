package edu.gonzaga.textsecretary;

import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

public class MainActivity extends Activity {
	
	String TAG = "TAG";
	ImageButton button;
	Boolean SMS_Service_State = true;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);


		button= (ImageButton)findViewById(R.id.imageButtonState);
		button.setOnClickListener(imgButtonHandler);

	}
	
	View.OnClickListener imgButtonHandler = new View.OnClickListener() {
	    public void onClick(View v) {
			if(SMS_Service_State == true){			//if service is on -> turn off
				stopService(v);
		        button.setImageResource(R.drawable.button_off);
		        SMS_Service_State = false;
			}
			else{						//else service is off -> turn on
				startService(v);
		        button.setImageResource(R.drawable.button_on);
		        SMS_Service_State = true;
			}

	    }
	};

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
	
	public void startService(View event){
		startService(new Intent(this, SMS_Service.class));
		Log.d(TAG, "Started service");
	}
	
	public void stopService (View event){
		stopService (new Intent(this, SMS_Service.class));
		Log.d(TAG, "Stoppped service");
	}
	
}
