package com.eim.facerecognizer;

import java.util.ArrayList;
import java.util.List;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.os.Bundle;
import android.support.v13.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;

import com.eim.facerecognizer.fragments.FaceDetectionFragment;
import com.eim.facerecognizer.fragments.FaceRecognitionFragment;
import com.eim.facerecognizer.fragments.FacesManagementFragment;
import com.eim.facerecognizer.fragments.SettingsFragment;
import com.formichelli.facerecognizer.R;

public class FaceRecognizerMainActivity extends Activity {
	private static final String TAG = "FaceRecognizerMainActivity";

	/**
	 * The {@link android.support.v4.view.PagerAdapter} that will provide
	 * fragments for each of the sections. We use a {@link FragmentPagerAdapter}
	 * derivative, which will keep every loaded fragment in memory. If this
	 * becomes too memory intensive, it may be best to switch to a
	 * {@link android.support.v13.app.FragmentStatePagerAdapter}.
	 */
	SectionsPagerAdapter mSectionsPagerAdapter;
	FaceDetectionFragment mFaceDetectionFragment;
	FaceRecognitionFragment mFaceRecognitionFragment;
	FacesManagementFragment mFacesManagementFragment;
	SettingsFragment mSettingsFragment;

	ViewPager mViewPager;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_face_recognizer_main);

		mFaceDetectionFragment = new FaceDetectionFragment();
		mFaceRecognitionFragment = new FaceRecognitionFragment();
		mFacesManagementFragment = new FacesManagementFragment();
		mSettingsFragment = new SettingsFragment();

		List<Fragment> sections = new ArrayList<Fragment>();
		sections.add(mFaceDetectionFragment);
		sections.add(mFaceRecognitionFragment);
		sections.add(mFacesManagementFragment);
		sections.add(mSettingsFragment);

		// Create the adapter that will return a fragment for each of the three
		// primary sections of the activity.
		mSectionsPagerAdapter = new SectionsPagerAdapter(getFragmentManager(),
				sections);

		// Set up the ViewPager with the sections adapter.
		mViewPager = (ViewPager) findViewById(R.id.pager);
		mViewPager.setAdapter(mSectionsPagerAdapter);
	}

	/**
	 * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
	 * one of the sections/tabs/pages.
	 */
	public class SectionsPagerAdapter extends FragmentPagerAdapter {
		List<Fragment> sections;

		public SectionsPagerAdapter(FragmentManager fm, List<Fragment> sections) {
			super(fm);

			this.sections = sections;
		}

		@Override
		public Fragment getItem(int position) {
			if (sections == null)
				throw new IllegalStateException(
						"Cannot call getItem if sections is null");

			if (sections.size() < position + 1)
				throw new IllegalStateException("Index out of bound: position "
						+ position + ", size " + sections.size());

			return sections.get(position);
		}

		@Override
		public int getCount() {
			if (sections == null)
				throw new IllegalStateException(
						"Cannot call getItem if sections is null");

			return sections.size();
		}

		@Override
		public CharSequence getPageTitle(int position) {

			if (sections == null)
				throw new IllegalStateException(
						"Cannot call getItem if sections is null");

			return sections.get(position).toString();
		}
	}

	/**
	 * OpenCV initialization
	 */
	@Override
	public void onResume() {
		super.onResume();
		OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_9, this,
				new BaseLoaderCallback(this) {
					@Override
					public void onManagerConnected(int status) {
						switch (status) {
						case LoaderCallbackInterface.SUCCESS:
							Log.i(TAG, "OpenCV loaded successfully");
							break;
						default:
							Log.i(TAG, "OpenCV connection error: " + status);
							super.onManagerConnected(status);
						}
					}
				});
	}
}
