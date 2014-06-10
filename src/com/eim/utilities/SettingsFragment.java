package com.eim.utilities;

import android.os.Bundle;
import android.preference.PreferenceFragment;

import com.eim.facerecognition.FaceRecognizerMainActivity;
import com.eim.R;

public class SettingsFragment extends PreferenceFragment {
	private static final String TAG = "SettingsFragment";
	
	FaceRecognizerMainActivity activity;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.preferences);
    }

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
	}
	
	@Override
	public String toString(){
		return TAG;
	}
}
