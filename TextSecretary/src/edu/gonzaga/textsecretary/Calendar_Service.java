package edu.gonzaga.textsecretary;

import android.app.Activity;
import android.database.Cursor;
import android.provider.CalendarContract.Calendars;


public class Calendar_Service extends Activity {
	String[] calendars;
	
	public void getCalendars(){
		calendars = new String[]{
	            Calendars._ID, 
	            Calendars.NAME, 
	            Calendars.ACCOUNT_NAME, 
	            Calendars.ACCOUNT_TYPE};
		Cursor calCursor = 
			      getContentResolver().
			            query(Calendars.CONTENT_URI, 
			                  calendars, 
			                  Calendars.VISIBLE + " = 1", 
			                  null, 
			                  Calendars._ID + " ASC");
		if (calCursor.moveToFirst()) {
		   do {
		      long id = calCursor.getLong(0);
		      String displayName = calCursor.getString(1);
		      // ...
		   } while (calCursor.moveToNext());
		}

	}
}
