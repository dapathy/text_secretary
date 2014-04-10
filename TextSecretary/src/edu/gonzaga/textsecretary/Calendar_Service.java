package edu.gonzaga.textsecretary;

import java.util.Calendar;

import android.content.Context;
import android.database.Cursor;
import android.provider.CalendarContract.Events;
import android.util.Log;
import android.widget.Toast;

public class Calendar_Service {
	private Context context;
	
	public Calendar_Service(Context context){
		this.context = context;
	}
	
	boolean inEvent(){
		String [] projection = new String[]{
				Events.DTSTART,
				Events.DTEND,
				Events.AVAILABILITY};
		
		Calendar cStart = Calendar.getInstance();
		Calendar cEnd = Calendar.getInstance();
		cEnd.add(Calendar.HOUR, 2);
		
		String selection = "((dtstart >= " + cStart.getTimeInMillis() + ") AND (dtend <= " + cEnd.getTimeInMillis() + ") AND (availability == 0))";
		
		Cursor calendarCursor = context.getContentResolver().query(Events.CONTENT_URI, projection, selection, null, null);
		
		if (calendarCursor.moveToFirst()){
			Toast.makeText(context, "Event Present", Toast.LENGTH_SHORT).show();
			Log.d("CALENDAR", "event present");
			return true;
		}
		else{
			Toast.makeText(context, "Event not present", Toast.LENGTH_SHORT).show();
			Log.d("CALENDAR", "not event");
			return false;
		}
	}
}
