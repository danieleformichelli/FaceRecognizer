package com.eim.facesmanagement;

import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.eim.R;
import com.eim.facesmanagement.PhotoGallery.PhotoGalleryListener;
import com.eim.facesmanagement.peopledb.Person;
import com.eim.facesmanagement.peopledb.Photo;

/**
 * ExpandableAdapter that shows the people in the database
 */
public class PeopleAdapter extends BaseExpandableListAdapter {
	private static final String TAG = "PeopleAdapter";

	private Activity context;
	private SparseArray<Person> people;
	private int groupResource, childResource;
	private PeopleAdapterListener peopleAdapterListener;
	private PhotoGalleryListener photoGalleryListener;

	/**
	 * 
	 * @param context
	 *            Activity context
	 * @param groupResource
	 *            resource that contains the person's name
	 * @param childResource
	 *            resource that contains the person's photos and allow to add or
	 *            remove them
	 * @param people
	 *            list of people to be added to the adapter
	 * @param editOnClickListener
	 *            onClickListener of the edit issue button, the related issue
	 *            will be added as a tag to the associated view
	 * @param deleteOnClickListener
	 *            onClickListener of the done issue button, the related issue
	 *            will be added as a tag to the associated view
	 */
	public PeopleAdapter(Activity context, int groupResource,
			int childResource, SparseArray<Person> people,
			PeopleAdapterListener peopleAdapterListener,
			PhotoGalleryListener photoGalleryListener) {

		this.context = context;

		this.groupResource = groupResource;
		this.childResource = childResource;

		this.people = new SparseArray<Person>();
		setPeople(people);

		this.peopleAdapterListener = peopleAdapterListener;
		this.photoGalleryListener = photoGalleryListener;
	}

	@Override
	public View getGroupView(int groupPosition, boolean isExpanded,
			View convertView, ViewGroup parent) {

		if (convertView == null) {
			LayoutInflater inflater = (LayoutInflater) context
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			convertView = inflater.inflate(groupResource, parent, false);
		}

		int id = people.keyAt(groupPosition);
		Person person = people.get(id);

		TextView name = (TextView) convertView
				.findViewById(R.id.person_list_item_label);
		name.setText(person.getName());

		if (isExpanded)
			name.setCompoundDrawablesWithIntrinsicBounds(
					R.drawable.action_collapse, 0, 0, 0);
		else
			name.setCompoundDrawablesWithIntrinsicBounds(
					R.drawable.action_expand, 0, 0, 0);

		ImageView edit = (ImageView) convertView
				.findViewById(R.id.person_list_item_edit_button);
		edit.setVisibility(isExpanded ? View.VISIBLE : View.GONE);
		edit.setOnClickListener(editPersonOnClickListener);
		edit.setTag(id);

		return convertView;
	}

	@Override
	public View getChildView(int groupPosition, int childPosition,
			boolean isLastChild, View convertView, ViewGroup parent) {
		PhotoGallery mPhotoGallery;

		if (convertView == null) {
			LayoutInflater inflater = (LayoutInflater) context
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			convertView = (ViewGroup) inflater.inflate(childResource, parent,
					false);
			mPhotoGallery = (PhotoGallery) convertView
					.findViewById(R.id.person_view_gallery);
			mPhotoGallery.setPhotoGalleryListener(photoGalleryListener);
		} else {
			mPhotoGallery = (PhotoGallery) convertView
					.findViewById(R.id.person_view_gallery);
			mPhotoGallery.removeAllPhotos();
		}

		// set the id as tag so it can be retrieved later
		int id = people.keyAt(groupPosition);
		mPhotoGallery.setTag(id);

		SparseArray<Photo> photos = people.get(id).getPhotos();
		for (int i = 0, l = photos.size(); i < l; i++) {
			Photo photo = photos.valueAt(i);
			mPhotoGallery.addPhoto(photo);
		}

		return convertView;
	}

	@Override
	public int getGroupCount() {
		return people.size();
	}

	@Override
	public int getChildrenCount(int groupPosition) {
		return 1;
	}

	@Override
	public Object getGroup(int groupPosition) {
		return people.valueAt(groupPosition);
	}

	@Override
	public Object getChild(int groupPosition, int childPosition) {
		return getGroup(groupPosition);
	}

	@Override
	public boolean hasStableIds() {
		return false;
	}

	@Override
	public boolean isChildSelectable(int groupPosition, int childPosition) {
		return false;
	}

	@Override
	public long getGroupId(int groupPosition) {
		return groupPosition;
	}

	@Override
	public long getChildId(int groupPosition, int childPosition) {
		return childPosition;
	}

	public SparseArray<Person> getPeople() {
		return people;
	}

	public Person getPersonById(int id) {
		return people.get(id);
	}

	/**
	 * Add a person to the ExpandableListView, if a person with the same id
	 * already exists it is replaced
	 * 
	 * @param id
	 *            id of the person
	 * 
	 * @param person
	 *            person to add
	 */
	public void addPerson(int id, Person person) {
		if (person == null)
			throw new IllegalArgumentException("person cannot be null");

		people.put(id, person);
		notifyDataSetChanged();
	}

