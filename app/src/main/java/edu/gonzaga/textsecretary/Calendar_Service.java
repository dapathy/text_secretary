package edu.gonzaga.textsecretary;

import java.util.Calendar;
import java.util.Date;

import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.CalendarContract.Instances;
import android.util.Log;

public class Calendar_Service {

	private static final String TAG = "CALENDAR";
	private Context context;
	private long eventEnd = Long.MAX_VALUE;
	private String eventName = "";

    public class ProjectionAttributes {
        public static final int TITLE = 0;
        public static final int BEGIN = 1;
        public static final int END = 2;
        public static final int AVAILABILITY = 3;
        public static final int ALL_DAY = 4;
    }

	public Calendar_Service(Context context){
		this.context = context;
	}
	
	public boolean inEvent(){
        //establish search time frame
        Calendar cStart = Calendar.getInstance();
        Calendar cEnd = Calendar.getInstance();
        cStart.add(Calendar.DATE, -1);
        cEnd.add(Calendar.DATE, 1);

        long start, end;
        long currentMillis = Calendar.getInstance().getTimeInMillis();

		try{
			Cursor calendarCursor = getCursorForDates(cStart, cEnd);
			//iterate over all events returned by query checking existence during current time
			while (calendarCursor.moveToNext()){
				start = calendarCursor.getLong(ProjectionAttributes.BEGIN);
				end = calendarCursor.getLong(ProjectionAttributes.END);
				
				//adjust start time for all day event
				if (calendarCursor.getInt(ProjectionAttributes.ALL_DAY) == 1)
					start = getAllDayStart(start);
								
				//checks if during current time
				if (start <= currentMillis && end >= currentMillis){
					eventEnd = end;
					eventName = calendarCursor.getString(ProjectionAttributes.TITLE);
					Log.d(TAG, "in event");
					calendarCursor.close();
					return true;
				}
			}
			calendarCursor.close();
		}
		catch(Exception e){
			Log.d(TAG, e.getMessage());
		}
		
		Log.d(TAG, "not in event");
		return false;
	}

    public Cursor getCursorForDates (Calendar start, Calendar end) {
        String [] projection = new String[]{
                Instances.TITLE,
                Instances.BEGIN,
                Instances.END,
                Instances.AVAILABILITY,
                Instances.ALL_DAY};

        long cStartMillis = start.getTimeInMillis();
        long cEndMillis = end.getTimeInMillis();

        //construct query
        String[] selectionArgs = new String[]{""+cStartMillis, ""+cEndMillis, ""+Instances.AVAILABILITY_BUSY};
        String selection = "((" + Instances.BEGIN + " >= ?) AND (" + Instances.END + " <= ? ) AND (" + Instances.AVAILABILITY + " == ?))";

        // Construct the uri with the desired date range.
        Uri.Builder builder = Instances.CONTENT_URI.buildUpon();
        ContentUris.appendId(builder, cStartMillis);
        ContentUris.appendId(builder, cEndMillis);

        return context.getContentResolver().query(builder.build(), projection, selection, selectionArgs, null);
    }
	
	//adjust start time for all day events
	private static long getAllDayStart(long origStart){
		Calendar allStart = Calendar.getInstance();
		allStart.setTime(new Date(origStart));
		
		//if already start of day, probably fine
		if (allStart.get(Calendar.HOUR_OF_DAY) != 0){
			//if PM then increment up to beginning of next day
			if (allStart.get(Calendar.AM_PM) == Calendar.PM){
				allStart.add(Calendar.DATE, 1);
				allStart.set(Calendar.HOUR_OF_DAY, 0);
				allStart.set(Calendar.MINUTE, 0);
			}
			//else, AM so increment back to beginning of current day
			else{
				allStart.set(Calendar.HOUR_OF_DAY, 0);
				allStart.set(Calendar.MINUTE, 0);
			}
		}
		return allStart.getTimeInMillis();
	}

	public long getEventEnd() {
		return eventEnd;
	}

	public String getEventName() {
		return eventName;
	}
	
}
