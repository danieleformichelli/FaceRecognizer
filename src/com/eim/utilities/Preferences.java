package com.eim.utilities;

import com.eim.R;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class Preferences {
	private static Preferences instance;

	private SharedPreferences mSharedPreferences;

	public static Preferences getInstance(Context mContext) {
		if (instance == null)
			instance = new Preferences(mContext);

		return instance;
	}

	private Preferences(Context mContext) {
		mSharedPreferences = PreferenceManager
				.getDefaultSharedPreferences(mContext);

		showFacesManagementOnStartupKey = mContext
				.getString(R.string.general_show_management_on_startup);
		showFacesManagementOnStartupDefault = Boolean
				.valueOf(mContext
						.getString(R.string.general_show_management_on_startup_default));

		showDetectionConfirmationDialogKey = mContext
				.getString(R.string.detection_show_confirmation_dialog);
		showDetectionConfirmationDialogDefault = Boolean
				.valueOf(mContext
						.getString(R.string.detection_show_confirmation_dialog_default));
	}

	private String showFacesManagementOnStartupKey;
	private Boolean showFacesManagementOnStartupDefault;

	public boolean showFacesManagementOnStartup() {
		return mSharedPreferences.getBoolean(showFacesManagementOnStartupKey,
				showFacesManagementOnStartupDefault);
	}

	private String showDetectionConfirmationDialogKey;
	private boolean showDetectionConfirmationDialogDefault;

	public boolean showDetectionConfirmationDialog() {
		return mSharedPreferences.getBoolean(
				showDetectionConfirmationDialogKey,
				showDetectionConfirmationDialogDefault);
	}
}
