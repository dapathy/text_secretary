package edu.gonzaga.textsecretary;

import android.app.IntentService;
import android.app.PendingIntent;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

public class Intent_Handler extends IntentService{

	public Intent_Handler() {
		super("Intent_Handler");
		Log.d("TAG", "snoozingh");

		// TODO Auto-generated constructor stub
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		// TODO Auto-generated method stub
		//PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_CANCEL_CURRENT);
		Log.d("TAG", "snoozingh");
		Toast.makeText(getBaseContext(), "SNOOZE", Toast.LENGTH_LONG).show();

	}

}
