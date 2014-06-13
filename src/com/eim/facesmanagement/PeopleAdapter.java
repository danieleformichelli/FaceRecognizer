package com.eim.facesmanagement;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.LongSparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.eim.R;
import com.eim.facesmanagement.peopledb.Person;
import com.eim.facesmanagement.peopledb.Photo;

/**
 * ExpandableAdapter that shows the people in the database
 */
public class PeopleAdapter extends BaseExpandableListAdapter {
	private static final String TAG = "PeopleAdapter";

	private Activity context;
	LongSparseArray<Person> people;
	int groupResource, childResource;
	PeopleAdapterListener peopleAdapterListener;
	PhotoGalleryListener photoGalleryListener;

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
			int childResource, LongSparseArray<Person> people,
			PeopleAdapterListener peopleAdapterListener,
			PhotoGalleryListener photoGalleryListener) {

		this.context = context;

		this.groupResource = groupResource;
		this.childResource = childResource;

		this.people = new LongSparseArray<Person>();
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

		long id = people.keyAt(groupPosition);
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

		long id = people.keyAt(groupPosition);
		// set the id as tag so it can be retrieved later
		mPhotoGallery.setTag(id);

		LongSparseArray<Photo> photos = people.get(id).getPhotos();
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

	public Person getPersonById(long id) {
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
	public void addPerson(long id, Person person) {
		if (person == null)
			throw new IllegalArgumentException("person cannot be null");

		people.put(id, person);
		notifyDataSetChanged();
	}

	/**
	 * Remove a person to the ExpandableListView
	 * 
	 * @param person
	 *            person to add
	 */
	public boolean removePerson(long id) {
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
	public void setPeople(LongSparseArray<Person> people) {
		this.people.clear();

		if (people != null) {
			for (int i = 0, l = people.size(); i < l; i++) {
				Person person = people.valueAt(i);
				if (person != null)
					addPerson(people.keyAt(i), person);
			}

			// Collections.sort(this.people); TODO
		}

		notifyDataSetChanged();
	}

	private OnClickListener editPersonOnClickListener = new OnClickListener() {
		EditPersonDialog mEditPersonDialog;
		long id;

		@Override
		public void onClick(View v) {
			id = (long) v.getTag();

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
							mEditPersonDialog.getName());
					break;

				case DialogInterface.BUTTON_NEUTRAL:
					askForPersonDeletion(id);
					break;
				default:
					break;
				}
			}
		};

		private void askForPersonDeletion(final long id) {
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

	public boolean editPersonName(long id, String newName) {
		Person mPerson = people.get(id);
		if (mPerson == null)
			return false;

		people.get(id).setName(newName);
		notifyDataSetChanged();
		return true;
	}

	public boolean removePhoto(long personId, long photoId) {
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

	public boolean addPhoto(long personId, long photoId, Photo photo) {
		Person mPerson = people.get(personId);
		if (mPerson == null)
			return false;

		mPerson.addPhoto(photoId, photo);
		notifyDataSetChanged();
		return true;
	}
}