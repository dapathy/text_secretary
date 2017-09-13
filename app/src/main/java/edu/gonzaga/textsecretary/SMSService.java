package edu.gonzaga.textsecretary;

import android.app.NotificationManager;
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

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;

import edu.gonzaga.textsecretary.activity_recognition.ActivityRecognizer;

public class SMSService extends Service {

	private static final String TAG = "SMS_SERVICE";
	private static final String defMessage = "Sorry, I'm busy at the moment. I'll get back to you as soon as possible.";
	private static final String defCalMsg = "Sorry, I'm busy at the moment. I'll get back to you around [end].";
	private static final String defDrivingMsg = "Sorry, I'm on the road. I'll get back to you as soon as possible.";
	private static final String appendMsg = " Sent by Text Secretary.";

	private CalendarUtility calendar;
	private SharedPreferences prefs;
	private int respondTo;
	private NotificationUtility mNotification = new NotificationUtility(this);
	private NotificationManager notificationManager;
	private PhoneStateChangeListener pscl;
	private TelephonyManager tm;
	private OutgoingListener outgoingListener;
	private HashMap<String, Long> recentNumbers = new HashMap<>();
	private boolean listenerLock = false;
	private DrivingNotification drivingNotification = new DrivingNotification(this);
	private Silencer silencer;
	private ActivityRecognizer activityRecognizer;
	private BroadcastReceiver receiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			SharedPreferences.Editor editor;
			editor = prefs.edit();

			respondTo = Integer.parseInt(prefs.getString("respond_to_preference", "3"));

			//if call (this will only run once, we only want one listener active)
			if (intent.getAction().equals(TelephonyManager.ACTION_PHONE_STATE_CHANGED) && !listenerLock) {
				pscl = new PhoneStateChangeListener(context);
				tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
				tm.listen(pscl, PhoneStateListener.LISTEN_CALL_STATE);
				Log.d(TAG, "register listener");
				listenerLock = true;
			}

			//if received SMS
			else if (intent.getAction().equals("android.provider.Telephony.SMS_RECEIVED") && shouldReply()) {
				//now check auto reply settings
				if (respondTo != 1 && respondTo != 2) {
					Bundle bundle = intent.getExtras();
					SmsMessage[] msgs;
					String msg_from = "empty";
					if (bundle != null) {
						try {
							Object[] pdus = (Object[]) bundle.get("pdus");
							msgs = new SmsMessage[pdus.length];
							Log.d(TAG, "created msgs");
							for (int i = 0; i < msgs.length; i++) {
								msgs[i] = SmsMessage.createFromPdu((byte[]) pdus[i]);
								msg_from = msgs[i].getOriginatingAddress();
							}

							handleSMSReply(msg_from);

						} catch (Exception e) {
							Log.d(TAG, "cought");
						}
					}
				}
			}

