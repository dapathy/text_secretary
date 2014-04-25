package edu.gonzaga.textsecretary;

import java.util.Calendar;

import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.CalendarContract.Instances;
import android.util.Log;

public class Calendar_Service {
	private Context context;
	private long eventEnd = Long.MAX_VALUE;
	private String eventName = "";
	
	public Calendar_Service(Context context){
		this.context = context;
	}
	
	boolean inEvent(){
		String [] projection = new String[]{
				Instances.TITLE,
				Instances.BEGIN,
				Instances.END,
				Instances.AVAILABILITY};
		
		long start, end;
		//establish search time frame
		Calendar current = Calendar.getInstance();
		Calendar cStart = Calendar.getInstance();
		Calendar cEnd = Calendar.getInstance();
		cStart.roll(Calendar.DATE, false);
		cEnd.roll(Calendar.DATE, true);
		
		long cStartMillis = cStart.getTimeInMillis();
		long cEndMillis = cEnd.getTimeInMillis();
		long currentMillis = current.getTimeInMillis();
		
		//construct query
		String[] selectionArgs = new String[]{""+cStartMillis, ""+cEndMillis, ""+Instances.AVAILABILITY_BUSY};
		String selection = "((" + Instances.BEGIN + " >= ?) AND (" + Instances.END + " <= ? ) AND (" + Instances.AVAILABILITY + " == ?))";
		
		// Construct the uri with the desired date range.
		Uri.Builder builder = Instances.CONTENT_URI.buildUpon();
		ContentUris.appendId(builder, cStartMillis);
		ContentUris.appendId(builder, cEndMillis);
		
		try{
			Cursor calendarCursor = context.getContentResolver().query(builder.build(), projection, selection, selectionArgs, null);
			//iterate over all events returned by query checking existance during current time
			while (calendarCursor.moveToNext()){
				start = calendarCursor.getLong(1);
				end = calendarCursor.getLong(2);
				if (start <= currentMillis && end >= currentMillis){
					Log.d("CALENDAR", "event present");
					eventEnd = end;
					eventName = calendarCursor.getString(0);
					calendarCursor.close();
					return true;
				}
			}
			calendarCursor.close();
		}
		catch(Exception e){
			Log.d("CALENDAR", e.getMessage());
		}
		
		Log.d("CALENDAR", "not event");
		return false;
	}

	public long getEventEnd() {
		return eventEnd;
	}

	public String getEventName() {
		return eventName;
	}
	
}