	/**
	 * Remove a person to the ExpandableListView
	 * 
	 * @param id
	 *            id of the person to be removed
	 */
	public boolean removePerson(int id) {
		Person mPerson = people.get(id);
		if (mPerson == null)
			return false;

		people.remove(id);
		notifyDataSetChanged();
		return true;
	}

	/**
	 * Replace all people of the ExpandableListView
	 * 
	 * @param people
	 *            new people of the ExpandableListView
	 */
	public void setPeople(SparseArray<Person> people) {
		this.people.clear();

		if (people != null) {
			for (int i = 0, l = people.size(); i < l; i++) {
				Person person = people.valueAt(i);
				if (person != null)
					addPerson(people.keyAt(i), person);
			}
		}

		notifyDataSetChanged();
	}

	/**
	 * Edit the name of a person
	 * 
	 * @param id
	 *            id of the person to be edited
	 * @param newName
	 *            new name
	 * @return true if the person exists, false otherwise
	 */
	public boolean editPersonName(int id, String newName) {
		if (newName == null)
			throw new IllegalArgumentException("newName cannot be null");

		Person mPerson = people.get(id);
		if (mPerson == null)
			return false;

		people.get(id).setName(newName);
		notifyDataSetChanged();
		return true;
	}

	/**
	 * Remove a photo from a person
	 * 
	 * @param personId
	 *            id of the person
	 * @param photoId
	 *            id of the photo
	 * @return true if both the person and the photo exist, false otherwise
	 */
	public boolean removePhoto(int personId, int photoId) {
		Person mPerson = people.get(personId);
		if (mPerson == null)
			return false;

		Photo mPhoto = mPerson.getPhotos().get(photoId);
		if (mPhoto == null)
			return false;

		mPerson.removePhoto(photoId);
		notifyDataSetChanged();
		return true;
	}

	/**
	 * Add a photo to a person. If the photoId is already present, the photo
	 * will be replaced.
	 * 
	 * @param personId
	 *            id of the person
	 * @param photoId
	 *            id of the photo
	 * @param photo
	 *            photo to be added
	 * @return true if both the person and the photo exist, false otherwise
	 */
	public boolean addPhoto(int personId, int photoId, Photo photo) {
		if (photo == null)
			throw new IllegalArgumentException("photo cannot be null");

		Person mPerson = people.get(personId);
		if (mPerson == null)
			return false;

		mPerson.addPhoto(photoId, photo);
		notifyDataSetChanged();
		return true;
	}

	private OnClickListener editPersonOnClickListener = new OnClickListener() {
		EditPersonDialog mEditPersonDialog;
		int id;

		@Override
		public void onClick(View v) {
			id = (int) v.getTag();

			mEditPersonDialog = new EditPersonDialog(people.get(id).getName(),
					editPersonListener, editPersonListener, editPersonListener);
			mEditPersonDialog.show(context.getFragmentManager(), TAG);
		}

		DialogInterface.OnClickListener editPersonListener = new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				switch (which) {
				case DialogInterface.BUTTON_POSITIVE:
					peopleAdapterListener.editPersonName(id,
							mEditPersonDialog.getInsertedName());
					break;

				case DialogInterface.BUTTON_NEUTRAL:
					askForPersonDeletion(id);
					break;
				default:
					break;
				}
			}
		};

		private void askForPersonDeletion(final int id) {
			new DialogFragment() {
				@Override
				public Dialog onCreateDialog(Bundle savedInstanceState) {
					return new AlertDialog.Builder(context)
							.setIcon(
									context.getResources().getDrawable(
											R.drawable.action_delete))
							.setTitle(
									context.getString(R.string.alert_dialog_delete_person_title))
							.setMessage(
									String.format(
											context.getString(R.string.alert_dialog_delete_person_text),
											people.get(id).getName()))
							.setPositiveButton(
									context.getString(R.string.alert_dialog_yes),
									positiveClick)
							.setNegativeButton(
									context.getString(R.string.alert_dialog_no),
									null).create();
				}

				DialogInterface.OnClickListener positiveClick = new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						peopleAdapterListener.removePerson(id);
					}
				};
			}.show(context.getFragmentManager(), TAG);
		}
	};

	public interface PeopleAdapterListener {

		/**
		 * A person has been added to the list
		 * 
		 * @param name
		 *            name of the added person
		 */
		public void addPerson(String name);

		/**
		 * The name of a person has been edited
		 * 
		 * @param id
		 *            old name of the person
		 * @param newName
		 *            new name of the person
		 */
		public void editPersonName(int id, String newName);

		/**
		 * A person has been removed from the list
		 * 
		 * @param id
		 *            name of the removed person
		 */
		public void removePerson(int id);

		/**
		 * A photo has been added to a person
		 * 
		 * @param id
		 *            id of the person
		 * @param photo
		 *            url of the photo
		 */
		public void addPhoto(int personId, Photo photo);
		
		/**
		 * Multiple photos have been added to a person
		 * 
		 * @param id
		 *            id of the person
		 * @param urls
		 *            urls of the photos
		 */
		public void addPhoto(int personId, String[] urls);

		/**
		 * A photo has been deleted
		 * 
		 * @param photoId
		 */
		public void removePhoto(int personId, int photoId);

		void removePhotos(int personId, List<Integer> toBeDeleted);

		void removePeople();
	}

}