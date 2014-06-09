package com.eim.facerecognizer.fragments;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.eim.facerecognizer.FaceRecognizerMainActivity;
import com.formichelli.facerecognizer.R;

public class FaceDetectionFragment extends Fragment {
	private static final String TAG = "FaceDetectionFragment";
	
	FaceRecognizerMainActivity activity;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_face_detection, container, false);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
	}
	
	@Override
	public String toString(){
		return TAG;
	}
}
