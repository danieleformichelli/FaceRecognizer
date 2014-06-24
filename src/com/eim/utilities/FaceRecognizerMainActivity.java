package com.eim.utilities;

import java.util.ArrayList;
import java.util.List;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.Log;

import com.eim.R;
import com.eim.facedetection.FaceDetector;
import com.eim.facerecognition.EIMFaceRecognizer;
import com.eim.facerecognition.FaceRecognitionFragment;
import com.eim.facesmanagement.FacesManagementFragment;

public class FaceRecognizerMainActivity extends Activity {
	private static final String TAG = "FaceRecognizerMainActivity";

	private ViewPager mViewPager;
	private List<Fragment> sections;
	private int currentPosition;
	private SectionsPagerAdapter mSectionsPagerAdapter;
	private FaceRecognitionFragment mFaceRecognitionFragment;
	private FacesManagementFragment mFacesManagementFragment;
	private MyPreferencesFragment mPreferencesFragment;
	private FaceDetector mFaceDetector;
	private EIMFaceRecognizer mFaceRecognizer;
	private boolean isOpenCVLoaded;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_face_recognizer_main);

		// Instantiate the fragments
		mPreferencesFragment = new MyPreferencesFragment();
		mFaceRecognitionFragment = new FaceRecognitionFragment();
		mFacesManagementFragment = new FacesManagementFragment();

		// Create the sections of the adapter
		sections = new ArrayList<Fragment>();
		sections.add(mFaceRecognitionFragment);
		sections.add(mFacesManagementFragment);
		sections.add(mPreferencesFragment);

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

	@Override
	public void onResume() {
		super.onResume();
		isOpenCVLoaded = false;
		OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_9, this,
				new BaseLoaderCallback(this) {
					@Override
					public void onManagerConnected(int status) {
						switch (status) {
						case LoaderCallbackInterface.SUCCESS:
							System.loadLibrary("facerecognizer");
							setupFaceRecognizer();
							System.loadLibrary("nativedetector");
							setupFaceDetector();
							isOpenCVLoaded = true;
							mFaceRecognitionFragment.onOpenCVLoaded();
							mFacesManagementFragment.onOpenCVLoaded();
							break;
						default:
							Log.e(TAG, "OpenCV connection error: " + status);
							super.onManagerConnected(status);
						}
					}
				});
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

	public interface OnOpenCVLoaded {
		public void onOpenCVLoaded();
	}

	@Override
	public void onBackPressed() {
		askForExit();
	}

	private void askForExit() {
		new DialogFragment() {
			@Override
			public Dialog onCreateDialog(Bundle savedInstanceState) {
				Context mContext = FaceRecognizerMainActivity.this;

				return new AlertDialog.Builder(mContext)
						.setIcon(
								mContext.getResources().getDrawable(
										R.drawable.action_alert))
						.setTitle(
								mContext.getString(R.string.alert_dialog_exit_title))
						.setMessage(
								mContext.getString(R.string.alert_dialog_exit_text))
						.setPositiveButton(
								mContext.getString(R.string.alert_dialog_yes),
								positiveClick)
						.setNegativeButton(
								mContext.getString(R.string.alert_dialog_no),
								null).create();
			}

			DialogInterface.OnClickListener positiveClick = new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					finish();
				}
			};
		}.show(FaceRecognizerMainActivity.this.getFragmentManager(), TAG);
	}

	public FacesManagementFragment getFacesManagementFragment() {
		return mFacesManagementFragment;
	}

	public boolean isOpenCVLoaded() {
		return isOpenCVLoaded;
	}

	private void setupFaceRecognizer() {
		final EIMPreferences mPreferences = EIMPreferences.getInstance(this);
		final EIMFaceRecognizer.Type mRecognitionType = mPreferences
				.recognitionType();
		final EIMFaceRecognizer.CutMode mCutMode = mPreferences.
				recognitionCutMode();
		
		switch (mRecognitionType) {
		case LBPH:
			final int radius = mPreferences.LBPHRadius();
			final int neighbours = mPreferences.LBPHNeighbours();
			final int gridX = mPreferences.LBPHGridX();
			final int gridY = mPreferences.LBPHGridY();
			mFaceRecognizer = new EIMFaceRecognizer(this, mRecognitionType,
					radius, neighbours, gridX, gridY);
			break;
		case EIGEN:
			final int eigenComponents = mPreferences.EigenComponents();
			mFaceRecognizer = new EIMFaceRecognizer(this, mRecognitionType,
					eigenComponents);
		case FISHER:
			final int fisherComponents = mPreferences.FisherComponents();
			mFaceRecognizer = new EIMFaceRecognizer(this, mRecognitionType,
					fisherComponents);
			break;
		default:
			throw new IllegalArgumentException("invalid recognition type");
		}
		
		mFaceRecognizer.setCutMode(mCutMode);
	}

	public EIMFaceRecognizer getFaceRecognizer() {
		return mFaceRecognizer;
	}

	public EIMFaceRecognizer recreateFaceRecognizer() {
		EIMFaceRecognizer.deleteModelFromDisk(this);
		setupFaceRecognizer();
		return mFaceRecognizer;
	}

	public FaceDetector getFaceDetector() {
		return mFaceDetector;
	}

	public FaceDetector recreateFaceDetector() {
		setupFaceDetector();
		return mFaceDetector;
	}

	private void setupFaceDetector() {
		final EIMPreferences mPreferences = EIMPreferences.getInstance(this);

		final FaceDetector.Type type = mPreferences.detectorType();
		final FaceDetector.Classifier classifier = mPreferences
				.detectorClassifier();
		final double scaleFactor = mPreferences.detectionScaleFactor();
		final int minNeighbors = mPreferences.detectionMinNeighbors();
		final double minRelativeFaceSize = mPreferences
				.detectionMinRelativeFaceSize();
		final double maxRelativeFaceSize = mPreferences
				.detectionMaxRelativeFaceSize();

		mFaceDetector = new FaceDetector(this, type, classifier, scaleFactor,
				minNeighbors, minRelativeFaceSize, maxRelativeFaceSize);
	}
};
