package edu.gonzaga.textsecretary;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;

import com.google.android.gms.location.DetectedActivity;

import edu.gonzaga.textsecretary.activity_recognition.ActivityRecognitionIntentService;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.ContentObserver;
import android.database.Cursor;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.provider.ContactsContract;
import android.provider.ContactsContract.PhoneLookup;
import android.provider.Telephony;
import android.telephony.PhoneStateListener;
import android.telephony.SmsManager;
import android.telephony.SmsMessage;
import android.telephony.TelephonyManager;
import android.util.Log;

public class SMS_Service extends Service{
	
	private static final String TAG = "SMS_SERVICE";
	private static final String defMessage = "Sorry, I'm busy at the moment. I'll get back to you as soon as possible.";
	private static final String defDrivingMsg = "Sorry, I'm on the road. I'll get back to you as soon as possible.";
	
	private Calendar_Service calendar;
	private SharedPreferences prefs;
    private SharedPreferences.Editor editor;
	private int respondTo;
	private final Notification_Service mnotification = new Notification_Service(SMS_Service.this);
	private PhoneStateChangeListener pscl;
	private TelephonyManager tm;
	private OutgoingListener outgoingListener;
	private HashMap<String, Long> recentNumbers = new HashMap<String, Long>();
	private boolean listenerLock = false;
	private AudioManager ringerManager;
    private int currentRingerMode = AudioManager.RINGER_MODE_SILENT;
    private int lastActivityState = DetectedActivity.UNKNOWN;
	
	@Override
	public IBinder onBind(Intent arg0) {
		return null;
	}

	@Override
	public void onCreate(){
		super.onCreate();
		calendar = new Calendar_Service(getApplicationContext());
		ringerManager = (AudioManager) getApplicationContext().getSystemService(Context.AUDIO_SERVICE);
		
		IntentFilter filter = new IntentFilter();
		filter.addAction("android.provider.Telephony.SMS_RECEIVED");
		filter.addAction(TelephonyManager.ACTION_PHONE_STATE_CHANGED);
		filter.addAction("edu.gonzaga.text_secretary.activity_recognition.ACTIVITY_STATE");
		
		ContentResolver contentResolver = getApplicationContext().getContentResolver();
		outgoingListener = new OutgoingListener(new Handler());
		contentResolver.registerContentObserver(Uri.parse("content://sms"), true, outgoingListener);
		
		registerReceiver (receiver, filter);
	}
	
	@Override
	public void onDestroy(){
		unregisterReceiver(receiver);
		getContentResolver().unregisterContentObserver(outgoingListener);
		
		//if listener has been created, unregister it
		if (listenerLock) {
	        tm.listen(pscl, PhoneStateListener.LISTEN_NONE);
	        pscl = null;
			tm = null;
			listenerLock = false;
		}
		super.onDestroy();
	}
	
