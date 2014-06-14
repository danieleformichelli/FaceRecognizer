package com.eim.utilities;

import android.app.Activity;
import android.os.Bundle;
import android.preference.PreferenceFragment;

import com.eim.R;

public class MyPreferencesFragment extends PreferenceFragment implements Swipeable {
	private Activity activity;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		addPreferencesFromResource(R.xml.preferences);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		activity = getActivity();
	}

	@Override
	public void swipeOut(boolean right) {
	}

	@Override
	public void swipeIn(boolean right) {
	}
}
