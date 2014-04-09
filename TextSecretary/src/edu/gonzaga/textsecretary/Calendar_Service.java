package edu.gonzaga.textsecretary;

import java.util.Calendar;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.CalendarContract.Calendars;
import android.provider.CalendarContract.Events;
import android.util.Log;
import android.widget.Toast;


public class Calendar_Service extends Activity {
	private Context context;
	
	public Calendar_Service(Context context){
		this.context = context;
	}
	
	public void getCalendars(){
		String [] projection = new String[]{
				Events.DTSTART,
				Events.DTEND,
				Events.AVAILABILITY};
		
		Calendar cStart = Calendar.getInstance();
		Calendar cEnd = Calendar.getInstance();
		cEnd.add(Calendar.HOUR, 2);
		
		String selection = "((dtstart >= " + cStart.getTimeInMillis() + ") AND (dtend <= " + cEnd.getTimeInMillis() + ") AND (availability == 0))";
		//String[] selectionArgs = new String[] {startString, endString};
		
		Cursor calendarCursor = context.getContentResolver().query(Events.CONTENT_URI, projection, selection, null, null);
		
		if (calendarCursor.moveToFirst()){
			Toast.makeText(context, "Event Present", Toast.LENGTH_SHORT).show();
			Log.d("CALENDAR", "event present");
		}
		else{
			Toast.makeText(context, "Event not present", Toast.LENGTH_SHORT).show();
			Log.d("CALENDAR", "not event");
		}
	}
	
	public void getCurrentEvent(){
		
	}
}
