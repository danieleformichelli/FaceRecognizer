package com.eim.utilities;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;

import com.eim.R;
import com.eim.facerecognition.FaceRecognitionFragment;
import com.eim.facesmanagement.FacesManagementFragment;

public class FaceRecognizerMainActivity extends Activity {
	int currentPosition;

	SectionsPagerAdapter mSectionsPagerAdapter;
	FaceRecognitionFragment mFaceRecognitionFragment;
	FacesManagementFragment mFacesManagementFragment;
	SettingsFragment mSettingsFragment;

	List<Fragment> sections;
	ViewPager mViewPager;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_face_recognizer_main);

		// Instantiate the fragments
//		mFaceRecognitionFragment = new FaceRecognitionFragment();
		mFacesManagementFragment = new FacesManagementFragment();
		mSettingsFragment = new SettingsFragment();

		// Create the sections of the adapter
		sections = new ArrayList<Fragment>();
//		sections.add(mFaceRecognitionFragment);
		sections.add(mFacesManagementFragment);
		sections.add(mSettingsFragment);

		// Create the adapter that will return a fragment for each of the three
		// primary sections of the activity.
		mSectionsPagerAdapter = new SectionsPagerAdapter(getFragmentManager(),
				sections);

		// Set up the ViewPager with the sections adapter.
		mViewPager = (ViewPager) findViewById(R.id.pager);
		mViewPager.setAdapter(mSectionsPagerAdapter);
		mViewPager.setOnPageChangeListener(mOnPageChangeListener);

		currentPosition = 0;
	}

	OnPageChangeListener mOnPageChangeListener = new ViewPager.SimpleOnPageChangeListener() {
		@Override
		public void onPageSelected(int position) {
			boolean swipeDirection = position - currentPosition > 0;

			((Swipeable) sections.get(currentPosition))
					.swipeOut(swipeDirection);

			((Swipeable) sections.get(position)).swipeIn(!swipeDirection);

			currentPosition = position;
		}
	};
}
