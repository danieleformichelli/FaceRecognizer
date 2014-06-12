package com.eim.facesmanagement;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;

public class PhotoAdapter extends BaseAdapter {
	Context context;
	List<Bitmap> photos;
	List<Boolean> selected;
	PhotoSelectionListener photoSelectionListener;
	int selectedColor, notSelectedColor;

	public PhotoAdapter(Context context, List<Bitmap> photos,
			PhotoSelectionListener photoSelectionListener) {
		this.context = context;

		this.photos = new ArrayList<Bitmap>();
		selected = new ArrayList<Boolean>();
		this.photoSelectionListener = photoSelectionListener;

		replacePhotos(photos);

		selectedColor = context.getResources().getColor(
				android.R.color.holo_blue_bright);
		notSelectedColor = context.getResources().getColor(
				android.R.color.transparent);
	}

	public void replacePhotos(List<Bitmap> photos) {
		this.photos.clear();
		selected.clear();
		photoSelectionListener.photosSelectionChanged(false);

		if (photos == null)
			return;

		for (Bitmap photo : photos)
			if (photo != null) {
				this.photos.add(photo);
				selected.add(false);
			}

		notifyDataSetChanged();
	}

	public void addPhoto(Bitmap photo) {
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
			photoSelectionListener.photosSelectionChanged(true);
		} else if (!select && selected.get(position) == true) {
			selected.set(position, false);
			photoSelectionListener.photosSelectionChanged(atLeastOneSelected());
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

	public void replacePhoto(int position, Bitmap photo) {
		if (photo == null)
			return;

		if (position >= photos.size())
			throw new IllegalArgumentException(
					"replacePhoto: position out of bound");

		photos.set(position, photo);
		if (selected.get(position) == true) {
			selected.set(position, false);
			photoSelectionListener.photosSelectionChanged(atLeastOneSelected());
		}

		notifyDataSetChanged();
	}

	public void removePhoto(int position) {
		if (position >= photos.size())
			throw new IllegalArgumentException(
					"replacePhoto: position out of bound");

		photos.remove(position);
		if (selected.remove(position) == true)
			photoSelectionListener.photosSelectionChanged(atLeastOneSelected());

		notifyDataSetChanged();
	}

	public int getCount() {
		return photos.size();
	}

	public Bitmap getItem(int position) {
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
			imageView = new SquareImageView(context);
			imageView.setSize(GridView.LayoutParams.MATCH_PARENT);
		} else
			imageView = (SquareImageView) convertView;

		imageView.setImageBitmap(photos.get(position));
		imageView.setBackgroundColor(selected.get(position) ? selectedColor
				: notSelectedColor);

		return imageView;
	}

	public void removeSelectedPhotos() {
		List<Integer> toBeDeleted = new ArrayList<Integer>();

		for (int i = selected.size() - 1; i >= 0; i--)
			if (selected.get(i))
				toBeDeleted.add(i);

		for (int i : toBeDeleted) {
			photos.remove(i);
			selected.remove(i);
		}

		photoSelectionListener.photosSelectionChanged(false);
		notifyDataSetChanged();
	}

	public boolean isSelected(int position) {
		return selected.get(position);
	}

}