package com.eim.utilities;

import android.app.Activity;
import android.os.Bundle;
import android.preference.PreferenceFragment;

import com.eim.R;

public class SettingsFragment extends PreferenceFragment implements Swipeable {
	private static final String TAG = "SettingsFragment";

	private Activity activity;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Load the preferences from an XML resource
		addPreferencesFromResource(R.xml.preferences);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		
		activity = getActivity();
	}

	@Override
	public void swipeOut(boolean right) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void swipeIn(boolean right) {
		// TODO Auto-generated method stub
		
	}
}
