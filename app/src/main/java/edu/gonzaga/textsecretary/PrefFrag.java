package edu.gonzaga.textsecretary;

import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;

import edu.gonzaga.textsecretary.activity_recognition.ActivityRecognizer;

public class PrefFrag extends PreferenceFragment implements OnSharedPreferenceChangeListener {

    private String unlockMsg;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);

        //set message disability based on calendar selection
        if (getPreferenceManager().getSharedPreferences().getBoolean("calendar_preference", true)) {
            Preference messagePreference = findPreference("custom_message_preference");
            messagePreference.setEnabled(false);
        }

        changeUnlockMsg();
    }

    @Override
    public void onResume() {
        super.onResume();
        getPreferenceManager().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        getPreferenceManager().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
    }

    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference key) {
        //this is ridiculous
        if (key.toString().equals("Remove Signature " + unlockMsg)) {
            ((SettingsActivity) getActivity()).purchaseUnlock();
        }
        return false;
    }

    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        boolean smsState = sharedPreferences.getBoolean("smsState", false);
        boolean calendarOn = sharedPreferences.getBoolean("calendar_preference", false);
        boolean silencerOn = sharedPreferences.getBoolean("silence_preference", false);

        if (key.equals("calendar_preference")) {
            //set custom message enablement opposite of calendar preference
            Preference messagePreference = findPreference("custom_message_preference");
            messagePreference.setEnabled(!calendarOn);

            //handle silencer service
            if (smsState) {
                //if both enabled start service
                if (silencerOn && calendarOn)
                    Silencer.getInstance(getActivity().getApplicationContext()).startSilencerPoller();
                    //if only silencer enabled, turn off service
                else if (silencerOn)
                    Silencer.getInstance(getActivity().getApplicationContext()).stopSilencerPoller();
            }
        } else if (key.equals("driving_preference")) {
            //starts or stops driving service when selected or deselected on preference screen and sms service is already enabled
            if (smsState) {
                if (sharedPreferences.getBoolean("driving_preference", false))
                    ActivityRecognizer.getInstance(getActivity().getApplicationContext()).startUpdates();
                else
                    ActivityRecognizer.getInstance(getActivity().getApplicationContext()).stopUpdates();
            }
        } else if (key.equals("silence_preference")) {
            //starts or stops silencer service when selected or deselected on preference screen and sms service is already enabled
            if (smsState) {
                //if both enabled start service
                if (silencerOn && calendarOn)
                    Silencer.getInstance(getActivity().getApplicationContext()).startSilencerPoller();
                    //if silencer off, turn off
                else if (calendarOn)
                    Silencer.getInstance(getActivity().getApplicationContext()).stopSilencerPoller();
            }
        }
    }

    private void changeUnlockMsg() {
        Preference unlockPreference = findPreference("unlock");
        int daysLeft = RegCheck.getTrialDaysRemaining(getActivity().getApplicationContext());
        final String msgSuf = " remaining in your trial. After the trial, a signature will be appended to every auto-reply. Purchase the Unlock here to remove the signature.";

        //grammar!
        if (daysLeft == 1)
            unlockMsg = "There is 1 day" + msgSuf;
        else
            unlockMsg = "There are " + daysLeft + " days" + msgSuf;
        unlockPreference.setSummary(unlockMsg);
    }
}
