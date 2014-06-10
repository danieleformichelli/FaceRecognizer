package com.eim.facerecognition;

import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.eim.R;

public class FaceRecognitionFragment extends Fragment {
	private static final String TAG = "FaceRecognitionFragment";
	
	FaceRecognizerMainActivity activity;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_face_recognition, container, false);
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
