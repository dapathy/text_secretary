package edu.gonzaga.textsecretary.activity_recognition;


public final class ActivityUtils {

    public static final String APPTAG = "ActivityRecognition";
    /*
     * Define a request code to send to Google Play services
     * This code is returned in Activity.onActivityResult
     */
    public final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;
    // Intent actions and extras for sending information from the IntentService to the Activity
    public static final String ACTION_CONNECTION_ERROR =
            "com.example.android.activityrecognition.ACTION_CONNECTION_ERROR";
    public static final String ACTION_REFRESH_STATUS_LIST =
            "com.example.android.activityrecognition.ACTION_REFRESH_STATUS_LIST";
    public static final String CATEGORY_LOCATION_SERVICES =
            "com.example.android.activityrecognition.CATEGORY_LOCATION_SERVICES";
    public static final String EXTRA_CONNECTION_ERROR_CODE =
            "com.example.android.activityrecognition.EXTRA_CONNECTION_ERROR_CODE";
    public static final String EXTRA_CONNECTION_ERROR_MESSAGE =
            "com.example.android.activityrecognition.EXTRA_CONNECTION_ERROR_MESSAGE";
    // Constants used to establish the activity update interval
    public static final int MILLISECONDS_PER_SECOND = 1000;
    public static final int DETECTION_INTERVAL_SECONDS = 60;
    public static final int DETECTION_INTERVAL_MILLISECONDS =
            MILLISECONDS_PER_SECOND * DETECTION_INTERVAL_SECONDS;

    // Used to track what type of request is in process
    public enum REQUEST_TYPE {
        ADD, REMOVE
    }
}
