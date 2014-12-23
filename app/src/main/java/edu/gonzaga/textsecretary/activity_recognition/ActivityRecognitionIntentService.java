/*
 * Copyright (C) 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package edu.gonzaga.textsecretary.activity_recognition;

import com.google.android.gms.location.ActivityRecognitionResult;
import com.google.android.gms.location.DetectedActivity;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

/**
 * Service that receives ActivityRecognition updates. It receives updates
 * in the background, even if the main Activity is not visible.
 */
public class ActivityRecognitionIntentService extends IntentService {
	
	public ActivityRecognitionIntentService() {
        // Set the label for the service's background thread
        super("ActivityRecognitionIntentService");
    }

    /**
     * Called when a new activity detection update is available.
     */
    @Override
    protected void onHandleIntent(Intent intent) {

        // If the intent contains an update
        if (ActivityRecognitionResult.hasResult(intent)) {
        	Log.d(ActivityUtils.APPTAG, "new activity update available");
            // Get the update
            ActivityRecognitionResult result = ActivityRecognitionResult.extractResult(intent);

            // Get the most probable activity from the list of activities in the update
            DetectedActivity mostProbableActivity = result.getMostProbableActivity();

            // Get the confidence percentage for the most probable activity
            int confidence = mostProbableActivity.getConfidence();

            // Get the type of activity
            int activityType = mostProbableActivity.getType();

            // If the repository contains a type
            if (
                       // The activity has changed from the previous activity
                       activityChanged(activityType)

                       // The confidence level for the current activity is > 50%
                       && (confidence >= 50)) {

                //broadcast current type
            	broadcastActivityState(activityType);
            }
        }
    }

    /**
     * Tests to see if the activity has changed
     *
     * @param currentType The current activity type
     * @return true if the user's current activity is different from the previous most probable
     * activity; otherwise, false.
     */
    private boolean activityChanged(int currentType) {
        // If the previous type isn't the same as the current type, the activity has changed
        if (ActivityRecognizer.previousActivityType != currentType) {
        	ActivityRecognizer.previousActivityType = currentType;
            return true;

        // Otherwise, it hasn't.
        } else {
            return false;
        }
    }

    /**
     * Determine if an activity means that the user is in a vehicle.
     *
     * @param type The type of activity the user is doing (see DetectedActivity constants)
     * @return true if the user seems to be moving from one location to another, otherwise false
     */
    public static boolean isMoving(int type) {
        switch (type) {
            // These types mean that the user is probably in a vehicle
            case DetectedActivity.IN_VEHICLE:
            case DetectedActivity.ON_BICYCLE:
                return true;
            default:
                return false;
        }
    }
    
    private void broadcastActivityState(int activityType) {
    	Intent state = new Intent();
    	state.setAction("edu.gonzaga.text_secretary.activity_recognition.ACTIVITY_STATE");
    	state.putExtra("type", activityType);
    	sendBroadcast(state);
    	Log.d(ActivityUtils.APPTAG, "activity broadcast sent");
    }
}