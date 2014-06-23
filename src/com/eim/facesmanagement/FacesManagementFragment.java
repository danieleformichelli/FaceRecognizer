package com.eim.facesmanagement;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.Fragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ExpandableListView;
import android.widget.TextView;
import android.widget.Toast;

import com.eim.R;
import com.eim.facedetection.FaceDetectionActivity;
import com.eim.facerecognition.EIMFaceRecognizer;
import com.eim.facesmanagement.PeopleAdapter.PeopleAdapterListener;
import com.eim.facesmanagement.PhotoGallery.PhotoGalleryListener;
import com.eim.facesmanagement.peopledb.PeopleDatabase;
import com.eim.facesmanagement.peopledb.Person;
import com.eim.facesmanagement.peopledb.Photo;
import com.eim.utilities.FaceRecognizerMainActivity;
import com.eim.utilities.FaceRecognizerMainActivity.OnOpenCVLoaded;
import com.eim.utilities.PhotoAdapter;
import com.eim.utilities.Swipeable;

public class FacesManagementFragment extends Fragment implements Swipeable,
		OnOpenCVLoaded {
	private static final String TAG = "FacesManagementFragment";
	private static final int FACE_DETECTION_AND_EXTRACTION = 1;

	private boolean retrainModel;

	private FaceRecognizerMainActivity activity;
	private ExpandableListView mPeopleList;
	private PeopleAdapter mPeopleAdapter;
	private TextView addPerson, noPeopleMessage;
	private View mainLayout;
	private EIMFaceRecognizer mFaceRecognizer;

	private PeopleDatabase mPeopleDatabase;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		mainLayout = inflater.inflate(R.layout.fragment_faces_management,
				container, false);

		return mainLayout;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		activity = (FaceRecognizerMainActivity) getActivity();

		mPeopleDatabase = PeopleDatabase.getInstance(activity);

		addPerson = (TextView) mainLayout
				.findViewById(R.id.faces_management_add_person);
		addPerson.setOnClickListener(addPersonListener);

		noPeopleMessage = (TextView) mainLayout
				.findViewById(R.id.faces_management_no_people);

		mPeopleList = (ExpandableListView) mainLayout
				.findViewById(R.id.faces_management_people_list);
		mPeopleAdapter = new PeopleAdapter(activity, R.layout.person_list_item,
				R.layout.person_view, mPeopleDatabase.getPeople(),
				mPeopleAdapterListener, mPhotoGalleryListener);

		mPeopleList.setAdapter(mPeopleAdapter);
		if (mPeopleAdapter.getGroupCount() == 0)
			noPeopleMessage.setVisibility(View.VISIBLE);

		if (activity.isOpenCVLoaded() && getUserVisibleHint())
			setupFaceRecognizer();
	}

	@Override
	public void onOpenCVLoaded() {
		if (activity != null && getUserVisibleHint()) {
			setupFaceRecognizer();
			if (retrainModel)
				retrainRecognizer();
		}
	}

	private void retrainRecognizer() {
		mFaceRecognizer = activity.recreateFaceRecognizer();
		mFaceRecognizer.trainWithLoading(activity, mPeopleAdapter.getPeople());
		retrainModel = false;
	}

	@Override
	public void swipeOut(boolean toRight) {
		mFaceRecognizer = null;
	}

	@Override
	public void swipeIn(boolean fromRight) {
		if (activity.isOpenCVLoaded())
			setupFaceRecognizer();
	}

	private void setupFaceRecognizer() {
		mFaceRecognizer = activity.getFaceRecognizer();
	}

	OnClickListener addPersonListener = new OnClickListener() {
		EditPersonDialog mEditPersonDialog;

		@Override
		public void onClick(View v) {
			mEditPersonDialog = new EditPersonDialog("", addPersonListener,
					null, addPersonListener);
			mEditPersonDialog.show(getFragmentManager(), TAG);
		}

		DialogInterface.OnClickListener addPersonListener = new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				switch (which) {
				case DialogInterface.BUTTON_POSITIVE:
					mPeopleAdapterListener.addPerson(mEditPersonDialog
							.getInsertedName());
					break;
				default:
					break;
				}
			}
		};
	};

	/**
	 * Remove all the people
	 */
	public void clearPeople() {
		SparseArray<Person> people = mPeopleAdapter.getPeople();
		for (int i = 0, l = people.size(); i < l; i++)
			mPeopleAdapterListener.removePerson(people.keyAt(i));
	}

	/**
	 * Retrain the model if a parameter is changed
	 */
	public void recognitionSettingsChanged() {
		if (activity.isOpenCVLoaded())
			retrainRecognizer();
		else
			// the model will be trained on opencv load
			retrainModel = true;
	}

	PeopleAdapterListener mPeopleAdapterListener = new PeopleAdapterListener() {

		@Override
		public void addPerson(String name) {
			if (name == null || name.length() == 0) {
				Toast.makeText(
						activity,
						activity.getString(R.string.error_person_name_not_valid),
						Toast.LENGTH_SHORT).show();
				return;
			}

			int id = mPeopleDatabase.addPerson(name);

			if (id == -1) {
				Toast.makeText(
						activity,
						activity.getString(R.string.error_person_already_present),
						Toast.LENGTH_SHORT).show();
				return;
			}

			mPeopleAdapter.addPerson(id, new Person(name));

			if (mPeopleAdapter.getGroupCount() != 0)
				noPeopleMessage.setVisibility(View.GONE);
		}

		@Override
		public void editPersonName(int id, String newName) {
			if (mPeopleAdapter.editPersonName(id, newName) == false) {
				Toast.makeText(
						activity,
						activity.getString(R.string.error_person_already_present),
						Toast.LENGTH_SHORT).show();
				return;
			}

			mPeopleDatabase.editPersonName(id, newName);
		}

		@Override
		public void removePerson(int id) {
			mPeopleAdapter.removePerson(id);
			if (mPeopleAdapter.getGroupCount() == 0)
				noPeopleMessage.setVisibility(View.VISIBLE);

			mPeopleDatabase.removePerson(id);

			// A person has been removed: retrain the entire network
			mFaceRecognizer.trainWithLoading(activity, mPeopleAdapter.getPeople());
		}

		@Override
		public void addPhoto(int personId, Photo photo) {
			int photoId = mPeopleDatabase.addPhoto(personId, photo.getUrl());

			mPeopleAdapter.addPhoto(personId, photoId, photo);

			// A photo has been added: incrementally train the network
			if (mFaceRecognizer.getType().isIncrementable())
				mFaceRecognizer.incrementalTrainWithLoading(activity, photo.getUrl(),
						personId);
			else
				mFaceRecognizer.trainWithLoading(activity, mPeopleAdapter.getPeople());
		}

		@Override
		public void removePhoto(int personId, int photoId) {
			mPeopleAdapter.removePhoto(personId, photoId);
			mPeopleDatabase.removePhoto(personId, photoId);

			// A photo has been removed: retrain the entire network
			mFaceRecognizer.trainWithLoading(activity, mPeopleAdapter.getPeople());
		}
	};

	PhotoGalleryListener mPhotoGalleryListener = new PhotoGalleryListener() {

		@Override
		public void addPhoto(PhotoGallery gallery) {
			int id = (int) gallery.getTag();
			String name = mPeopleAdapter.getPersonById(id).getName();

			Intent mIntent = new Intent(activity, FaceDetectionActivity.class);
			mIntent.putExtra(FaceDetectionActivity.PERSON_ID, id);
			mIntent.putExtra(FaceDetectionActivity.PERSON_NAME, name);
			startActivityForResult(mIntent, FACE_DETECTION_AND_EXTRACTION);
		}

		@Override
		public void removeSelectedPhotos(PhotoGallery gallery) {
			PhotoAdapter mPhotoAdapter = (PhotoAdapter) gallery.getAdapter();
			List<Integer> toBeDeleted = new ArrayList<Integer>();

			int personId = (int) gallery.getTag();
			Person mPerson = mPeopleAdapter.getPersonById(personId);
			SparseArray<Photo> photos = mPerson.getPhotos();

			// i = 0 is add/delete
			for (int i = 1, l = mPhotoAdapter.getCount(); i < l; i++)
				if (mPhotoAdapter.isSelected(i))
					toBeDeleted.add(photos.keyAt(i - 1));

			for (Integer i : toBeDeleted)
				mPeopleAdapterListener.removePhoto(personId, i);
		}

	};

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode != Activity.RESULT_OK)
			return;

		switch (requestCode) {
		case FACE_DETECTION_AND_EXTRACTION:
			Bundle extras = data.getExtras();
			if (extras == null)
				return;

			int personId = extras.getInt(FaceDetectionActivity.PERSON_ID);
			String[] photoPaths = data.getExtras().getStringArray(
					FaceDetectionActivity.PHOTO_PATHS);

			for (String photoPath : photoPaths)
				mPeopleAdapterListener.addPhoto(personId, new Photo(photoPath,
						null));
			break;
		}
	}
}
