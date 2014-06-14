package com.eim.facesmanagement;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Fragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
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
import com.eim.facerecognition.LBPHFaceRecognizer;
import com.eim.facesmanagement.PeopleAdapter.PeopleAdapterListener;
import com.eim.facesmanagement.PhotoGallery.PhotoGalleryListener;
import com.eim.facesmanagement.peopledb.PeopleDatabase;
import com.eim.facesmanagement.peopledb.Person;
import com.eim.facesmanagement.peopledb.Photo;
import com.eim.utilities.FaceRecognizerMainActivity;
import com.eim.utilities.FaceRecognizerMainActivity.OnOpenCVLoaded;
import com.eim.utilities.PhotoAdapter;
import com.eim.utilities.Swipeable;

@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
public class FacesManagementFragment extends Fragment implements Swipeable, OnOpenCVLoaded {
	private static final String TAG = "FacesManagementFragment";
	private static final int FACE_DETECTION_AND_EXTRACTION = 1;

	Activity mActivity;
	ExpandableListView mPeopleList;
	PeopleAdapter mPeopleAdapter;
	TextView addPerson, noPeopleMessage;
	View mainLayout;
	LBPHFaceRecognizer mFaceRecognizer;

	PeopleDatabase mPeopleDatabase;
	private boolean mOpenCVLoaded = false;

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

		mActivity = (FaceRecognizerMainActivity) getActivity();

		mPeopleDatabase = PeopleDatabase.getInstance(mActivity);

		addPerson = (TextView) mainLayout
				.findViewById(R.id.faces_management_add_person);
		addPerson.setOnClickListener(addPersonListener);

		noPeopleMessage = (TextView) mainLayout
				.findViewById(R.id.faces_management_no_people);

		mPeopleList = (ExpandableListView) mainLayout
				.findViewById(R.id.faces_management_people_list);
		mPeopleAdapter = new PeopleAdapter(mActivity,
				R.layout.person_list_item, R.layout.person_view,
				mPeopleDatabase.getPeople(), mPeopleAdapterListener,
				mPhotoGalleryListener);

		mPeopleList.setAdapter(mPeopleAdapter);
		if (mPeopleAdapter.getGroupCount() == 0)
			noPeopleMessage.setVisibility(View.VISIBLE);
		
		if (mOpenCVLoaded)
			mFaceRecognizer = LBPHFaceRecognizer.getInstance(mActivity);
	}
	
	@Override
	public void onOpenCVLoaded() {
		// Due to dynamic linking, LBPHFaceRecognizer cannot be created before OpenCV library has been loaded,
		// but due to dependency of Context, cannot be created before OnActivityCreated()
		mOpenCVLoaded = true;
		if (mActivity != null)
			mFaceRecognizer = LBPHFaceRecognizer.getInstance(mActivity);
	}
	
	@Override
	public void swipeOut(boolean toRight) {
	}

	@Override
	public void swipeIn(boolean fromRight) {
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

	PeopleAdapterListener mPeopleAdapterListener = new PeopleAdapterListener() {

		@Override
		public void addPerson(String name) {
			if (name == null || name.length() == 0) {
				Toast.makeText(
						mActivity,
						mActivity
								.getString(R.string.error_person_name_not_valid),
						Toast.LENGTH_SHORT).show();
				return;
			}

			long id = mPeopleDatabase.addPerson(name);

			if (id == -1) {
				Toast.makeText(
						mActivity,
						mActivity
								.getString(R.string.error_person_already_present),
						Toast.LENGTH_SHORT).show();
				return;
			}

			mPeopleAdapter.addPerson(id, new Person(name, null));

			if (mPeopleAdapter.getGroupCount() != 0)
				noPeopleMessage.setVisibility(View.GONE);
		}

		@Override
		public void editPersonName(long id, String newName) {
			if (mPeopleAdapter.editPersonName(id, newName) == false) {
				Toast.makeText(
						mActivity,
						mActivity
								.getString(R.string.error_person_already_present),
						Toast.LENGTH_SHORT).show();
				return;
			}

			mPeopleDatabase.editPersonName(id, newName);
		}

		@Override
		public void removePerson(long id) {
			mPeopleAdapter.removePerson(id);
			if (mPeopleAdapter.getGroupCount() == 0)
				noPeopleMessage.setVisibility(View.VISIBLE);

			mPeopleDatabase.removePerson(id);

			// A person has been removed: retrain the entire network
			mFaceRecognizer.train(mPeopleAdapter.getPeople());
		}

		@Override
		public void addPhoto(long personId, Photo photo) {
			long photoId = mPeopleDatabase.addPhoto(personId, photo.getUrl());

			android.util.Log.e(TAG, personId + ", " + photoId);

			mPeopleAdapter.addPhoto(personId, photoId, photo);

			// A person has been removed: incrementally train the network
			mFaceRecognizer.incrementalTrain(photo.getUrl(), (int) personId);
		}

		@Override
		public void removePhoto(long personId, long photoId) {
			mPeopleAdapter.removePhoto(personId, photoId);
			mPeopleDatabase.removePhoto(photoId);

			// A photo has been removed: retrain the entire network
			mFaceRecognizer.train(mPeopleAdapter.getPeople());
		}
	};

	PhotoGalleryListener mPhotoGalleryListener = new PhotoGalleryListener() {

		@Override
		public void addPhoto(PhotoGallery gallery) {
			long id = (long) gallery.getTag();
			String name = mPeopleAdapter.getPersonById(id).getName();

			Intent mIntent = new Intent(mActivity, FaceDetectionActivity.class);
			mIntent.putExtra(FaceDetectionActivity.PERSON_ID, id);
			mIntent.putExtra(FaceDetectionActivity.PERSON_NAME, name);
			startActivityForResult(mIntent, FACE_DETECTION_AND_EXTRACTION);
		}

		@Override
		public void removeSelectedPhotos(PhotoGallery gallery) {
			PhotoAdapter mPhotoAdapter = (PhotoAdapter) gallery.getAdapter();
			for (int i = 1, l = mPhotoAdapter.getCount(); i < l; i++)
				if (mPhotoAdapter.isSelected(i)) {
					final long personId = (long) gallery.getTag();
					final long photoId = mPeopleAdapter.getPersonById(personId)
							.getPhotos().keyAt(i);
					mPeopleAdapterListener.removePhoto(personId, photoId);
				}
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

			long personId = extras.getLong(FaceDetectionActivity.PERSON_ID);
			String photoPath = data.getExtras().getString(
					FaceDetectionActivity.PHOTO_PATH);
			mPeopleAdapterListener.addPhoto(personId,
					new Photo(photoPath, null));
			break;
		}
	}
}
