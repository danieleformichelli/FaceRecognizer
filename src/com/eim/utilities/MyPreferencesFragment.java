package com.eim.utilities;

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

public class MyPreferencesFragment extends PreferenceFragment implements
		Swipeable {
	private FaceRecognizerMainActivity activity;
	private PreferenceScreen mPreferenceScreen;
	private String oldValue;

	private String recognitionCategoryKey, detectionCategoryKey,
			clearDatabaseKey, restorePreferencesKey;

	private enum Validity {
		VALID, NOT_VALID_RECOGNITION_THRESHOLD, NOT_VALID_CUTMODE_PERCENTAGE, NOT_VALID_LBPH_RADIUS, NOT_VALID_LBPH_NEIGHBOURS, NOT_VALID_LBPH_GRID, NOT_VALID_EIGEN_COMPONENTS, NOT_VALID_FISHER_COMPONENTS, NOT_VALID_DETECTION_SCALE_FACTOR, NOT_VALID_DETECTION_MIN_NEIGHBORS, NOT_VALID_DETECTION_MIN_RELATIVE_FACE_SIZE, NOT_VALID_DETECTION_MAX_RELATIVE_FACE_SIZE, NOT_VALID_DETECTION_RELATIVE_FACE_SIZE_RATIO, NOT_VALID_NUMBER_OF_GALLERY_COLUMNS_PORTRAIT, NOT_VALID_NUMBER_OF_GALLERY_COLUMNS_LANDSCAPE
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

		activity = (FaceRecognizerMainActivity) getActivity();

		recognitionCategoryKey = activity
				.getString(R.string.preference_recognition);
		detectionCategoryKey = activity
				.getString(R.string.preference_detection);
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
	}

	@Override
	public void swipeOut(boolean right) {
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

			if (!(mPreference instanceof EditTextPreference)) {
				setPreferenceSummary(mPreference);
				signalSettingsChange(key);
				return;
			}

			if (oldValue != null) {
				int msgId;

				switch (isValid(sharedPreferences)) {
				case VALID:
					setPreferenceSummary(mPreference);
					signalSettingsChange(key);
					return;
				case NOT_VALID_RECOGNITION_THRESHOLD:
					msgId = R.string.recognition_invalid_threshold;
					break;
				case NOT_VALID_CUTMODE_PERCENTAGE:
					msgId = R.string.recognition_invalid_cutmode_percentage;
					break;
				case NOT_VALID_LBPH_RADIUS:
					msgId = R.string.recognition_lbph_invalid_radius;
					break;
				case NOT_VALID_LBPH_NEIGHBOURS:
					msgId = R.string.recognition_lbph_invalid_neighbours;
					break;
				case NOT_VALID_LBPH_GRID:
					msgId = R.string.recognition_lbph_invalid_grid;
					break;
				case NOT_VALID_EIGEN_COMPONENTS:
					msgId = R.string.recognition_eigen_invalid_components;
					break;
				case NOT_VALID_FISHER_COMPONENTS:
					msgId = R.string.recognition_fisher_invalid_components;
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

		private void signalSettingsChange(String key) {
			if (hasCategory(key, recognitionCategoryKey))
				activity.getFacesManagementFragment()
						.recognitionSettingsChanged();

			if (hasCategory(key, detectionCategoryKey))
				activity.recreateFaceDetector();
		}

		private boolean hasCategory(String preferenceKey, String categoryKey) {
			Preference category = mPreferenceScreen.findPreference(categoryKey);

			if (category == null || !(category instanceof PreferenceCategory))
				return false;

			return ((PreferenceCategory) category)
					.findPreference(preferenceKey) != null;
		}

		private Validity isValid(SharedPreferences sharedPreferences) {
			EIMPreferences mPreferences = EIMPreferences.getInstance(activity);

			if (mPreferences.recognitionThreshold() < 0
					|| mPreferences.recognitionThreshold() > 5000)
				return Validity.NOT_VALID_RECOGNITION_THRESHOLD;

			if (mPreferences.recognitionCutModePercentage() < 0
					|| mPreferences.recognitionCutModePercentage() > 25)
				return Validity.NOT_VALID_CUTMODE_PERCENTAGE;

			if (mPreferences.LBPHRadius() < 1)
				return Validity.NOT_VALID_LBPH_RADIUS;

			if (mPreferences.LBPHNeighbours() < 1)
				return Validity.NOT_VALID_LBPH_NEIGHBOURS;

			if (mPreferences.LBPHGridX() < 1 || mPreferences.LBPHGridX() < 1)
				return Validity.NOT_VALID_LBPH_GRID;

			if (mPreferences.EigenComponents() < 0)
				return Validity.NOT_VALID_EIGEN_COMPONENTS;

			if (mPreferences.FisherComponents() < 0)
				return Validity.NOT_VALID_FISHER_COMPONENTS;

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
	};

	OnPreferenceClickListener mOnPreferenceClickListener = new OnPreferenceClickListener() {

		@Override
		public boolean onPreferenceClick(Preference mPreference) {
			if (mPreference.getKey().compareTo(clearDatabaseKey) == 0) {
				activity.getFacesManagementFragment().clearPeople();

				Toast.makeText(
						activity,
						activity.getString(R.string.general_clear_database_confirmation),
						Toast.LENGTH_SHORT).show();
				return true;
			}

			if (mPreference.getKey().compareTo(restorePreferencesKey) == 0) {
				restorePreferences();

				Toast.makeText(
						activity,
						activity.getString(R.string.general_restore_default_preferences_confirmation),
						Toast.LENGTH_SHORT).show();
				return true;
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
		setPreference(R.string.recognition_lbph_radius,
				R.string.recognition_lbph_radius_default);
		setPreference(R.string.recognition_lbph_neighbors,
				R.string.recognition_lbph_neighbors_default);
		setPreference(R.string.recognition_lbph_grid_x,
				R.string.recognition_lbph_grid_x_default);
		setPreference(R.string.recognition_lbph_grid_y,
				R.string.recognition_lbph_grid_y_default);
		setPreference(R.string.recognition_eigen_components,
				R.string.recognition_eigen_components_default);
		setPreference(R.string.recognition_fisher_components,
				R.string.recognition_fisher_components_default);
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
