package edu.gonzaga.textsecretary;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.telephony.SmsManager;
import android.telephony.SmsMessage;
import android.util.Log;

public class SMS_Service extends Service{
	
	private final String TAG = "Service";
	private Calendar_Service calendar;
	private String defMessage = "Sorry, I'm busy at the moment. I'll get back to you as soon as possible.";
	private final Notification_Service mnotification = new Notification_Service(SMS_Service.this);
	private HashMap<String, Long> recentNumbers = new HashMap<String, Long>();
	
	@Override
	public IBinder onBind(Intent arg0) {
		return null;
	}

	@Override
	public void onCreate(){
		super.onCreate();		
		IntentFilter filter = new IntentFilter();
		filter.addAction("android.provider.Telephony.SMS_RECEIVED");
		calendar = new Calendar_Service(getApplicationContext());
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
			SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
			//check if action is sms and (calendar integration is disabled or is in event)
			if(intent.getAction().equals("android.provider.Telephony.SMS_RECEIVED") && (!prefs.getBoolean("calendar_preference", true) || calendar.inEvent())){
				Bundle bundle = intent.getExtras();
				SmsMessage[] msgs = null;
				String msg_from = "empty";
				if (bundle != null){
					try{
						Object[] pdus = (Object[]) bundle.get("pdus");
	                    msgs = new SmsMessage[pdus.length];
	    				Log.d(TAG, "created msgs");
						for(int i = 0; i < msgs.length; i++){
	                        msgs[i] = SmsMessage.createFromPdu((byte[])pdus[i]);
	                        msg_from = msgs[i].getOriginatingAddress(); 
	                    }
						
						//if timer is disabled or is recent
        				if (!prefs.getBoolean("sleep_timer_preference", true) || !isRecent(msg_from, Long.valueOf(prefs.getString("list_preference", "1800000")))){
        			        String message;
        			        
        			        //retrieves correct message
        			        if(prefs.getBoolean("calendar_preference", true)){
        			        	message = prefs.getString("custom_calendar_message_preference", defMessage);
        			        	message = getNewMessage(message);	//replaces insertables
        			        }
        			        else{
        			        	message = prefs.getString("custom_message_preference", defMessage);
        			        }
        					sendSMS(msg_from, message);
	        				
        					//create notification
        					if(prefs.getBoolean("notification_preference", true))
        						mnotification.displayNotification(msg_from);
        					
		        				final String savefrom = msg_from;
		        				final String savemessage = message;
        				    new Handler().postDelayed(new Runnable() {
	        				        @Override
	        				        public void run() {
									storeMessage(savefrom , savemessage);
	        				        }
	        				    }, 2000);
        				}
						
					} catch(Exception e){
						Log.d(TAG, "cought");
					}
				}
			}
		}
	};
	
	//checks if sender has sent a text recently (determined by settings)
	//stores and updates information in hashmap
	private boolean isRecent(String phoneNumber, long repeatTime){
		//if number is new
		long currentTime = Calendar.getInstance().getTimeInMillis();
		if (!recentNumbers.containsKey(phoneNumber)){
			recentNumbers.put(phoneNumber, currentTime);
			return false;
		}
		//if number is already in map
		else{
			long time = recentNumbers.get(phoneNumber);
			recentNumbers.put(phoneNumber, currentTime);
			if ((currentTime - time) > repeatTime){
				return false;
			}
			else{
				Log.d("SDF", "recent");
				return true;
			}
		}
	}
	
	//parses message for [end] and [name] and replaces with info from calendar
	private String getNewMessage(String oldMessage){
		String newMessage;
		CharSequence end = "[end]";
		CharSequence name = "[name]";
		
		newMessage = oldMessage.replace(end, getDate(calendar.getEventEnd()));
		newMessage = newMessage.replace(name, calendar.getEventName());
			
		return newMessage;
	}
	
	//converts milliseconds to date
	private String getDate (long date){
		SimpleDateFormat dateFormat = new SimpleDateFormat ("hh:mm a", Locale.US);
		Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis(date);
		return dateFormat.format (calendar.getTime());
	}
	
	//sends auto reply
	private void sendSMS(String phoneNumber, String message){
		SmsManager sms = SmsManager.getDefault();
		sms.sendTextMessage(phoneNumber, null, message, null, null);
	}
	
	//puts auto reply in conversation
    private void storeMessage(String mobNo, String msg) { 
    	ContentValues values = new ContentValues(); 
    	values.put("address", mobNo); 
    	values.put("body", msg); 
    	getContentResolver().insert(Uri.parse("content://sms/sent"), values); 
  } 
	
}
