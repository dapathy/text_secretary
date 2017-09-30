package edu.gonzaga.textsecretary;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.RemoteViews;

public class Widget extends AppWidgetProvider {
	private static final String TAG = "WIDGET";
	private static final String SYNC_CLICKED = "automaticWidgetSyncButtonClick";
	private SharedPreferences settings;
	private boolean SMS_Service_State;

	@Override
	public void onReceive(Context context, Intent intent) {
		super.onReceive(context, intent);

		if (SYNC_CLICKED.equals(intent.getAction())) {
			settings = PreferenceManager.getDefaultSharedPreferences(context);
			SMS_Service_State = settings.getBoolean("smsState", false);
			Intent serviceIntent = new Intent(context, SMSService.class);
			SharedPreferences.Editor editor = settings.edit();

			AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
			RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.widgetlayout);
			ComponentName textWidget = new ComponentName(context, Widget.class);

			//stop service
			if (SMS_Service_State) {
				Log.d(TAG, "widget toggling service off");
				remoteViews.setImageViewResource(R.id.imageview_icon, R.drawable.widgetoff);
				context.stopService(serviceIntent);
				editor.putBoolean("smsState", false);
			}
			//start service
			else {
				Log.d(TAG, "widget toggling service on");
				remoteViews.setImageViewResource(R.id.imageview_icon, R.drawable.widgeton);
				context.startService(serviceIntent);
				editor.putBoolean("smsState", true);
			}

			appWidgetManager.updateAppWidget(textWidget, remoteViews);
			editor.apply();
		}
	}

	@Override
	public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
		RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.widgetlayout);
		ComponentName watchWidget = new ComponentName(context, Widget.class);

		remoteViews.setOnClickPendingIntent(R.id.imageview_icon, getPendingIntent(context));

		settings = PreferenceManager.getDefaultSharedPreferences(context);
		SMS_Service_State = settings.getBoolean("smsState", false);

		setWidgetIcon(remoteViews, SMS_Service_State);

		appWidgetManager.updateAppWidget(watchWidget, remoteViews);
	}

	private PendingIntent getPendingIntent(Context context) {
		Intent intent = new Intent(context, getClass());
		intent.setAction(SYNC_CLICKED);
		return PendingIntent.getBroadcast(context, 0, intent, 0);
	}

	//sets widget icon image
	private void setWidgetIcon(RemoteViews remoteViews, boolean serviceState) {
		if (serviceState)
			remoteViews.setImageViewResource(R.id.imageview_icon, R.drawable.widgeton);
		else
			remoteViews.setImageViewResource(R.id.imageview_icon, R.drawable.widgetoff);
	}

}