			//broadcast is sent upon change in activity (driving or not)
			else if (intent.getAction().equals("edu.gonzaga.text_secretary.activity_recognition.ACTIVITY_STATE")) {
				editor.putBoolean("isPassenger", false).apply();    //reset passenger upon change in state

				//actually driving
				if (isDriving()) {
					if (prefs.getBoolean("silence_preference", false))
						silencer.silenceRinger();
					drivingNotification.displayNotification();
				}
				//if not in moving vehicle
				else if (!activityRecognizer.isDriving()) {
					if (prefs.getBoolean("silence_preference", false))
						silencer.restoreRingerMode();
					notificationManager.cancel(11001100);
				}
			}
		}
	};

	@Override
	public void onCreate() {
		super.onCreate();
		calendar = new CalendarUtility(getApplicationContext());
		prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
		prefs.edit().putBoolean("isPassenger", false).apply();        //"set passenget to false on start up
		notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		silencer = Silencer.getInstance(getApplicationContext());
		activityRecognizer = ActivityRecognizer.getInstance(getApplicationContext());

		//start silencer service if necessary
		if (prefs.getBoolean("silence_preference", false))
			silencer.startSilencerPoller();

		//start driving service if necessary
		if (prefs.getBoolean("driving_preference", false))
			activityRecognizer.startUpdates();

		IntentFilter filter = new IntentFilter();
		filter.addAction("android.provider.Telephony.SMS_RECEIVED");
		filter.addAction(TelephonyManager.ACTION_PHONE_STATE_CHANGED);
		filter.addAction("edu.gonzaga.text_secretary.activity_recognition.ACTIVITY_STATE");

		ContentResolver contentResolver = getApplicationContext().getContentResolver();
		outgoingListener = new OutgoingListener(new Handler());
		contentResolver.registerContentObserver(Uri.parse("content://sms"), true, outgoingListener);

		registerReceiver(receiver, filter);
	}

	@Override
	public void onDestroy() {
		unregisterReceiver(receiver);
		getContentResolver().unregisterContentObserver(outgoingListener);

		//if listener has been created, unregister it
		if (listenerLock) {
			tm.listen(pscl, PhoneStateListener.LISTEN_NONE);
			pscl = null;
			tm = null;
			listenerLock = false;
		}

		//stop driving service if necessary
		if (prefs.getBoolean("driving_preference", false))
			activityRecognizer.stopUpdates();

		//stop silencer service if necessary
		if (prefs.getBoolean("silence_preference", false))
			silencer.stopSilencerPoller();
		super.onDestroy();
	}

	@Override
	public IBinder onBind(Intent arg0) {
		return null;
	}

	//converts milliseconds to date
	public static String convertDateToString(long date) {
		SimpleDateFormat dateFormat = new SimpleDateFormat("hh:mm a", Locale.US);
		Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis(date);
		return dateFormat.format(calendar.getTime());
	}

	private static boolean isMobileContact(String number, Context context) {
		if (number.isEmpty())
			return false;
		else {
			Uri uri = Uri.withAppendedPath(PhoneLookup.CONTENT_FILTER_URI, Uri.encode(number));
			String[] projection = new String[]{
					PhoneLookup.TYPE
			};
			Cursor contactCursor = context.getContentResolver().query(uri, projection, null, null, null);
			Log.d(TAG, "query completed");
			if (contactCursor.moveToFirst()
					&& (contactCursor.getInt(0) == ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE)) {

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
		long singleResponse = Long.valueOf(prefs.getString("single_response_preference", "0"));
		//if timer is disabled or is not recent
		if ((singleResponse == 0) || !isRecent(msg_from, singleResponse)) {
			String message = getCorrectMessage();

			sendSMS(msg_from, message);

			//create notification
			if (prefs.getBoolean("notification_preference", true))
				mNotification.displayNotification(msg_from);

			//store message
			final String savefrom = msg_from;
			final String savemessage = message;
			new Handler().postDelayed(new Runnable() {
				@Override
				public void run() {
					storeMessage(savefrom, savemessage);
				}
			}, 2000);
		}
	}

	//checks if sender has sent a text recently (determined by settings)
	//stores and updates information in hashmap
	private boolean isRecent(String phoneNumber, long repeatTime) {
		//if number is new
		String newNumber = formatPhoneNumber(phoneNumber);

		//avoid dumb phones
		if (newNumber.length() < 4)
			return false;

		long currentTime = Calendar.getInstance().getTimeInMillis();
		if (!recentNumbers.containsKey(newNumber)) {
			recentNumbers.put(newNumber, currentTime);
			Log.d(TAG, "new number: " + newNumber);
			return false;
		}
		//if number is already in map
		else {
			long time = recentNumbers.get(newNumber);
			recentNumbers.put(newNumber, currentTime);
			if ((currentTime - time) > repeatTime) {
				Log.d(TAG, "update time" + newNumber);
				return false;
			} else {
				Log.d("SDF", "recent: " + newNumber);
				return true;
			}
		}
	}

	//retrieves correct message
	private String getCorrectMessage() {
		String message;
		//check driving, then calendar, then get default
		if (isDriving()) {
			message = prefs.getString("custom_driving_message_preference", defDrivingMsg);
		} else if (prefs.getBoolean("calendar_preference", true)) {
			message = prefs.getString("custom_calendar_message_preference", defCalMsg);
			message = replaceInsertables(message);
		} else {
			message = prefs.getString("custom_message_preference", defMessage);
		}
		return message;
	}

	//sends auto reply
	private void sendSMS(String phoneNumber, String message) {
		SmsManager sms = SmsManager.getDefault();
		//if not activated, append
		if (!RegistrationValidator.isActivated(getApplicationContext())) {
			message = message + appendMsg;
			Log.d(TAG, "auto: not activated so appending message");
		}
		sms.sendTextMessage(phoneNumber, null, message, null, null);
		Log.d(TAG, "AUTO REPLIED!");
	}

	//puts auto reply in conversation
	private void storeMessage(String mobNo, String msg) {
		ContentValues values = new ContentValues();
		values.put("address", mobNo);
		values.put("body", msg);
		getContentResolver().insert(Uri.parse("content://sms/sent"), values);
	}

	private static String formatPhoneNumber(String number) {
		String newNumber = number;

		//replace non-numbers
		newNumber = newNumber.replaceAll("\\W", "");

		try {
			//replace starting '1' if exists
			if (newNumber.charAt(0) == '1')
				newNumber = newNumber.substring(1);
		} catch (Exception e) {
			Log.e(TAG, e.getMessage());
		}

		Log.d(TAG, "formatted number is: " + newNumber);

		return newNumber;
	}

	private boolean isDriving() {
		return activityRecognizer.isDriving() && !prefs.getBoolean("isPassenger", false) && prefs.getBoolean("driving_preference", false);
	}

	//parses message for [end] and [name] and replaces with info from calendar
	private String replaceInsertables(String oldMessage) {
		String newMessage;
		CharSequence end = "[end]";
		CharSequence name = "[name]";

		newMessage = oldMessage.replace(end, convertDateToString(calendar.getEventEnd()));
		newMessage = newMessage.replace(name, calendar.getEventName());

		return newMessage;
	}

	private boolean shouldReply() {
		return isDriving() || shouldAlwaysReply() || calendar.inEvent();
	}

	private boolean shouldAlwaysReply() {
		return !prefs.getBoolean("driving_preference", false) && !prefs.getBoolean("calendar_preference", true);
	}

	private class PhoneStateChangeListener extends PhoneStateListener {
		private Context mContext;
		private boolean wasRinging = false;
		private String number;

		public PhoneStateChangeListener(Context context) {
			super();
			mContext = context;
		}

		@Override
		public void onCallStateChanged(int state, String incomingNumber) {
			if (shouldReply()) {
				switch (state) {
					case TelephonyManager.CALL_STATE_RINGING:
						Log.d(TAG, "RINGING");
						if ((respondTo != 0) && ((respondTo == 2 || respondTo == 4) || isMobileContact(incomingNumber, mContext))) {
							wasRinging = true;
							number = incomingNumber;
						}
						break;
					case TelephonyManager.CALL_STATE_OFFHOOK:
						Log.d(TAG, "OFFHOOK");
						wasRinging = false;
						break;
					case TelephonyManager.CALL_STATE_IDLE:
						Log.d(TAG, "IDLE");
						if (wasRinging)
							handleSMSReply(number);

						wasRinging = false;
						number = "";
						break;
				}
			}
		}
	}

	private class OutgoingListener extends ContentObserver {

		public OutgoingListener(Handler handler) {
			super(handler);
		}

		@Override
		public void onChange(boolean selfChange) {
			super.onChange(selfChange);

			prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
			//single response disabled and should reply
			if (Long.valueOf(prefs.getString("single_response_preference", "0")) != 0 && shouldReply()) {
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
						Log.d(TAG, "You sent a message to: " + newNumber);
					}
				}
				cursor.close();
			}
		}
	}
}
