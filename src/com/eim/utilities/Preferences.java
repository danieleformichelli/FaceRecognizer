package com.eim.utilities;

import com.eim.R;
import com.eim.facedetection.FaceDetector;

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
						
		detectionScaleFactorKey = mContext
				.getString(R.string.detection_scale_factor);
		detectionScaleFactorDefault = Float.parseFloat(mContext
				.getString(R.string.detection_scale_factor_default));

		detectionMinNeighborsKey = mContext
				.getString(R.string.detection_min_neighbors);
		detectionMinNeighborsDefault = Integer.parseInt(mContext
				.getString(R.string.detection_min_neighbors_default));

		detectionMinRelativeFaceSizeKey = mContext
				.getString(R.string.detection_min_relative_face_size);
		detectionMinRelativeFaceSizeDefault = Float.parseFloat(mContext
				.getString(R.string.detection_min_relative_face_size_default));

		detectionMaxRelativeFaceSizeKey = mContext
				.getString(R.string.detection_max_relative_face_size);
		detectionMaxRelativeFaceSizeDefault = Float.parseFloat(mContext
				.getString(R.string.detection_max_relative_face_size_default));

		detectorTypeKey = mContext.getString(R.string.detection_detector_type);
		detectorTypeDefault = mContext
				.getString(R.string.detection_detector_type_on);

		numberOfGalleryColumnsLandscapeKey = mContext
				.getString(R.string.management_number_of_gallery_columns_landscape);
		numberOfGalleryColumnsLandscapeDefault = Integer.parseInt(mContext
				.getString(R.string.management_number_of_gallery_columns_landscape));

		numberOfGalleryColumnsPortraitKey = mContext
				.getString(R.string.management_number_of_gallery_columns_portrait);
		numberOfGalleryColumnsPortraitDefault = Integer.parseInt(mContext
				.getString(R.string.management_number_of_gallery_columns_portrait));
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
	private float detectionScaleFactorDefault;

	public float detectionScaleFactor() {
		return mSharedPreferences.getFloat(
				detectionScaleFactorKey, detectionScaleFactorDefault);
	}

	private String detectionMinNeighborsKey;
	private int detectionMinNeighborsDefault;

	public int detectionMinNeighbors() {
		return mSharedPreferences.getInt(
				detectionMinNeighborsKey, detectionMinNeighborsDefault);
	}

	private String detectionMinRelativeFaceSizeKey;
	private float detectionMinRelativeFaceSizeDefault;

	public float detectionMinRelativeFaceSize() {
		return mSharedPreferences.getFloat(
				detectionMinRelativeFaceSizeKey,
				detectionMinRelativeFaceSizeDefault);
	}

	private String detectionMaxRelativeFaceSizeKey;
	private float detectionMaxRelativeFaceSizeDefault;

	public float detectionMaxRelativeFaceSize() {
		return mSharedPreferences.getFloat(
				detectionMaxRelativeFaceSizeKey,
				detectionMaxRelativeFaceSizeDefault);
	}

	private String numberOfGalleryColumnsLandscapeKey;
	private int numberOfGalleryColumnsLandscapeDefault;

	public int numberOfGalleryColumnsLandscape() {
		return mSharedPreferences.getInt(
				numberOfGalleryColumnsLandscapeKey,
				numberOfGalleryColumnsLandscapeDefault);
	}

	private String numberOfGalleryColumnsPortraitKey;
	private int numberOfGalleryColumnsPortraitDefault;

	public int numberOfGalleryColumnsPortrait() {
		return mSharedPreferences.getInt(
				numberOfGalleryColumnsPortraitKey,
				numberOfGalleryColumnsPortraitDefault);
	}

	private String detectorTypeKey;
	private String detectorTypeDefault;

	public FaceDetector.Type detectionDetectorType() { 
		return FaceDetector.Type.JAVA;
//		return mSharedPreferences.getBoolean(detectorTypeKey,
//				detectorTypeDefault) ? FaceDetector.Type.JAVA : FaceDetector.Type.NATIVE;
	}
}
