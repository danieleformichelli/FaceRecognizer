package com.eim.facesmanagement;

import java.util.List;

import android.os.Bundle;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ExpandableListView;
import android.widget.TextView;
import android.widget.Toast;

import com.eim.utilities.FaceRecognizerMainActivity;
import com.eim.utilities.Swipeable;
import com.eim.R;

public class FacesManagementFragment extends Fragment implements
		PeopleAdapterListener, Swipeable {
	private static final String TAG = "FacesManagementFragment";

	FaceRecognizerMainActivity activity;
	ExpandableListView peopleList;
	PeopleAdapter peopleAdapter;
	TextView addPerson, noPeopleMessage;
	View layout;

	PeopleDatabase peopleDatabase;
	List<Person> people;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		layout = inflater.inflate(R.layout.fragment_faces_management,
				container, false);
		return layout;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		activity = (FaceRecognizerMainActivity) getActivity();

		peopleDatabase = new PeopleDatabase(activity);

		addPerson = (TextView) layout
				.findViewById(R.id.faces_management_add_person);
		addPerson.setOnClickListener(addPersonListener);

		noPeopleMessage = (TextView) layout
				.findViewById(R.id.faces_management_no_people);

		peopleList = (ExpandableListView) layout
				.findViewById(R.id.faces_management_people_list);
		peopleList.setOnItemLongClickListener(onItemLongClickListener);
		peopleAdapter = new PeopleAdapter(activity, R.layout.person_list_item,
				R.layout.person_view, peopleDatabase.getPeople(), this);

		peopleList.setAdapter(peopleAdapter);
		if (peopleAdapter.getGroupCount() == 0)
			noPeopleMessage.setVisibility(View.VISIBLE);

	}

	@Override
	public void onPersonAdded(String name) {
		if (name == null || name.length() == 0) {
			Toast.makeText(activity,
					activity.getString(R.string.error_person_name_not_valid),
					Toast.LENGTH_SHORT).show();
			return;
		}

		if (peopleAdapter.addPerson(new Person(name, null)) == false) {
			Toast.makeText(activity,
					activity.getString(R.string.error_person_already_present),
					Toast.LENGTH_SHORT).show();
		} else {
			if (peopleAdapter.getGroupCount() != 0)
				noPeopleMessage.setVisibility(View.GONE);
			// peopleDatabase.addPerson(name);
		}
	}

	@Override
	public void onPersonEdited(String oldName, String newName) {
		if (peopleAdapter.editPerson(oldName, newName) == false) {
			Toast.makeText(activity,
					activity.getString(R.string.error_person_already_present),
					Toast.LENGTH_SHORT).show();
			return;
		}
		// peopleDatabase.editPerson(oldName, newName);
	}

	@Override
	public void onPersonRemoved(String name) {
		peopleAdapter.removePerson(name);
		if (peopleAdapter.getGroupCount() == 0)
			noPeopleMessage.setVisibility(View.VISIBLE);

		// peopleDatabase.removePerson(name);
	}

	@Override
	public void onPhotoAdded(String name, String photo, String features) {
		peopleAdapter.addPhoto(name, photo, features);
		// peopleDatabase.addPhoto(name, photo, features);
	}

	@Override
	public void onPhotoRemoved(String name, String photo) {
		peopleAdapter.removePhoto(name, photo);
		// peopleDatabase.removePhoto(name, photo);
	}

	@Override
	public void swipeOut(boolean toRight) {
	}

	@Override
	public void swipeIn(boolean fromRight) {
	}

	OnClickListener addPersonListener = new OnClickListener() {
		InsertNameDialog insertNameDialog;

		@Override
		public void onClick(View v) {
			insertNameDialog = new InsertNameDialog("", addPersonOkListener,
					null);
			insertNameDialog.show(getFragmentManager(), TAG);
		}

		DialogInterface.OnClickListener addPersonOkListener = new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				onPersonAdded(insertNameDialog.getName());
			}
		};
	};

	OnItemLongClickListener onItemLongClickListener = new OnItemLongClickListener() {
		@Override
		public boolean onItemLongClick(AdapterView<?> parent, View view,
				int position, long id) {
			int itemType = ExpandableListView.getPackedPositionType(id);

			if (itemType == ExpandableListView.PACKED_POSITION_TYPE_GROUP) {
				final int personPosition = ExpandableListView
						.getPackedPositionGroup(id);
				final String name = ((Person) peopleAdapter
						.getGroup(personPosition)).getName();
				askForPersonDeletion(name);
				return true;
			}

			return false;
		}

		private void askForPersonDeletion(final String name) {
			new DialogFragment() {
				@Override
				public Dialog onCreateDialog(Bundle savedInstanceState) {
					return new AlertDialog.Builder(activity)
							.setIcon(
									activity.getResources().getDrawable(
											R.drawable.action_delete))
							.setTitle(
									activity.getString(R.string.alert_dialog_delete_person_title))
							.setMessage(
									String.format(
											activity.getString(R.string.alert_dialog_delete_person_text),
											name))
							.setPositiveButton(
									activity.getString(R.string.alert_dialog_yes),
									positiveClick)
							.setNegativeButton(
									activity.getString(R.string.alert_dialog_no),
									null).create();
				}

				DialogInterface.OnClickListener positiveClick = new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						FacesManagementFragment.this.onPersonRemoved(name);
					}
				};
			}.show(getFragmentManager(), TAG);
		}
	};
}
