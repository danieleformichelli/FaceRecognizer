package com.eim.facesmanagement;

import java.util.List;

import android.app.Activity;
import android.app.Fragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ExpandableListView;
import android.widget.TextView;
import android.widget.Toast;

import com.eim.R;
import com.eim.facedetection.FaceDetectionActivity;
import com.eim.facesmanagement.peopledb.Person;
import com.eim.facesmanagement.peopledb.Photo;
import com.eim.utilities.FaceRecognizerMainActivity;
import com.eim.utilities.PhotoAdapter;
import com.eim.utilities.Swipeable;

public class FacesManagementFragment extends Fragment implements Swipeable {
	private static final String TAG = "FacesManagementFragment";
	private static final int FACE_DETECTION_AND_EXTRACTION = 1;

	Activity activity;
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
		peopleAdapter = new PeopleAdapter(activity, R.layout.person_list_item,
				R.layout.person_view, peopleDatabase.getPeople(),
				peopleAdapterListener, photoGalleryListener);

		peopleList.setAdapter(peopleAdapter);
		if (peopleAdapter.getGroupCount() == 0)
			noPeopleMessage.setVisibility(View.VISIBLE);

	}

	@Override
	public void swipeOut(boolean toRight) {
	}

	@Override
	public void swipeIn(boolean fromRight) {
	}

	OnClickListener addPersonListener = new OnClickListener() {
		EditPersonDialog insertNameDialog;

		@Override
		public void onClick(View v) {
			insertNameDialog = new EditPersonDialog("", addPersonListener,
					null, addPersonListener);
			insertNameDialog.show(getFragmentManager(), TAG);
		}

		DialogInterface.OnClickListener addPersonListener = new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				switch (which) {
				case DialogInterface.BUTTON_POSITIVE:
					peopleAdapterListener.addPerson(insertNameDialog.getName());
					break;
				default:
					break;
				}
			}
		};
	};

	PeopleAdapterListener peopleAdapterListener = new PeopleAdapterListener() {

		@Override
		public void addPerson(String name) {
			if (name == null || name.length() == 0) {
				Toast.makeText(
						activity,
						activity.getString(R.string.error_person_name_not_valid),
						Toast.LENGTH_SHORT).show();
				return;
			}

			if (peopleAdapter.addPerson(new Person(name, null)) == false) {
				Toast.makeText(
						activity,
						activity.getString(R.string.error_person_already_present),
						Toast.LENGTH_SHORT).show();
			} else {
				if (peopleAdapter.getGroupCount() != 0)
					noPeopleMessage.setVisibility(View.GONE);
				// peopleDatabase.addPerson(name);
			}
		}

		@Override
		public void editPerson(String oldName, String newName) {
			if (peopleAdapter.editPerson(oldName, newName) == false) {
				Toast.makeText(
						activity,
						activity.getString(R.string.error_person_already_present),
						Toast.LENGTH_SHORT).show();
				return;
			}
			// peopleDatabase.editPerson(oldName, newName);
		}

		@Override
		public void removePerson(String name) {
			peopleAdapter.removePerson(name);
			if (peopleAdapter.getGroupCount() == 0)
				noPeopleMessage.setVisibility(View.VISIBLE);

			// peopleDatabase.removePerson(name);
		}

		@Override
		public void addPhoto(String name, Photo photo) {
			peopleAdapter.addPhoto(name, photo);
			// peopleDatabase.addPhoto(name, photo);
		}

		@Override
		public void removePhoto(String name, Photo photo) {
			peopleAdapter.removePhoto(name, photo);
			// peopleDatabase.removePhoto(name, photo);
		}
	};

	PhotoGalleryListener photoGalleryListener = new PhotoGalleryListener() {

		@Override
		public void addPhoto(PhotoGallery gallery) {
			Intent intent = new Intent(activity,
					FaceDetectionActivity.class);
			intent.putExtra(FaceDetectionActivity.PERSON_NAME,
					(String) gallery.getTag());
			startActivityForResult(intent,
					FACE_DETECTION_AND_EXTRACTION);
		}

		@Override
		public void removeSelectedPhotos(PhotoGallery gallery) {
			PhotoAdapter galleryAdapter = (PhotoAdapter) gallery.getAdapter();
			for (int i = 1, l = galleryAdapter.getCount(); i < l; i++)
				if (galleryAdapter.isSelected(i))
					peopleAdapterListener.removePhoto(
							(String) gallery.getTag(),
							galleryAdapter.getItem(i));
		}

	};

	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode != Activity.RESULT_OK)
			return;

		switch (requestCode) {
		case FACE_DETECTION_AND_EXTRACTION:
			String personName = data.getExtras().getString(
					FaceDetectionActivity.PERSON_NAME);
			String photoPath = data.getExtras().getString(
					FaceDetectionActivity.PHOTO_PATH);
			
			peopleAdapterListener.addPhoto(personName, new Photo(photoPath,
					null));
			break;
		}
	}
}
