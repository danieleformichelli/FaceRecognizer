package com.eim.facerecognition;

import org.opencv.android.CameraBridgeViewBase.CvCameraViewFrame;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2;
import org.opencv.core.Mat;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;

import com.eim.R;
import com.eim.utilities.FaceRecognizerMainActivity;
import com.eim.utilities.Swipeable;

public class FaceRecognitionFragment extends Fragment implements Swipeable, CvCameraViewListener2,
FaceRecognizerMainActivity.OpenCVLoadedCallback {

	private static final String TAG = "FaceRecognitionFragment";

	private boolean mOpenCVLoaded = false;

	private ControlledJavaCameraView mCameraView;

	/**
	 * Called when the fragment is first created.
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		
		return inflater.inflate(R.layout.fragment_face_recognition, container, false);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		// XXX da gestire in uscita?
		getActivity().getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		
		mCameraView = (ControlledJavaCameraView) getActivity().findViewById(R.id.face_recognition_surface_view);

		mCameraView.setCvCameraViewListener(this);
	}

	@Override
	public String toString() {
		return TAG;
	}

	@Override
	public void swipeOut(boolean right) {
		mCameraView.disableView();
	}

	@Override
	public void swipeIn(boolean right) {
		mCameraView.enableView();
	}
	
	public void onResume() {
		super.onResume();
		if (mOpenCVLoaded)
			mCameraView.enableView();
	}

	@Override
	public void onPause() {
		
		if (mCameraView != null)
            mCameraView.disableView();
		
		super.onPause();
	}
	
	@Override
	public void onOpenCVLoaded() {
		mOpenCVLoaded = true;
		if (mCameraView != null)
			mCameraView.enableView();
	}
	
	@Override
	public void onCameraViewStarted(int width, int height) {
		/*for (Size s: mCameraView.getResolutionList())
			if (s.width == 320) {
				mCameraView.setResolution(s);
				break;
			}*/
	}

	@Override
	public void onCameraViewStopped() {

	}

	@Override
	public Mat onCameraFrame(CvCameraViewFrame inputFrame) {
		return inputFrame.rgba();
	}
}
