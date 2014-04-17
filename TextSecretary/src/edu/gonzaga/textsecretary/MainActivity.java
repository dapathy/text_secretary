package edu.gonzaga.textsecretary;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.Image;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageSwitcher;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.ViewSwitcher.ViewFactory;

public class MainActivity extends Activity {
	
	String TAG = "TAG";
	ImageButton button;
	Boolean SMS_Service_State = true;
	RelativeLayout lowerBar;
	ImageView imageState;
	Animation in; 
	Animation out;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        SMS_Service_State = settings.getBoolean("smsState", true);
		button= (ImageButton)findViewById(R.id.imageButtonState);
		button.setOnClickListener(imgButtonHandler);
		lowerBar = (RelativeLayout) findViewById(R.id.bottomBar);
		imageState = (ImageView) findViewById(R.id.stateImage);


	}
	
	View.OnClickListener imgButtonHandler = new View.OnClickListener() {
	    @SuppressLint("ResourceAsColor")
		public void onClick(View v) {
			if(SMS_Service_State == true){			//if service is on -> turn off
				stopService();
		        button.setImageResource(R.drawable.switch_off);
		        //imageSwitcher.setImageResource(R.drawable.button_off);
		        SMS_Service_State = false;
		        lowerBar.setBackgroundResource(R.drawable.lowbaroff);
		        imageState.setImageResource(R.drawable.button_off);
		        jiggleLayout(lowerBar);
		        jiggleImage(imageState);

			}
			else{						//else service is off -> turn on
				startService();
		        button.setImageResource(R.drawable.switch_on);
		        //imageSwitcher.setImageResource(R.drawable.button_on);
		        SMS_Service_State = true;
		        lowerBar.setBackgroundResource(R.drawable.lowbaron);
		        imageState.setImageResource(R.drawable.button_on);
		        jiggleLayout(lowerBar);
		        jiggleImage(imageState);
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
    	if(SMS_Service_State){
	        button.setImageResource(R.drawable.switch_on);
	        imageState.setImageResource(R.drawable.button_on);
	        lowerBar.setBackgroundResource(R.drawable.lowbaron);
    	}
    	
    	else{
	        button.setImageResource(R.drawable.switch_off);
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
		 in = AnimationUtils.loadAnimation(this,R.anim.animationin);
		 out = AnimationUtils.loadAnimation(this,R.anim.animationout);

		 l.setAnimation(out);
		 l.animate();

	}
	
	public void jiggleImage(final ImageView i){
		 in = AnimationUtils.loadAnimation(this,R.anim.animationin);
		 out = AnimationUtils.loadAnimation(this,R.anim.animationout);

		 i.setAnimation(out);
		 i.animate();

	}
	
}
