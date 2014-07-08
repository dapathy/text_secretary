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
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.provider.ContactsContract;
import android.provider.ContactsContract.PhoneLookup;
import android.telephony.PhoneStateListener;
import android.telephony.SmsManager;
import android.telephony.SmsMessage;
import android.telephony.TelephonyManager;
import android.util.Log;

public class SMS_Service extends Service{
	
	private final String TAG = "SMS_SERVICE";
	private Calendar_Service calendar;
	private String defMessage = "Sorry, I'm busy at the moment. I'll get back to you as soon as possible.";
	private SharedPreferences prefs;
	private int respondTo;
	private final Notification_Service mnotification = new Notification_Service(SMS_Service.this);
	private static PhoneStateChangeListener pscl;
	private static TelephonyManager tm;
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
		filter.addAction(TelephonyManager.ACTION_PHONE_STATE_CHANGED);
		calendar = new Calendar_Service(getApplicationContext());
		registerReceiver (receiver, filter);
	}
	
	@Override
	public void onDestroy(){
		super.onDestroy();
		unregisterReceiver(receiver);
	}
	
	private BroadcastReceiver receiver = new BroadcastReceiver(){
		@Override
		public void onReceive(Context context, Intent intent) {
			prefs = PreferenceManager.getDefaultSharedPreferences(context);
			//calendar integration is disabled or is in event
			if(!prefs.getBoolean("calendar_preference", true) || calendar.inEvent()){
				
				respondTo = Integer.parseInt(prefs.getString("respond_to_preference", "3"));
				
				//if call
				if(intent.getAction().equals(TelephonyManager.ACTION_PHONE_STATE_CHANGED) && (respondTo != 0)) {
					pscl = new PhoneStateChangeListener(context);
					tm = (TelephonyManager)context.getSystemService(Context.TELEPHONY_SERVICE);
					tm.listen(pscl, PhoneStateListener.LISTEN_CALL_STATE);
				}
				
				//if received SMS
				else if(intent.getAction().equals("android.provider.Telephony.SMS_RECEIVED") && (respondTo != 1 && respondTo != 2)){
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
							
							handleSMSReply(msg_from);
							
						} catch(Exception e){
							Log.d(TAG, "cought");
						}
					}
				}
			}
		}
	};
	
	private class PhoneStateChangeListener extends PhoneStateListener {
	    private Context mContext;
		private boolean wasRinging = false;
	    private String number;

	    public PhoneStateChangeListener (Context context) {
	    	super();
	    	mContext = context;
	    }
	    
	    @Override
	    public void onCallStateChanged(int state, String incomingNumber) {
	        switch(state){
	            case TelephonyManager.CALL_STATE_RINGING:
	                 Log.d(TAG, "RINGING");
	                 wasRinging = true;
	                 number = incomingNumber;
	                 break;
	            case TelephonyManager.CALL_STATE_OFFHOOK:
	                 Log.d(TAG, "OFFHOOK");
	                 wasRinging = false;
	                 break;
	            case TelephonyManager.CALL_STATE_IDLE:
	                 Log.d(TAG, "IDLE");
	                 if (wasRinging && (isMobileContact(number, mContext) || (respondTo == 2 || respondTo == 4))){
	                	 handleSMSReply(number);
	                 }
	                 wasRinging = false;
	                 number = "";
	                 tm.listen(pscl, PhoneStateListener.LISTEN_NONE);
	                 break;
	        }
	    }
	}
	
	private boolean isMobileContact(String number, Context context) {
		if (number.isEmpty())
			return false;
		else{
			Uri uri = Uri.withAppendedPath(PhoneLookup.CONTENT_FILTER_URI, Uri.encode(number));
			String[] projection = new String[]{
					PhoneLookup.TYPE
			};
			Cursor contactCursor = context.getContentResolver().query(uri, projection, null, null, null);
			Log.d(TAG, "query completed");
			if (contactCursor.moveToFirst()
				&& (contactCursor.getInt(0) == ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE)){
					
				Log.d(TAG, "mobile found");
				contactCursor.close();
				return true;
			}
			Log.d(TAG, "no mobile found");
			contactCursor.close();
			return false;
		}
	}
	
	private void handleSMSReply(String msg_from) {
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
			
			//store message
			final String savefrom = msg_from;
			final String savemessage = message;
		    new Handler().postDelayed(new Runnable() {
			        @Override
			        public void run() {
					storeMessage(savefrom , savemessage);
			        }
			    }, 2000);
		}
	}
	
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
