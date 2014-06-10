package com.eim.facesmanagement;

import java.util.List;

import android.os.Bundle;
import android.app.Fragment;
import android.content.DialogInterface;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ExpandableListView;
import android.widget.TextView;

import com.eim.utilities.FaceRecognizerMainActivity;
import com.eim.utilities.Swipeable;
import com.eim.R;

public class FacesManagementFragment extends Fragment implements Swipeable {
	private static final String TAG = "FacesManagementFragment";

	FaceRecognizerMainActivity activity;
	ExpandableListView peopleList;
	PeopleExpandableAdapter peopleAdapter;
	TextView addPerson;

	PeopleDatabase peopleDatabase;
	List<Person> people;

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

		peopleDatabase = new PeopleDatabase(activity);
		people = peopleDatabase.getPeople();

		addPerson = (TextView) activity
				.findViewById(R.id.faces_management_add_person);
		addPerson.setOnClickListener(addPersonListener);
		peopleList = (ExpandableListView) activity
				.findViewById(R.id.faces_management_people_list);
		peopleAdapter = new PeopleExpandableAdapter(activity,
				R.id.person_list_item_label, R.id.person_view, people,
				peopleAdapterListener);
	}

	private PeopleAdapterListener peopleAdapterListener = new PeopleAdapterListener() {

		@Override
		public void onPersonAdded(String name) {
			peopleDatabase.addPerson(name);
		}

		@Override
		public void onPersonEdited(String oldName, String newName) {
			// peopleDatabase.editPersonName(oldName, newName);
		}

		@Override
		public void onPersonRemoved(String name) {
			// peopleDatabase.removePerson(name);
		}

		@Override
		public void onPhotoAdded(String name, String photo) {
			// peopleDatabase.addPhoto(name, photo);

		}

		@Override
		public void onPhotoRemoved(String photo) {
			// peopleDatabase.removePhoto(photo);
		}
	};

	@Override
	public void swipeOut(boolean toRight) {
		Log.e(TAG, "swiped out to " + (toRight ? "right" : "left"));
	}

	@Override
	public void swipeIn(boolean fromRight) {
		Log.e(TAG, "swiped in from " + (fromRight ? "right" : "left"));
	}

	OnClickListener addPersonListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			new InsertNameDialog(addPersonOkListener, null).show(
					getFragmentManager(), TAG);
		}

		DialogInterface.OnClickListener addPersonOkListener = new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// peopleDatabase.addPerson(name);
			}
		};
	};
}
