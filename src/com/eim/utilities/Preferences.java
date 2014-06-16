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
		
		showDetectionScaleFactorKey = mContext.getString(R.string.detection_scale_factor);
		showDetectionScaleFactorDefault = mContext.getString(R.string.detection_scale_factor_default);
		
		showDetectionMinNeighborsKey = mContext.getString(R.string.detection_min_neighbors);
		showDetectionMinNeighborsDefault = mContext.getString(R.string.detection_min_neighbors_default);
		
		showDetectionMinRelativeFaceSizeKey = mContext.getString(R.string.detection_min_relative_face_size);
		showDetectionMinRelativeFaceSizeDefault = mContext.getString(R.string.detection_min_relative_face_size_default);
		
		showDetectionMaxRelativeFaceSizeKey = mContext.getString(R.string.detection_max_relative_face_size);
		showDetectionMaxRelativeFaceSizeDefault = mContext.getString(R.string.detection_max_relative_face_size_default);
		
		showManagementNumberOfColumnsLandscapeKey = mContext.getString(R.string.management_number_of_columns_landscape);
		showManagementNumberOfColumnsLandscapeDefault = mContext.getString(R.string.management_number_of_columns_landscape);
		
		showManagementNumberOfColumnsPortraitKey = mContext.getString(R.string.management_number_of_columns_portrait);
		showManagementNumberOfColumnsPortraitDefault = mContext.getString(R.string.management_number_of_columns_portrait);
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
	
	private String showDetectionScaleFactorKey;
	private String showDetectionScaleFactorDefault;
	public String showDetectionScaleFactor () {
		return mSharedPreferences.getString(showDetectionScaleFactorKey, showDetectionScaleFactorDefault);
	}
	
	private String showDetectionMinNeighborsKey;
	private String showDetectionMinNeighborsDefault;
	public String showDetectionMinNeighbors () {
		return mSharedPreferences.getString(showDetectionMinNeighborsKey , showDetectionMinNeighborsDefault);
	}
	
	private String showDetectionMinRelativeFaceSizeKey;
	private String showDetectionMinRelativeFaceSizeDefault;
	public String showDetectionMinRelativeFaceSize () {
		return mSharedPreferences.getString(showDetectionMinRelativeFaceSizeKey, showDetectionMinRelativeFaceSizeDefault);
	}
	
	private String showDetectionMaxRelativeFaceSizeKey;
	private String showDetectionMaxRelativeFaceSizeDefault;
	private String showDetectionMaxRelativeFaceSize() {
		return mSharedPreferences.getString(showDetectionMaxRelativeFaceSizeKey, showDetectionMaxRelativeFaceSizeDefault);
	}
	
	private String showManagementNumberOfColumnsLandscapeKey;
	private String showManagementNumberOfColumnsLandscapeDefault;
	private String showManagementNumberOfColumnsLandscape () {
		return mSharedPreferences.getString(showManagementNumberOfColumnsLandscapeKey, showManagementNumberOfColumnsLandscapeDefault);
	}
	
	private String showManagementNumberOfColumnsPortraitKey;
	private String showManagementNumberOfColumnsPortraitDefault;
	private String showManagementNumberOfColumnsPortrait () {
		return mSharedPreferences.getString(showManagementNumberOfColumnsPortraitKey, showManagementNumberOfColumnsPortraitDefault);
	}
}
