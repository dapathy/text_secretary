package edu.gonzaga.textsecretary;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.telephony.SmsManager;
import android.telephony.SmsMessage;
import android.util.Log;

public class SMS_Service extends Service{
	
	String TAG = "TAG";
	Calendar_Service calendar;
	final Notification_Service mnotification = new Notification_Service();
	
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

		calendar = new Calendar_Service(SMS_Service.this);
		registerReceiver (smsListener, filter);
	}
	
	@Override
	public void onDestroy(){
		super.onDestroy();
		unregisterReceiver(smsListener);
	}
	
	private BroadcastReceiver smsListener = new BroadcastReceiver(){
		@Override
		public void onReceive(Context context, Intent intent) {
			if(intent.getAction().equals("android.provider.Telephony.SMS_RECEIVED") && calendar.inEvent()){
				Bundle bundle = intent.getExtras();
				SmsMessage[] msgs = null;
				String msg_from = "empty";
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
	                        //String msgBody = msgs[i].getMessageBody();
	        				
	        				sendSMS(msg_from);
	        				storeMessage(msg_from, "Sorry I'm busy. I'll get back to you as soon as possible.");
	                    }
					} catch(Exception e){
						Log.d(TAG, "cought");
					}
				}
			}
		}
		
		public void sendSMS(String phoneNumber){
			String message = "Sorry I'm busy. I'll get back to you as soon as possible.";
			SmsManager sms = SmsManager.getDefault();
			sms.sendTextMessage(phoneNumber, null, message, null, null);
            mnotification.displayNotification(getBaseContext(), phoneNumber);
		}
	};
	
    private void storeMessage(String mobNo, String msg) { 
    	ContentValues values = new ContentValues(); 
    	values.put("address", mobNo); 
    	values.put("body", msg); 
    	getContentResolver().insert(Uri.parse("content://sms/sent"), values); 
  } 
	
}
