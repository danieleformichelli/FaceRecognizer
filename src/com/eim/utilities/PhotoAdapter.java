package com.eim.utilities;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

import com.eim.facesmanagement.peopledb.Photo;

public class PhotoAdapter extends BaseAdapter {
	private Context mContext;
	private List<Photo> photos;
	private List<Boolean> selected;
	private PhotoSelectionListener photoSelectionListener;
	private int selectedColor, notSelectedColor;

	public PhotoAdapter(Context mContext, List<Photo> photos,
			PhotoSelectionListener photoSelectionListener) {
		this.mContext = mContext;

		this.photos = new ArrayList<Photo>();
		selected = new ArrayList<Boolean>();
		this.photoSelectionListener = photoSelectionListener;

		replacePhotos(photos);

		selectedColor = mContext.getResources().getColor(
				android.R.color.holo_blue_bright);
		notSelectedColor = mContext.getResources().getColor(
				android.R.color.transparent);
	}

	public void replacePhotos(List<Photo> photos) {
		this.photos.clear();
		selected.clear();
		if (photoSelectionListener != null)
			photoSelectionListener.photosSelectionChanged(false);

		if (photos == null)
			return;

		for (Photo photo : photos)
			addPhoto(photo);

		notifyDataSetChanged();
	}

	public void addPhoto(Photo photo) {
		if (photo == null)
			return;

		photos.add(photo);
		selected.add(false);

		notifyDataSetChanged();
	}

	public void setSelected(int position, boolean select) {
		if (position < 0 || position >= photos.size())
			throw new IllegalArgumentException(
					"replacePhoto: position out of bound");

		if (select && selected.get(position) == false) {
			selected.set(position, true);
			if (photoSelectionListener != null)
				photoSelectionListener.photosSelectionChanged(true);
		} else if (!select && selected.get(position) == true) {
			selected.set(position, false);
			if (photoSelectionListener != null)
				photoSelectionListener
						.photosSelectionChanged(atLeastOneSelected());
		} else
			return;

		notifyDataSetChanged();
	}

	private boolean atLeastOneSelected() {
		for (int i = 0, l = selected.size(); i < l; i++)
			if (selected.get(i))
				return true;

		return false;
	}

	public void replacePhoto(int position, Photo photo) {
		if (photo == null)
			return;

		if (position >= photos.size())
			throw new IllegalArgumentException(
					"replacePhoto: position out of bound");

		photos.set(position, photo);
		if (selected.get(position) == true) {
			selected.set(position, false);
			if (photoSelectionListener != null)
				photoSelectionListener
						.photosSelectionChanged(atLeastOneSelected());
		}

		notifyDataSetChanged();
	}

	public void removePhoto(int position) {
		if (position >= photos.size())
			throw new IllegalArgumentException(
					"replacePhoto: position out of bound");

		photos.remove(position);
		if (selected.remove(position) == true)
			if (photoSelectionListener != null)
				photoSelectionListener
						.photosSelectionChanged(atLeastOneSelected());

		notifyDataSetChanged();
	}

	public int getCount() {
		return photos.size();
	}

	public Photo getItem(int position) {
		return photos.get(position);
	}

	public long getItemId(int position) {
		return position;
	}

	// create a new ImageView for each item referenced by the Adapter
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		SquareImageView imageView;

		if (convertView == null) {
			imageView = new SquareImageView(mContext);
			imageView.setSize(GridView.LayoutParams.MATCH_PARENT);
			imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
			imageView.setPadding(5, 5, 5, 5);
		} else
			imageView = (SquareImageView) convertView;

		imageView.setImageBitmap(photos.get(position).getBitmap());

		imageView.setBackgroundColor(selected.get(position) ? selectedColor
				: notSelectedColor);

		return imageView;
	}

	public void removeSelectedPhotos() {
		List<Integer> toBeDeleted = new ArrayList<Integer>();

		if (selected.size() == 0)
			return;

		for (int i = selected.size() - 1; i >= 0; i--)
			if (selected.get(i))
				toBeDeleted.add(i);

		for (int i : toBeDeleted) {
			photos.remove(i);
			selected.remove(i);
		}

		if (photoSelectionListener != null)
			photoSelectionListener.photosSelectionChanged(false);
		notifyDataSetChanged();
	}

	public boolean isSelected(int position) {
		return selected.get(position);
	}

	public interface PhotoSelectionListener {

		/**
		 * The state of the selection is changed
		 * 
		 * @param selected
		 *            true if at least one photo is selected, false otherwise
		 */
		public void photosSelectionChanged(boolean selected);
	}
}