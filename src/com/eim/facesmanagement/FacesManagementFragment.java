package com.eim.facesmanagement;

import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.eim.facerecognition.FaceRecognizerMainActivity;
import com.eim.R;

public class FacesManagementFragment extends Fragment {
	private static final String TAG = "FacesManagementFragment";
	
	FaceRecognizerMainActivity activity;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_faces_management, container, false);
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
