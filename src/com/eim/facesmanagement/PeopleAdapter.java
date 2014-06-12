package com.eim.facesmanagement;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.eim.R;

/**
 * ExpandableAdapter that shows the people in the database
 */
public class PeopleAdapter extends BaseExpandableListAdapter {
	private static final String TAG = "PeopleExpandableAdapter";

	private Activity context;
	List<Person> people;
	int groupResource, childResource;
	PeopleAdapterListener peopleAdapterListener;;

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
			PeopleAdapterListener peopleAdapterListener) {

		this.context = context;

		this.groupResource = groupResource;
		this.childResource = childResource;

		this.people = new ArrayList<Person>();
		replacePeople(people);

		this.peopleAdapterListener = peopleAdapterListener;
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
		edit.setOnClickListener(editPersonNameOnClickListener);
		edit.setTag(person.getName());

		return convertView;
	}

	@Override
	public View getChildView(int groupPosition, int childPosition,
			boolean isLastChild, View convertView, ViewGroup parent) {

		if (convertView == null) {
			LayoutInflater inflater = (LayoutInflater) context
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			convertView = (ViewGroup) inflater.inflate(childResource, parent,
					false);
		}

		PhotoGallery gallery = (PhotoGallery) convertView
				.findViewById(R.id.person_view_gallery);
		gallery.removeAllPhotos();

		for (Bitmap bitmap : people.get(groupPosition).getBitmaps())
			gallery.addPhoto(bitmap);

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

	private OnClickListener editPersonNameOnClickListener = new OnClickListener() {
		InsertNameDialog insertNameDialog;

		@Override
		public void onClick(View v) {
			insertNameDialog = new InsertNameDialog((String) v.getTag(),
					addPersonOkListener, null);
			insertNameDialog.show(context.getFragmentManager(), TAG);
		}

		DialogInterface.OnClickListener addPersonOkListener = new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				peopleAdapterListener.onPersonEdited(
						insertNameDialog.getOldName(),
						insertNameDialog.getName());
			}
		};
	};

	public boolean removePhoto(String name, String photo) {
		Pair<Integer, Integer> indexes = getPhotoIndex(name, photo);

		// person doesn't exist or photo doesn't exist
		if (indexes.first == -1 || indexes.second == -1)
			return false;

		people.get(indexes.first).getPhotos().remove(indexes.second);
		notifyDataSetChanged();
		return true;
	}

	public boolean addPhoto(String name, String photo, Object features) {
		Pair<Integer, Integer> indexes = getPhotoIndex(name, photo);

		// person doesn't exist or photo already exists
		if (indexes.first == -1 || indexes.second != -1)
			return false;

		people.get(indexes.first).getPhotos()
				.add(new Photo(photo, null, features));
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

	private Pair<Integer, Integer> getPhotoIndex(String name, String photo) {
		int personIndex = getPersonIndex(name);

		// person doesn't exist
		if (personIndex == -1)
			return new Pair<Integer, Integer>(-1, -1);

		List<Photo> photos = people.get(personIndex).getPhotos();

		for (int i = 0, l = photos.size(); i < l; i++)
			if (photos.get(i).getUrl().compareTo(photo) == 0)
				return new Pair<Integer, Integer>(personIndex, i);

		return new Pair<Integer, Integer>(personIndex, -1);
	}
}