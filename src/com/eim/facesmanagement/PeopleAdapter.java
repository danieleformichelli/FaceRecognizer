package com.eim.facesmanagement;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Pair;
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
	List<Person> people;
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
			int childResource, List<Person> people,
			PeopleAdapterListener peopleAdapterListener,
			PhotoGalleryListener photoGalleryListener) {

		this.context = context;

		this.groupResource = groupResource;
		this.childResource = childResource;

		this.people = new ArrayList<Person>();
		replacePeople(people);

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

		Person person = people.get(groupPosition);

		TextView name = (TextView) convertView
				.findViewById(R.id.person_list_item_label);
		name.setText(person.getName());
		name.setTag(person);

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
		edit.setTag(person.getName());

		return convertView;
	}

	@Override
	public View getChildView(int groupPosition, int childPosition,
			boolean isLastChild, View convertView, ViewGroup parent) {
		PhotoGallery gallery;

		if (convertView == null) {
			LayoutInflater inflater = (LayoutInflater) context
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			convertView = (ViewGroup) inflater.inflate(childResource, parent,
					false);
			gallery = (PhotoGallery) convertView
					.findViewById(R.id.person_view_gallery);
			gallery.setPhotoGalleryListener(photoGalleryListener);
		} else {
			gallery = (PhotoGallery) convertView
					.findViewById(R.id.person_view_gallery);
			gallery.removeAllPhotos();
		}

		gallery.setTag(people.get(groupPosition).getName());
		for (Photo photo : people.get(groupPosition).getPhotos())
			gallery.addPhoto(photo);

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
		return people.get(groupPosition);
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

	/**
	 * Add a person to the ExpandableListView
	 * 
	 * @param person
	 *            person to add
	 */
	public boolean addPerson(Person person) {
		int personIndex = getPersonIndex(person.getName());

		// person already exists
		if (personIndex != -1)
			return false;

		people.add(person);
		notifyDataSetChanged();
		return true;
	}

	/**
	 * Remove a person to the ExpandableListView
	 * 
	 * @param person
	 *            person to add
	 */
	public boolean removePerson(String name) {
		int personIndex = getPersonIndex(name);

		// person doesn't exist
		if (personIndex == -1)
			return false;

		people.remove(personIndex);
		notifyDataSetChanged();
		return true;
	}

	/**
	 * Edit a person name
	 * 
	 * @param person
	 *            person to add
	 */
	public boolean editPerson(String oldName, String newName) {
		int oldPersonIndex = getPersonIndex(oldName);
		int newPersonIndex = getPersonIndex(newName);

		// person doesn't exist or new name already exists
		if (oldPersonIndex == -1
				|| (newPersonIndex != -1 && newPersonIndex != oldPersonIndex))
			return false;

		people.get(oldPersonIndex).setName(newName);
		notifyDataSetChanged();
		return true;
	}

	/**
	 * Replace all people of the ExpandableListView
	 * 
	 * @param people
	 *            new people of the ExpandableListView
	 */
	public void replacePeople(List<Person> people) {
		this.people.clear();

		if (people != null) {
			for (Person person : people)
				if (person != null)
					addPerson(person);

			Collections.sort(this.people);
		}

		notifyDataSetChanged();
	}

	private OnClickListener editPersonOnClickListener = new OnClickListener() {
		EditPersonDialog editPersonDialog;

		@Override
		public void onClick(View v) {
			editPersonDialog = new EditPersonDialog((String) v.getTag(),
					editPersonListener, editPersonListener, editPersonListener);
			editPersonDialog.show(context.getFragmentManager(), TAG);
		}

		DialogInterface.OnClickListener editPersonListener = new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				switch (which) {
				case DialogInterface.BUTTON_POSITIVE:
					peopleAdapterListener.editPerson(
							editPersonDialog.getOldName(),
							editPersonDialog.getName());
					break;

				case DialogInterface.BUTTON_NEUTRAL:
					askForPersonDeletion(editPersonDialog.getOldName());
					break;
				default:
					break;
				}
			}
		};

		private void askForPersonDeletion(final String name) {
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
											name))
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
						peopleAdapterListener.removePerson(name);
					}
				};
			}.show(context.getFragmentManager(), TAG);
		}
	};

	public boolean removePhoto(String name, Photo photo) {
		Pair<Integer, Integer> indexes = getPhotoIndex(name, photo);

		// person doesn't exist or photo doesn't exist
		if (indexes.first == -1 || indexes.second == -1)
			return false;

		people.get(indexes.first).removePhoto(indexes.second);
		notifyDataSetChanged();
		return true;
	}

	public boolean addPhoto(String name, Photo photo) {
		Pair<Integer, Integer> indexes = getPhotoIndex(name, photo);

		// person doesn't exist or photo already exists
		if (indexes.first == -1 || indexes.second != -1)
			return false;

		people.get(indexes.first).addPhoto(photo);
		notifyDataSetChanged();
		return true;
	}

	private int getPersonIndex(String name) {
		if (name == null)
			return -1;

		for (int i = 0, l = people.size(); i < l; i++)
			if (people.get(i).getName().compareTo(name) == 0)
				return i;

		return -1;
	}

	private Pair<Integer, Integer> getPhotoIndex(String name, Photo photo) {
		int personIndex = getPersonIndex(name);

		// person doesn't exist
		if (personIndex == -1)
			return new Pair<Integer, Integer>(-1, -1);

		List<Photo> photos = people.get(personIndex).getPhotos();
		String photoUrl = photo.getUrl();
		
		for (int i = 0, l = photos.size(); i < l; i++)
			if (photos.get(i).getUrl().compareTo(photoUrl) == 0)
				return new Pair<Integer, Integer>(personIndex, i);

		return new Pair<Integer, Integer>(personIndex, -1);
	}
}