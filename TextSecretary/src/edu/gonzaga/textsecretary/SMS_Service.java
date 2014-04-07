package edu.gonzaga.textsecretary;

import android.R.string;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.IBinder;
import android.telephony.SmsManager;
import android.telephony.SmsMessage;
import android.util.Log;
import android.widget.Toast;

public class SMS_Service extends Service{

	
	String TAG = "TAG";
	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void onCreate(){
		super.onCreate();
		IntentFilter filter = new IntentFilter();
		filter.addAction("android.provider.Telephony.SMS_RECEIVED");
		
		registerReceiver (smsListener, filter);
	}
	
	@Override
	public void onDestroy(){
		super.onDestroy();
		unregisterReceiver(smsListener);
	}
	
	private BroadcastReceiver smsListener = new BroadcastReceiver(){
		// Get the object of SmsManager
		final SmsManager sms = SmsManager.getDefault();
		
		@Override
		public void onReceive(Context context, Intent intent) {
			if(intent.getAction().equals("android.provider.Telephony.SMS_RECEIVED")){
				Toast.makeText(getApplicationContext(), "test", Toast.LENGTH_SHORT).show();
				Bundle bundle = intent.getExtras();
				SmsMessage[] msgs = null;
				String msg_from;
				Log.d(TAG, "we're here");
				if (bundle != null){
					try{
						Log.d(TAG, "trying");
						Object[] pdus = (Object[]) bundle.get("pdus");
	                    msgs = new SmsMessage[pdus.length];
	    				Log.d(TAG, "created msgs");
						for(int i = 0; i < msgs.length; i++){
							Log.d(TAG, "in the for");
	                        msgs[i] = SmsMessage.createFromPdu((byte[])pdus[i]);
	                        msg_from = msgs[i].getOriginatingAddress();
	        				Log.d(TAG, "finished for");
	                        //String msgBody = msgs[i].getMessageBody();
	                    }
					} catch(Exception e){
						Log.d(TAG, "cought");
						Log.d("SMS", e.getMessage());
					}
				}
			}
			
			Log.d(TAG, "DONE WITH ONRECEIVE");
		}

	};
	
}
