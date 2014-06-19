package com.eim.utilities;

import android.app.Activity;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceCategory;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;
import android.preference.SwitchPreference;
import android.widget.Toast;

import com.eim.R;
import com.eim.facedetection.FaceDetector;
import com.eim.facerecognition.EIMFaceRecognizer;
import com.eim.facesmanagement.peopledb.PeopleDatabase;
import com.eim.utilities.FaceRecognizerMainActivity.OnOpenCVLoaded;

public class MyPreferencesFragment extends PreferenceFragment implements
		OnOpenCVLoaded, Swipeable {
	private Activity activity;
	private PreferenceScreen mPreferenceScreen;
	private String oldValue;

	private PeopleDatabase mPeopleDatabase;
	private EIMPreferences mPreferences;
	private EIMFaceRecognizer mFaceRecognizer;
	private FaceDetector mFaceDetector;
	private boolean mOpenCVLoaded = false;

	private String clearDatabaseKey, restorePreferencesKey;
	private String recognizerTypeKey;
	private String detectorTypeKey, classifierKey, scaleFactorKey,
			minNeighborsKey, minRelativeFaceSizeKey, maxRelativeFaceSizeKey;

	private enum Validity {
		VALID, NOT_VALID_RECOGNITION_THRESHOLD, NOT_VALID_DETECTION_SCALE_FACTOR, NOT_VALID_DETECTION_MIN_NEIGHBORS, NOT_VALID_DETECTION_MIN_RELATIVE_FACE_SIZE, NOT_VALID_DETECTION_MAX_RELATIVE_FACE_SIZE, NOT_VALID_DETECTION_RELATIVE_FACE_SIZE_RATIO, NOT_VALID_NUMBER_OF_GALLERY_COLUMNS_PORTRAIT, NOT_VALID_NUMBER_OF_GALLERY_COLUMNS_LANDSCAPE
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		addPreferencesFromResource(R.xml.preferences);

		mPreferenceScreen = getPreferenceScreen();

		initPreferences();
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		activity = getActivity();

		if (mOpenCVLoaded && mFaceRecognizer == null)
			// opencv is loaded before the fragment
			getInstances();

		getKeys();
	}

	public void onOpenCVLoaded() {
		mOpenCVLoaded = true;

		if (activity != null)
			// activity is already loaded
			getInstances();
	}

	private void getInstances() {
		mPeopleDatabase = PeopleDatabase.getInstance(activity);
		mFaceDetector = FaceDetector.getInstance(activity);
		mPreferences = EIMPreferences.getInstance(activity);
		mFaceRecognizer = EIMFaceRecognizer.getInstance(activity,
				mPreferences.recognitionType());
	}

	private void getKeys() {
		detectorTypeKey = activity.getString(R.string.detection_detector_type);
		// classifierKey = activity.getString(R.string.detection_classifier);
		scaleFactorKey = activity.getString(R.string.detection_scale_factor);
		minNeighborsKey = activity.getString(R.string.detection_min_neighbors);
		minRelativeFaceSizeKey = activity
				.getString(R.string.detection_min_relative_face_size);
		maxRelativeFaceSizeKey = activity
				.getString(R.string.detection_max_relative_face_size);

		recognizerTypeKey = activity
				.getString(R.string.recognition_recognizer_type);

		clearDatabaseKey = activity.getString(R.string.general_clear_database);
		restorePreferencesKey = activity
				.getString(R.string.general_restore_default_preferences);
	}

	@Override
	public void onResume() {
		super.onResume();
		mPreferenceScreen.getSharedPreferences()
				.registerOnSharedPreferenceChangeListener(
						mOnSharedPreferenceChangeListener);
	}

	@Override
	public void onPause() {
		super.onPause();
		mPreferenceScreen.getSharedPreferences()
				.unregisterOnSharedPreferenceChangeListener(
						mOnSharedPreferenceChangeListener);
	}

	@Override
	public void swipeIn(boolean right) {
		mPreferenceScreen.getSharedPreferences()
				.registerOnSharedPreferenceChangeListener(
						mOnSharedPreferenceChangeListener);
	}

	@Override
	public void swipeOut(boolean right) {
		mPreferenceScreen.getSharedPreferences()
				.unregisterOnSharedPreferenceChangeListener(
						mOnSharedPreferenceChangeListener);
	}

	private void initPreferences() {

		for (int i = 0; i < mPreferenceScreen.getPreferenceCount(); i++) {
			Preference mPreference = mPreferenceScreen.getPreference(i);

			if (mPreference instanceof PreferenceCategory) {
				PreferenceCategory mPreferenceCategory = (PreferenceCategory) mPreference;
				for (int j = 0; j < mPreferenceCategory.getPreferenceCount(); j++)
					initPreference(mPreferenceCategory.getPreference(j));
			} else
				initPreference(mPreference);
		}
	}

	protected void initPreference(Preference mPreference) {
		mPreference.setOnPreferenceClickListener(mOnPreferenceClickListener);
		setPreferenceSummary(mPreference);
	}

	protected void setPreferenceSummary(Preference mPreference) {
		if (mPreference instanceof EditTextPreference) {
			EditTextPreference mEditTextPreference = (EditTextPreference) mPreference;
			mEditTextPreference.setSummary(mEditTextPreference.getText());
		} else if (mPreference instanceof ListPreference) {
			ListPreference mListPreference = (ListPreference) mPreference;
			mListPreference.setSummary(mListPreference.getEntry());
		}
	}

	OnSharedPreferenceChangeListener mOnSharedPreferenceChangeListener = new OnSharedPreferenceChangeListener() {
		@Override
		public void onSharedPreferenceChanged(
				SharedPreferences sharedPreferences, String key) {
			Preference mPreference = findPreference(key);

			setPreferenceSummary(mPreference);

			if (oldValue != null) {
				int msgId;

				switch (isValid(sharedPreferences)) {
				case VALID:
					setPreferenceSummary(mPreference);
					updateInstances(mPreference);
					return;
				case NOT_VALID_RECOGNITION_THRESHOLD:
					msgId = R.string.recognition_invalid_threshold;
					break;
				case NOT_VALID_DETECTION_SCALE_FACTOR:
					msgId = R.string.detection_invalid_scale_factor;
					break;
				case NOT_VALID_DETECTION_MIN_NEIGHBORS:
					msgId = R.string.detection_invalid_min_neighbors;
					break;
				case NOT_VALID_DETECTION_MIN_RELATIVE_FACE_SIZE:
					msgId = R.string.detection_invalid_min_relative_face_size;
					break;
				case NOT_VALID_DETECTION_MAX_RELATIVE_FACE_SIZE:
					msgId = R.string.detection_invalid_max_relative_face_size;
					break;
				case NOT_VALID_DETECTION_RELATIVE_FACE_SIZE_RATIO:
					msgId = R.string.detection_invalid_relative_face_size_ratio;
					break;
				case NOT_VALID_NUMBER_OF_GALLERY_COLUMNS_LANDSCAPE:
				case NOT_VALID_NUMBER_OF_GALLERY_COLUMNS_PORTRAIT:
					msgId = R.string.invalid_number_of_gallery_columns;
					break;
				default:
					return;
				}

				Toast.makeText(activity, getString(msgId), Toast.LENGTH_SHORT)
						.show();
				restoreValue(sharedPreferences, key);
			}

		}

		private Validity isValid(SharedPreferences sharedPreferences) {
			EIMPreferences mPreferences = EIMPreferences.getInstance(activity);

			if (mPreferences.recognitionThreshold() > 1000
					|| mPreferences.recognitionThreshold() < 0)
				return Validity.NOT_VALID_RECOGNITION_THRESHOLD;

			if (mPreferences.detectionScaleFactor() <= 1)
				return Validity.NOT_VALID_DETECTION_SCALE_FACTOR;

			if (mPreferences.detectionMinNeighbors() < 1)
				return Validity.NOT_VALID_DETECTION_MIN_NEIGHBORS;

			if (mPreferences.detectionMinRelativeFaceSize() < 0
					|| mPreferences.detectionMinRelativeFaceSize() > 1)
				return Validity.NOT_VALID_DETECTION_MIN_RELATIVE_FACE_SIZE;

			if (mPreferences.detectionMaxRelativeFaceSize() < 0
					|| mPreferences.detectionMaxRelativeFaceSize() > 1)
				return Validity.NOT_VALID_DETECTION_MAX_RELATIVE_FACE_SIZE;

			if (mPreferences.detectionMinRelativeFaceSize() >= mPreferences
					.detectionMaxRelativeFaceSize())
				return Validity.NOT_VALID_DETECTION_RELATIVE_FACE_SIZE_RATIO;

			if (mPreferences.numberOfGalleryColumnsPortrait() < 1)
				return Validity.NOT_VALID_NUMBER_OF_GALLERY_COLUMNS_PORTRAIT;

			if (mPreferences.numberOfGalleryColumnsLandscape() < 1)
				return Validity.NOT_VALID_NUMBER_OF_GALLERY_COLUMNS_LANDSCAPE;

			return Validity.VALID;
		}

		private void restoreValue(SharedPreferences sharedPreferences,
				String key) {
			if (sharedPreferences == null || key == null)
				return;

			Preference mPreference = mPreferenceScreen.findPreference(key);
			if (mPreference instanceof EditTextPreference)
				((EditTextPreference) mPreference).setText(oldValue);

			Editor mEditor = sharedPreferences.edit();
			mEditor.putString(key, oldValue);
			oldValue = null;
			mEditor.commit();

		}

		private void updateInstances(Preference mPreference) {
			String key = mPreference.getKey();

			if (key.equals(recognizerTypeKey)) {
				EIMFaceRecognizer.Type mRecognizerType = EIMFaceRecognizer.Type
						.valueOf(((ListPreference) mPreference).getValue());
				mFaceRecognizer.setType(mRecognizerType);
				mFaceRecognizer.train(mPeopleDatabase.getPeople());
			} else if (key.equals(detectorTypeKey)) {
				final FaceDetector.Type mDetectorType = FaceDetector.Type
						.valueOf(((EditTextPreference) mPreference).getText());
				mFaceDetector.setDetectorType(mDetectorType);
			} else if (key.equals(classifierKey)) {
				// TODO
				mFaceDetector.setClassifier(null);
			} else if (key.equals(scaleFactorKey)) {
				final double scaleFactor = Double
						.valueOf(((EditTextPreference) mPreference).getText());
				mFaceDetector.setScaleFactor(scaleFactor);
			} else if (key.equals(minNeighborsKey)) {
				final int minNeighbors = Integer
						.valueOf(((EditTextPreference) mPreference).getText());
				mFaceDetector.setMinNeighbors(minNeighbors);
			} else if (key.equals(minRelativeFaceSizeKey)) {
				final double minRelativeFaceSize = Double
						.valueOf(((EditTextPreference) mPreference).getText());
				mFaceDetector.setMinRelativeFaceSize(minRelativeFaceSize);
			} else if (key.equals(maxRelativeFaceSizeKey)) {
				final double maxRelativeFaceSize = Double
						.valueOf(((EditTextPreference) mPreference).getText());
				mFaceDetector.setMaxRelativeFaceSize(maxRelativeFaceSize);
			}
		}
	};

	OnPreferenceClickListener mOnPreferenceClickListener = new OnPreferenceClickListener() {

		@Override
		public boolean onPreferenceClick(Preference mPreference) {
			if (mPreference.getKey().compareTo(clearDatabaseKey) == 0) {
				((FaceRecognizerMainActivity) activity)
						.getFacesManagementFragment().clearPeople();
				Toast.makeText(
						activity,
						activity.getString(R.string.general_clear_database_confirmation),
						Toast.LENGTH_SHORT).show();
			}

			if (mPreference.getKey().compareTo(restorePreferencesKey) == 0) {
				restorePreferences();
				Toast.makeText(
						activity,
						activity.getString(R.string.general_restore_default_preferences_confirmation),
						Toast.LENGTH_SHORT).show();
			}

			if (mPreference instanceof EditTextPreference) {
				EditTextPreference mEditTextPreference = (EditTextPreference) mPreference;

				oldValue = mEditTextPreference.getText();
				mEditTextPreference.getEditText().selectAll();
			}

			return false;
		}
	};

	private void restorePreferences() {
		setPreference(R.string.recognition_recognizer_type,
				R.string.recognition_recognizer_type_default);
		setPreference(R.string.recognition_threshold,
				R.string.recognition_threshold_default);
		setPreference(R.string.detection_detector_type,
				R.string.detection_detector_type_default);
		setPreference(R.string.detection_scale_factor,
				R.string.detection_scale_factor_default);
		setPreference(R.string.detection_min_neighbors,
				R.string.detection_min_neighbors_default);
		setPreference(R.string.detection_min_relative_face_size,
				R.string.detection_min_relative_face_size_default);
		setPreference(R.string.detection_max_relative_face_size,
				R.string.detection_max_relative_face_size_default);
		setPreference(R.string.management_number_of_gallery_columns_landscape,
				R.string.management_number_of_gallery_columns_landscape_default);
		setPreference(R.string.management_number_of_gallery_columns_portrait,
				R.string.management_number_of_gallery_columns_portrait_default);
	}

	private void setPreference(int preferenceId, int defaultValue) {
		String preferenceKey = activity.getString(preferenceId);
		Preference mPreference = mPreferenceScreen
				.findPreference(preferenceKey);
		Editor mEditor;

		if (mPreference instanceof EditTextPreference) {
			String value = activity.getString(defaultValue);
			((EditTextPreference) mPreference).setText(value);
			mEditor = mPreferenceScreen.getSharedPreferences().edit();
			mEditor.putString(mPreference.getKey(), value);
			mEditor.commit();
		} else if (mPreference instanceof ListPreference) {
			String value = activity.getString(defaultValue);
			((ListPreference) mPreference).setValue(value);
			mEditor = mPreferenceScreen.getSharedPreferences().edit();
			mEditor.putString(mPreference.getKey(), value);
			mEditor.commit();
		} else if (mPreference instanceof SwitchPreference) {
			Boolean value = Boolean.parseBoolean(activity
					.getString(defaultValue));
			((SwitchPreference) mPreference).setChecked(value);
			mEditor = mPreferenceScreen.getSharedPreferences().edit();
			mEditor.putBoolean(mPreference.getKey(), value);
			mEditor.commit();
		}
		setPreferenceSummary(mPreference);
	}
}
