package edu.gonzaga.textsecretary.activity_recognition;

import android.app.PendingIntent;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.ActivityRecognition;
import com.google.android.gms.location.DetectedActivity;

import edu.gonzaga.textsecretary.Silencer;

public class ActivityRecognizer implements
		ConnectionCallbacks, OnConnectionFailedListener, ResultCallback<Status>  {

	private final static String TAG = "ActivityRecognizer";
	/**
	 * The desired time between activity detections. Larger values result in fewer activity
	 * detections while improving battery life. A value of 0 results in activity detections at the
	 * fastest possible rate. Getting frequent updates negatively impact battery life and a real
	 * app may prefer to request less frequent updates.
	 */
	static final long DETECTION_INTERVAL_IN_MILLISECONDS = 500;

	private static volatile ActivityRecognizer instance;    //instance of self
	protected boolean wasDriving;
	private int drivingConfidence;     // -2 <= dC <= 3
	private GoogleApiClient mGoogleApiClient;
	private Context mContext;

	private ActivityRecognizer(Context context) {
		mContext = context;
		buildGoogleApiClient(mContext);
	}

	public static ActivityRecognizer getInstance(Context context) {
		if (instance == null) {
			synchronized (ActivityRecognizer.class) {
				if (instance == null) {
					instance = new ActivityRecognizer(context);
				}
			}
		}

		return instance;
	}

	//start listening for updates
	public void startUpdates() {
		wasDriving = false;
		drivingConfidence = 0;

		mGoogleApiClient.connect();
		ActivityRecognition.ActivityRecognitionApi.requestActivityUpdates(
				mGoogleApiClient,
				DETECTION_INTERVAL_IN_MILLISECONDS,
				getActivityDetectionPendingIntent()
		).setResultCallback(this);
	}

	//stop checking for activity updates
	public void stopUpdates() {
		// Remove all activity updates for the PendingIntent that was used to request activity
		// updates.
		ActivityRecognition.ActivityRecognitionApi.removeActivityUpdates(
				mGoogleApiClient,
				getActivityDetectionPendingIntent()
		).setResultCallback(this);
		mGoogleApiClient.disconnect();

		//kill any remaining notification
		((NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE)).cancel(11001100);
		//ensure ringer is restored if necessary
		if (isDriving())
			Silencer.getInstance(mContext).restoreRingerMode();
		Log.d(TAG, "stopped driving service");
	}

	public boolean isDriving() {
		return drivingConfidence > 0;
	}

	@Override
	public void onConnected(@Nullable Bundle bundle) {
		Log.i(TAG, "Connected to GoogleApiClient");
	}

	@Override
	public void onConnectionSuspended(int i) {
		// The connection to Google Play services was lost for some reason. We call connect() to
		// attempt to re-establish the connection.
		Log.i(TAG, "Connection suspended");
		mGoogleApiClient.connect();
	}

	@Override
	public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
		Log.i(TAG, "Connection failed: ConnectionResult.getErrorCode() = " + connectionResult.getErrorCode());
	}

	/**
	 * Runs when the result of calling requestActivityUpdates() and removeActivityUpdates() becomes
	 * available. Either method can complete successfully or with an error.
	 *
	 * @param status The Status returned through a PendingIntent when requestActivityUpdates()
	 *               or removeActivityUpdates() are called.
	 */
	@Override
	public void onResult(@NonNull Status status) {
		if (!status.isSuccess()) {
			Log.e(TAG, "Error adding or removing activity detection: " + status.getStatusMessage());
		}
	}

	protected void raiseLowerDrivingConfidence(int activityType) {
		//if driving
		if (((activityType == DetectedActivity.IN_VEHICLE) || (activityType == DetectedActivity.ON_BICYCLE)) && (drivingConfidence < 3))
			drivingConfidence += 1;
		//if not driving and not unknown
		else if ((activityType != DetectedActivity.IN_VEHICLE) && (activityType != DetectedActivity.ON_BICYCLE) && (activityType != DetectedActivity.UNKNOWN) && (drivingConfidence > -2))
			drivingConfidence -= 1;

		Log.d(TAG, "driving confidence: " + drivingConfidence);
	}

	private synchronized void buildGoogleApiClient(Context context) {
		mGoogleApiClient = new GoogleApiClient.Builder(context)
				.addConnectionCallbacks(this)
				.addOnConnectionFailedListener(this)
				.addApi(ActivityRecognition.API)
				.build();
	}

	/**
	 * Gets a PendingIntent to be sent for each activity detection.
	 */
	private PendingIntent getActivityDetectionPendingIntent() {
		Intent intent = new Intent(mContext, ActivityRecognitionIntentService.class);

		// We use FLAG_UPDATE_CURRENT so that we get the same pending intent back when calling
		// requestActivityUpdates() and removeActivityUpdates().
		return PendingIntent.getService(mContext, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
	}
}
