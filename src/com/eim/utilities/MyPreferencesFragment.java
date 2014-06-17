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
import android.widget.Toast;

import com.eim.R;
import com.eim.facesmanagement.peopledb.PeopleDatabase;

public class MyPreferencesFragment extends PreferenceFragment implements
		Swipeable {
	private Activity activity;
	private PreferenceScreen mPreferenceScreen;

	private String oldValue;

	private String clearDatabaseKey, restorePreferencesKey;

	private enum Validity {
		VALID, NOT_VALID_DETECTION_SCALE_FACTOR, NOT_VALID_DETECTION_MIN_NEIGHBORS, NOT_VALID_DETECTION_MIN_RELATIVE_FACE_SIZE, NOT_VALID_DETECTION_MAX_RELATIVE_FACE_SIZE, NOT_VALID_DETECTION_RELATIVE_FACE_SIZE_RATIO, NOT_VALID_NUMBER_OF_GALLERY_COLUMNS_PORTRAIT, NOT_VALID_NUMBER_OF_GALLERY_COLUMNS_LANDSCAPE
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

			// avoid to reevaluate settings after a restore
			if (oldValue != null) {
				int msgId;

				switch (isValid(sharedPreferences)) {
				case VALID:
					setPreferenceSummary(findPreference(key));
					return;
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
			com.eim.utilities.EIMPreferences mPreferences = com.eim.utilities.EIMPreferences
					.getInstance(activity);

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

			android.util.Log.e("NGCP",
					"" + mPreferences.numberOfGalleryColumnsPortrait());

			if (mPreferences.numberOfGalleryColumnsPortrait() < 1)
				return Validity.NOT_VALID_NUMBER_OF_GALLERY_COLUMNS_PORTRAIT;

			android.util.Log.e("NGCL",
					"" + mPreferences.numberOfGalleryColumnsLandscape());

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
		public boolean onPreferenceClick(Preference preference) {
			if (preference.getKey().compareTo(clearDatabaseKey) == 0)
				// TODO notify faces management
				PeopleDatabase.getInstance(activity).clear();

			if (preference.getKey().compareTo(restorePreferencesKey) == 0)
				//TODO

			if (preference instanceof EditTextPreference) {
				EditTextPreference etp = (EditTextPreference) preference;

				oldValue = etp.getText();
				etp.getEditText().selectAll();
			}

			return false;
		}
	};
}
