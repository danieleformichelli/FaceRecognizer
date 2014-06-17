package com.eim.utilities;

import com.eim.R;
import com.eim.facedetection.FaceDetector;
import com.eim.facerecognition.FaceRecognitionFragment;

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

		getKeysAndDefaultValues(mContext);
	}

	private String showFacesManagementOnStartupKey;
	private String showFacesManagementOnStartupDefault;

	public boolean showFacesManagementOnStartup() {
		return Boolean.valueOf(mSharedPreferences.getString(
				showFacesManagementOnStartupKey,
				showFacesManagementOnStartupDefault));
	}

	private String showDetectionConfirmationDialogKey;
	private String showDetectionConfirmationDialogDefault;

	public boolean showDetectionConfirmationDialog() {
		return Boolean.valueOf(mSharedPreferences.getString(
				showDetectionConfirmationDialogKey,
				showDetectionConfirmationDialogDefault));
	}

	private String detectionScaleFactorKey;
	private String detectionScaleFactorDefault;

	public float detectionScaleFactor() {
		return Float.parseFloat(mSharedPreferences.getString(
				detectionScaleFactorKey, detectionScaleFactorDefault));
	}

	private String detectionMinNeighborsKey;
	private String detectionMinNeighborsDefault;

	public int detectionMinNeighbors() {
		return Integer.parseInt(mSharedPreferences.getString(detectionMinNeighborsKey,
				detectionMinNeighborsDefault));
	}

	private String detectionMinRelativeFaceSizeKey;
	private String detectionMinRelativeFaceSizeDefault;

	public float detectionMinRelativeFaceSize() {
		return Float.parseFloat(mSharedPreferences.getString(detectionMinRelativeFaceSizeKey,
				detectionMinRelativeFaceSizeDefault));
	}

	private String detectionMaxRelativeFaceSizeKey;
	private String detectionMaxRelativeFaceSizeDefault;

	public float detectionMaxRelativeFaceSize() {
		return Float.parseFloat(mSharedPreferences.getString(detectionMaxRelativeFaceSizeKey,
				detectionMaxRelativeFaceSizeDefault));
	}

	private String numberOfGalleryColumnsLandscapeKey;
	private String numberOfGalleryColumnsLandscapeDefault;

	public int numberOfGalleryColumnsLandscape() {
		return Integer.parseInt(mSharedPreferences.getString(numberOfGalleryColumnsLandscapeKey,
				numberOfGalleryColumnsLandscapeDefault));
	}

	private String numberOfGalleryColumnsPortraitKey;
	private String numberOfGalleryColumnsPortraitDefault;

	public int numberOfGalleryColumnsPortrait() {
		return Integer.parseInt(mSharedPreferences.getString(numberOfGalleryColumnsPortraitKey,
				numberOfGalleryColumnsPortraitDefault));
	}

	private String detectorTypeKey;
	private String detectorTypeDefault;

	public FaceDetector.Type detectorType() {
		return (Boolean.valueOf(mSharedPreferences.getString(detectorTypeKey,
				detectorTypeDefault)) ? FaceDetector.Type.JAVA
				: FaceDetector.Type.NATIVE);
	}

	private String recognitionTypeKey;

	public FaceRecognitionFragment.Type recognitionType() {
		int current = mSharedPreferences.getInt(recognitionTypeKey,
				R.string.recognition_type_default);
		switch (current) {
		case 0:
			return FaceRecognitionFragment.Type.EIGEN;
		case 1:
			return FaceRecognitionFragment.Type.FISHER;
		case 2:
			return FaceRecognitionFragment.Type.LBPH;
		default:
			return null;
		}

	}

	private void getKeysAndDefaultValues(Context mContext) {
		showFacesManagementOnStartupKey = mContext
				.getString(R.string.general_show_management_on_startup);
		showFacesManagementOnStartupDefault = mContext
				.getString(R.string.general_show_management_on_startup_default);

		showDetectionConfirmationDialogKey = mContext
				.getString(R.string.detection_show_confirmation_dialog);
		showDetectionConfirmationDialogDefault = mContext
				.getString(R.string.detection_show_confirmation_dialog_default);

		detectionScaleFactorKey = mContext
				.getString(R.string.detection_scale_factor);
		detectionScaleFactorDefault = mContext
				.getString(R.string.detection_scale_factor_default);

		detectionMinNeighborsKey = mContext
				.getString(R.string.detection_min_neighbors);
		detectionMinNeighborsDefault = mContext
				.getString(R.string.detection_min_neighbors_default);

		detectionMinRelativeFaceSizeKey = mContext
				.getString(R.string.detection_min_relative_face_size);
		detectionMinRelativeFaceSizeDefault = mContext
				.getString(R.string.detection_min_relative_face_size_default);

		detectionMaxRelativeFaceSizeKey = mContext
				.getString(R.string.detection_max_relative_face_size);
		detectionMaxRelativeFaceSizeDefault = mContext
				.getString(R.string.detection_max_relative_face_size_default);

		detectorTypeKey = mContext.getString(R.string.detection_detector_type);
		detectorTypeDefault = mContext
				.getString(R.string.detection_detector_type_on);

		numberOfGalleryColumnsLandscapeKey = mContext
				.getString(R.string.management_number_of_gallery_columns_landscape);
		numberOfGalleryColumnsLandscapeDefault = mContext
				.getString(R.string.management_number_of_gallery_columns_landscape_default);

		numberOfGalleryColumnsPortraitKey = mContext
				.getString(R.string.management_number_of_gallery_columns_portrait);
		numberOfGalleryColumnsPortraitDefault = mContext
				.getString(R.string.management_number_of_gallery_columns_portrait_default);

		recognitionTypeKey = mContext.getString(R.string.recognition_type);
	}

}
