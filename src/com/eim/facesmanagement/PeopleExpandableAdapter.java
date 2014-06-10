package com.eim.facesmanagement;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import android.app.Activity;
import android.content.Context;
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
public class PeopleExpandableAdapter extends BaseExpandableListAdapter {
	private Context context;
	List<Person> objects;
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
	 * @param objects
	 *            list of people to be added to the adapter
	 * @param editOnClickListener
	 *            onClickListener of the edit issue button, the related issue
	 *            will be added as a tag to the associated view
	 * @param deleteOnClickListener
	 *            onClickListener of the done issue button, the related issue
	 *            will be added as a tag to the associated view
	 */
	public PeopleExpandableAdapter(Activity context, int groupResource,
			int childResource, List<Person> objects,
			PeopleAdapterListener peopleAdapterListener) {

		this.context = context;

		this.groupResource = groupResource;
		this.childResource = childResource;

		this.objects = new ArrayList<Person>();
		replaceItems(objects);

		this.peopleAdapterListener = peopleAdapterListener;
	}

	@Override
	public View getGroupView(int groupPosition, boolean isExpanded,
			View convertView, ViewGroup parent) {
		LayoutInflater inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View item = inflater.inflate(groupResource, parent, false);

		Person object = objects.get(groupPosition);

		TextView name = (TextView) item
				.findViewById(R.id.person_list_item_label);
		name.setText(object.getName());
		name.setTag(object);

		if (isExpanded)
			name.setCompoundDrawablesWithIntrinsicBounds(
					R.drawable.action_collapse, 0, 0, 0);
		else
			name.setCompoundDrawablesWithIntrinsicBounds(
					R.drawable.action_expand, 0, 0, 0);

		ImageView edit = (ImageView) item
				.findViewById(R.id.person_list_item_edit_button);
		edit.setVisibility(isExpanded ? View.VISIBLE : View.GONE);
		edit.setOnClickListener(editPersonNameOnClickListener);

		return item;
	}

	@Override
	public View getChildView(int groupPosition, int childPosition,
			boolean isLastChild, View convertView, ViewGroup parent) {
		LayoutInflater inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		ViewGroup childView = (ViewGroup) inflater.inflate(childResource,
				parent, false);

		return childView;
	}

	@Override
	public int getGroupCount() {
		return objects.size();
	}

	@Override
	public int getChildrenCount(int groupPosition) {
		return 1;
	}

	@Override
	public Object getGroup(int groupPosition) {
		return objects.get(groupPosition);
	}

	@Override
	public Object getChild(int groupPosition, int childPosition) {
		return objects.get(groupPosition);
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
	 * Replace all items of the ExpandableListView
	 * 
	 * @param objects
	 *            new objects of the ExpandableListView
	 */
	public void replaceItems(List<Person> objects) {
		if (objects != null) {
			this.objects = objects;
			Collections.sort(this.objects);
		} else
			this.objects.clear();

		notifyDataSetChanged();
	}

	private OnClickListener editPersonNameOnClickListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
		}
	};
}