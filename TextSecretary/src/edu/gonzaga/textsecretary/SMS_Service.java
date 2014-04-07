package edu.gonzaga.textsecretary;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.IBinder;
import android.telephony.SmsMessage;
import android.util.Log;
import android.widget.Toast;

public class SMS_Service extends Service{

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

		@Override
		public void onReceive(Context context, Intent intent) {
			if(intent.getAction().equals("android.provider.Telephony.SMS_RECEIVED")){
				Toast.makeText(getApplicationContext(), "test", Toast.LENGTH_SHORT).show();
				Bundle bundle = intent.getExtras();
				SmsMessage[] msgs = null;
				String msg_from;
				if (bundle != null){
					try{
						Object[] pdus = (Object[]) bundle.get("pdus");
	                    msgs = new SmsMessage[pdus.length];
						for(int i = 0; i < msgs.length; i++){
	                        msgs[i] = SmsMessage.createFromPdu((byte[])pdus[i]);
	                        msg_from = msgs[i].getOriginatingAddress();
	                        //String msgBody = msgs[i].getMessageBody();
	                    }
					} catch(Exception e){
						Log.d("SMS", e.getMessage());
					}
				}
			}
		}
	};
	
}
