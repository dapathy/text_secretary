package edu.gonzaga.textsecretary.activity_recognition;

import android.app.Activity;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;

import edu.gonzaga.textsecretary.activity_recognition.ActivityUtils.REQUEST_TYPE;

public class ActivityRecognizer extends Service {
	
	private REQUEST_TYPE mRequestType;
	private DetectionRequester mDetectionRequester;
    private DetectionRemover mDetectionRemover;
	
	@Override
	public void onCreate () {
        super.onCreate();
        mDetectionRequester = new DetectionRequester(getApplicationContext());
        mDetectionRemover = new DetectionRemover(getApplicationContext());
        
        startUpdates();
	}
	
	@Override
	public void onDestroy() {
		stopUpdates();
		super.onDestroy();
	}
    
    private boolean servicesConnected() {
        // Check that Google Play services is available
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(getApplicationContext());

        // If Google Play services is available
        if (ConnectionResult.SUCCESS == resultCode) {

            return true;

        // Google Play services was not available for some reason
        } else {
            return false;
        }
    }
	
	//start listening for updates
	public void startUpdates() {

        // Check for Google Play services
        if (!servicesConnected()) {
            return;
        }

        /*
         * Set the request type. If a connection error occurs, and Google Play services can
         * handle it, then onActivityResult will use the request type to retry the request
         */
        mRequestType = ActivityUtils.REQUEST_TYPE.ADD;

        // Pass the update request to the requester object
        mDetectionRequester.requestUpdates();
    }
	
	//stop checking for activity updates
	public void stopUpdates() {

        // Check for Google Play services
        if (!servicesConnected()) {
            return;
        }

        /*
         * Set the request type. If a connection error occurs, and Google Play services can
         * handle it, then onActivityResult will use the request type to retry the request
         */
        mRequestType = ActivityUtils.REQUEST_TYPE.REMOVE;

        // Pass the remove request to the remover object
        mDetectionRemover.removeUpdates(mDetectionRequester.getRequestPendingIntent());

        /*
         * Cancel the PendingIntent. Even if the removal request fails, canceling the PendingIntent
         * will stop the updates.
         */
        mDetectionRequester.getRequestPendingIntent().cancel();
    }
	
	/*
     * Handle results returned to this Activity by other Activities started with
     * startActivityForResult(). In particular, the method onConnectionFailed() in
     * DetectionRemover and DetectionRequester may call startResolutionForResult() to
     * start an Activity that handles Google Play services problems. The result of this
     * call returns here, to onActivityResult.
     */
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {

        // Choose what to do based on the request code
        switch (requestCode) {

            // If the request code matches the code sent in onConnectionFailed
            case ActivityUtils.CONNECTION_FAILURE_RESOLUTION_REQUEST :

                switch (resultCode) {
                    // If Google Play services resolved the problem
                    case Activity.RESULT_OK:

                        // If the request was to start activity recognition updates
                        if (ActivityUtils.REQUEST_TYPE.ADD == mRequestType) {

                            // Restart the process of requesting activity recognition updates
                            mDetectionRequester.requestUpdates();

                        // If the request was to remove activity recognition updates
                        } else if (ActivityUtils.REQUEST_TYPE.REMOVE == mRequestType ){

                                /*
                                 * Restart the removal of all activity recognition updates for the 
                                 * PendingIntent.
                                 */
                                mDetectionRemover.removeUpdates(
                                    mDetectionRequester.getRequestPendingIntent());

                        }
                    break;

                    // If any other result was returned by Google Play services
                    default:

                        // Report that Google Play services was unable to resolve the problem.
                        Log.e(ActivityUtils.APPTAG, "google play couldn't do anything useful");
                }

            // If any other request code was received
            default:
               // Report that this Activity received an unknown requestCode
               Log.e(ActivityUtils.APPTAG, "I'm not sure what happened but it wasn't good");

               break;
        }
    }

	@Override
	public IBinder onBind(Intent arg0) {
		return null;
	}
}
