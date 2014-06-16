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
		
		detectionScaleFactorKey = mContext.getString(R.string.detection_scale_factor);
		detectionScaleFactorDefault = mContext.getString(R.string.detection_scale_factor_default);
		
		detectionMinNeighborsKey = mContext.getString(R.string.detection_min_neighbors);
		detectionMinNeighborsDefault = mContext.getString(R.string.detection_min_neighbors_default);
		
		detectionMinRelativeFaceSizeKey = mContext.getString(R.string.detection_min_relative_face_size);
		detectionMinRelativeFaceSizeDefault = mContext.getString(R.string.detection_min_relative_face_size_default);
		
		detectionMaxRelativeFaceSizeKey = mContext.getString(R.string.detection_max_relative_face_size);
		detectionMaxRelativeFaceSizeDefault = mContext.getString(R.string.detection_max_relative_face_size_default);
		
		detectionDetectorTypeKey = mContext.getString(R.string.detection_detector_type);
		detectionDetectorTypeDefault = mContext.getString(R.string.detection_detector_type_on);
		
		managementNumberOfColumnsLandscapeKey = mContext.getString(R.string.management_number_of_gallery_columns_landscape);
		managementNumberOfColumnsLandscapeDefault = mContext.getString(R.string.management_number_of_gallery_columns_landscape);
		
		managementNumberOfColumnsPortraitKey = mContext.getString(R.string.management_number_of_gallery_columns_portrait);
		managementNumberOfColumnsPortraitDefault = mContext.getString(R.string.management_number_of_gallery_columns_portrait);
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
	private String detectionScaleFactorKey;
	private String detectionScaleFactorDefault;
	public Double detectionScaleFactor () {
		return Double.parseDouble(mSharedPreferences.getString(detectionScaleFactorKey, detectionScaleFactorDefault));
	}
	
	private String detectionMinNeighborsKey;
	private String detectionMinNeighborsDefault;
	public int detectionMinNeighbors () {
		return Integer.parseInt(mSharedPreferences.getString(detectionMinNeighborsKey , detectionMinNeighborsDefault));
	}
	
	private String detectionMinRelativeFaceSizeKey;
	private String detectionMinRelativeFaceSizeDefault;
	public double detectionMinRelativeFaceSize () {
		return Double.parseDouble(mSharedPreferences.getString(detectionMinRelativeFaceSizeKey, detectionMinRelativeFaceSizeDefault));
	}
	
	private String detectionMaxRelativeFaceSizeKey;
	private String detectionMaxRelativeFaceSizeDefault;
	public double detectionMaxRelativeFaceSize() {
		return Double.parseDouble(mSharedPreferences.getString(detectionMaxRelativeFaceSizeKey, detectionMaxRelativeFaceSizeDefault));
	}
	
	private String managementNumberOfColumnsLandscapeKey;
	private String managementNumberOfColumnsLandscapeDefault;
	public int managementNumberOfColumnsLandscape () {
		return Integer.parseInt(mSharedPreferences.getString(managementNumberOfColumnsLandscapeKey, managementNumberOfColumnsLandscapeDefault));
	}
	
	private String managementNumberOfColumnsPortraitKey;
	private String managementNumberOfColumnsPortraitDefault;
	public int managementNumberOfColumnsPortrait () {
		return Integer.parseInt(mSharedPreferences.getString(managementNumberOfColumnsPortraitKey, managementNumberOfColumnsPortraitDefault));
	}
	
	private String detectionDetectorTypeKey;
	private String detectionDetectorTypeDefault;
	public String detectionDetectorType() {
		return mSharedPreferences.getString(detectionDetectorTypeKey, detectionDetectorTypeDefault);
	}
}
