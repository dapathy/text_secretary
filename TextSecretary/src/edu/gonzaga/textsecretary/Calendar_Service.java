package edu.gonzaga.textsecretary;

import java.util.Calendar;

import android.content.Context;
import android.database.Cursor;
import android.provider.CalendarContract.Events;
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
				Events.TITLE,
				Events.DTSTART,
				Events.DTEND,
				Events.AVAILABILITY};
		
		long start, end;
		Calendar current = Calendar.getInstance();
		Calendar cStart = Calendar.getInstance();
		Calendar cEnd = Calendar.getInstance();
		cStart.roll(Calendar.DATE, false);
		cEnd.roll(Calendar.DATE, true);
		
		String selection = "((dtstart >= " + cStart.getTimeInMillis() + ") AND (dtend <= " + cEnd.getTimeInMillis() + ") AND (availability == 0))";
		Cursor calendarCursor = context.getContentResolver().query(Events.CONTENT_URI, projection, selection, null, null);
		
		while (calendarCursor.moveToNext()){
			start = calendarCursor.getLong(1);
			end = calendarCursor.getLong(2);
			if (start <= current.getTimeInMillis() && end >= current.getTimeInMillis()){
				Log.d("CALENDAR", "event present");
				eventEnd = end;
				eventName = calendarCursor.getString(0);
				calendarCursor.close();
				return true;
			}
		}
		
		Log.d("CALENDAR", "not event");
		calendarCursor.close();
		return false;
	}

	public long getEventEnd() {
		return eventEnd;
	}

	public String getEventName() {
		return eventName;
	}
	
}
