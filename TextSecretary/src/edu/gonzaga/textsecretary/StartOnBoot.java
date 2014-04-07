package edu.gonzaga.textsecretary;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class StartOnBoot extends BroadcastReceiver{

	@Override
	public void onReceive(Context context, Intent intent) {
		Intent startServiceIntent = new Intent(context, SMS_Service.class);
        context.startService(startServiceIntent);
	}

}
