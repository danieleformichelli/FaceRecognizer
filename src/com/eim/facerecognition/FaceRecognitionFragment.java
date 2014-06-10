package com.eim.facerecognition;

import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.eim.R;
import com.eim.utilities.FaceRecognizerMainActivity;
import com.eim.utilities.Swipeable;

public class FaceRecognitionFragment extends Fragment implements Swipeable {
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

	@Override
	public void swipeOut(boolean right) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void swipeIn(boolean right) {
		// TODO Auto-generated method stub
		
	}
}
