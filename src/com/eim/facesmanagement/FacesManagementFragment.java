package com.eim.facesmanagement;

import android.os.Bundle;
import android.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ExpandableListView;

import com.eim.utilities.FaceRecognizerMainActivity;
import com.eim.utilities.Swipeable;
import com.eim.R;

public class FacesManagementFragment extends Fragment implements Swipeable {
	private static final String TAG = "FacesManagementFragment";

	FaceRecognizerMainActivity activity;
	ExpandableListView peopleList;
	PeopleExpandableAdapter peopleAdapter;

	PeopleDatabase peopleDatabase;

	private OnClickListener deletePerson;

	private OnClickListener editPersonName;

	private OnClickListener deletePhoto;

	private OnClickListener addPhoto;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_faces_management, container,
				false);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		activity = (FaceRecognizerMainActivity) getActivity();
		
		peopleDatabase = new PeopleDatabase();
		peopleList = (ExpandableListView) activity.findViewById(R.id.faces_management_people_list);
		peopleAdapter = new PeopleExpandableAdapter(activity, R.id.person_list_item_label, R.id.person_view, peopleDatabase.getPeople(), deletePerson, editPersonName, deletePhoto, addPhoto);
	}

	@Override
	public String toString() {
		return TAG;
	}

	@Override
	public void swipeOut(boolean right) {
		Log.i(TAG, "swiped out");
	}

	@Override
	public void swipeIn(boolean right) {
		Log.i(TAG, "swiped in");
	}
}