	private BroadcastReceiver receiver = new BroadcastReceiver(){
		@Override
		public void onReceive(Context context, Intent intent) {
			prefs = PreferenceManager.getDefaultSharedPreferences(context);
			editor = prefs.edit();
				
			respondTo = Integer.parseInt(prefs.getString("respond_to_preference", "3"));
			
			//if call (this will only run once, we only want one listener active)
			if(intent.getAction().equals(TelephonyManager.ACTION_PHONE_STATE_CHANGED) && !listenerLock) {
				pscl = new PhoneStateChangeListener(context);
				tm = (TelephonyManager)context.getSystemService(Context.TELEPHONY_SERVICE);
				tm.listen(pscl, PhoneStateListener.LISTEN_CALL_STATE);
				Log.d(TAG, "register listener");
				listenerLock = true;
			}
			
			//if received SMS
			else if(intent.getAction().equals("android.provider.Telephony.SMS_RECEIVED") && shouldReply()){
				//silence ringer regardless of auto reply settings
	    		final int silencerType = Integer.parseInt(prefs.getString("silencer_preference", "0"));
	    		
				if (silencerType == 1 || silencerType == 3)
					silenceRinger();
				
				//now check auto reply settings
				if (respondTo != 1 && respondTo != 2) {
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
				
				//need delay for turning off sms sound
				new Handler().postDelayed(new Runnable() {
			        @Override
			        public void run() {
			        	if (silencerType == 1 || silencerType == 3)
							restoreRingerMode();
			        }
			    }, 5000);
			}
			
			else if (intent.getAction().equals("edu.gonzaga.text_secretary.activity_recognition.ACTIVITY_STATE")) {
				lastActivityState = intent.getIntExtra("type", DetectedActivity.UNKNOWN);
				if(ActivityRecognitionIntentService.isMoving(lastActivityState) && !prefs.getBoolean("drivingTempOff", false)){
					Intent intentDriving = new Intent(getApplicationContext(), DrivingNotification.class);
					startService(intentDriving);
				}
				else{
					editor.putBoolean("drivingTempOff", false);
				}
				Log.d(TAG, "received activity state: " + lastActivityState);
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
	    	if (shouldReply()) {
	    		int silencerType = Integer.parseInt(prefs.getString("silencer_preference", "0"));
	    		switch(state){
		            case TelephonyManager.CALL_STATE_RINGING:
		                 Log.d(TAG, "RINGING"); 
		                 if ((respondTo != 0) && ((respondTo == 2 || respondTo == 4) || isMobileContact(incomingNumber, mContext))) {
			                 wasRinging = true;
			                 number = incomingNumber;
		                 }
		                 
		                 if (silencerType == 2 || silencerType == 3)
		                	 silenceRinger();
		                 break;
		            case TelephonyManager.CALL_STATE_OFFHOOK:
		                 Log.d(TAG, "OFFHOOK");
		                 wasRinging = false;
		                 
		                 if (silencerType == 2 || silencerType == 3)
		                 	restoreRingerMode();
		                 break;
		            case TelephonyManager.CALL_STATE_IDLE:
		                 Log.d(TAG, "IDLE");
		                 if (wasRinging)
		                	 handleSMSReply(number);
		                 
		                 wasRinging = false;
		                 number = "";
		                 
		                 if (silencerType == 2 || silencerType == 3)
		                	 restoreRingerMode();
		                 break;
		        }
	    	}
	    }
	}
	
	private static boolean isMobileContact(String number, Context context) {
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
		//if timer is disabled or is not recent
		if (!prefs.getBoolean("sleep_timer_preference", true) || !isRecent(msg_from, Long.valueOf(prefs.getString("list_preference", "1800000")))){
	        String message = getCorrectMessage();
	        
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
		String newNumber = formatPhoneNumber(phoneNumber);
		long currentTime = Calendar.getInstance().getTimeInMillis();
		if (!recentNumbers.containsKey(newNumber)){
			recentNumbers.put(newNumber, currentTime);
			Log.d(TAG, "new number: " + newNumber);
			return false;
		}
		//if number is already in map
		else{
			long time = recentNumbers.get(newNumber);
			recentNumbers.put(newNumber, currentTime);
			if ((currentTime - time) > repeatTime){
				Log.d(TAG, "update time" + newNumber);
				return false;
			}
			else{
				Log.d("SDF", "recent: " + newNumber);
				return true;
			}
		}
	}
	
	//parses message for [end] and [name] and replaces with info from calendar
	private String replaceInsertables(String oldMessage){
		String newMessage;
		CharSequence end = "[end]";
		CharSequence name = "[name]";
		
		newMessage = oldMessage.replace(end, convertDateToString(calendar.getEventEnd()));
		newMessage = newMessage.replace(name, calendar.getEventName());
			
		return newMessage;
	}
	
	//converts milliseconds to date
	private static String convertDateToString (long date){
		SimpleDateFormat dateFormat = new SimpleDateFormat ("hh:mm a", Locale.US);
		Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis(date);
		return dateFormat.format (calendar.getTime());
	}
	
	//sends auto reply
	private static void sendSMS(String phoneNumber, String message){
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
	
    private class OutgoingListener extends ContentObserver {
    	
        public OutgoingListener(Handler handler) {
            super(handler);
        }

        @Override
        public void onChange(boolean selfChange) {
            super.onChange(selfChange);
            
            prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
			//calendar integration is disabled or is in event
			if(prefs.getBoolean("smart_sent_message", false) && prefs.getBoolean("sleep_timer_preference", true) && (!prefs.getBoolean("calendar_preference", true) || calendar.inEvent())){
				Cursor cursor = getApplicationContext().getContentResolver().query(
						Uri.parse("content://sms"), null, null, null, null);
				
				//last outgoing message
				if (cursor.moveToNext()) {
	    			String protocol = cursor.getString(cursor.getColumnIndex("protocol"));
	    			int type = cursor.getInt(cursor.getColumnIndex("type"));
	    			// Only processing outgoing sms event & only when it
	    			// is sent successfully (available in SENT box).
	    			if (protocol == null && type == Telephony.TextBasedSmsColumns.MESSAGE_TYPE_SENT) {		    			
		    			int addressColumn = cursor.getColumnIndex("address");
		    			String number = cursor.getString(addressColumn);
		    			long currentTime = Calendar.getInstance().getTimeInMillis();
		    			String newNumber = formatPhoneNumber(number);
		    			recentNumbers.put(newNumber, currentTime);
	    				Log.d(TAG, "Sent message to: " + newNumber);
	    			}
	    		}
				cursor.close();
			}		
		}	                 
    }
    
    private static String formatPhoneNumber(String number) {
    	String newNumber = number;
    	
    	//replace non-numbers
    	newNumber = newNumber.replaceAll("\\W", "");
    	
    	//replace starting '1' if exists
    	if (newNumber.charAt(0) == '1') 
    		newNumber = newNumber.substring(1);
    	
    	Log.d(TAG, "formatted number is: " + newNumber);
    	
    	return newNumber;
    }
    
    private void silenceRinger() {
    	int tempRingerMode = ringerManager.getRingerMode();
    	//prevents possible conflicts between listeners
    	if (tempRingerMode != AudioManager.RINGER_MODE_SILENT) {
    		Log.d(TAG, currentRingerMode + " silence");
        	ringerManager.setRingerMode(AudioManager.RINGER_MODE_SILENT);
        	currentRingerMode = tempRingerMode;	//save current mode
    	}
    }
    
    private void restoreRingerMode() {
    	Log.d(TAG, currentRingerMode + " restore");
    	ringerManager.setRingerMode(currentRingerMode);
    }
    
    //retrieves correct message
    private String getCorrectMessage() {
    	String message;
    	//check driving, then calendar, then get default
        if (ActivityRecognitionIntentService.isMoving(lastActivityState) && prefs.getBoolean("driving_preference", false)){
        	message = prefs.getString("custom_driving_message_preference", defDrivingMsg);
        }
        else if(prefs.getBoolean("calendar_preference", true)){
        	message = prefs.getString("custom_calendar_message_preference", defMessage);
        	message = replaceInsertables(message);
        }
        else{
        	message = prefs.getString("custom_message_preference", defMessage);
        }
        return message;
    }
    
    //(is driving and driving enabled) or calendar disabled or in event
    private boolean shouldReply() {
    	return (ActivityRecognitionIntentService.isMoving(lastActivityState) && prefs.getBoolean("driving_preference", false)) || !prefs.getBoolean("calendar_preference", true) || calendar.inEvent();
    }
}
