package com.eim.facesmanagement;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.media.ThumbnailUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.eim.R;
import com.eim.facedetection.FaceDetectionAndExtractionActivity;

/**
 * A Gallery is an horizontal LinearLayout that can contain zero or more photos
 * and allows to add or delete them.
 */
public class PhotoGallery extends HorizontalScrollView {
	private static final String TAG = "VineyardGallery";
	public static final int FACE_DETECTION_AND_EXTRACTION = 1;

	ImageView addDeletePhoto;
	LinearLayout gallery;
	int size, padding, selectedColor, notSelectedColor;
	List<ImageView> photos, selected;
	Drawable add, delete;

	public PhotoGallery(Context context) {
		super(context);
		Log.e(TAG, context.toString());
		initView();
	}

	public PhotoGallery(Context context, AttributeSet attrs) {
		super(context, attrs);
		Log.e(TAG, context.toString());
		initView();
	}

	public PhotoGallery(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		Log.e(TAG, context.toString());
		initView();
	}

	private void initView() {
		Context context = getContext();
		LayoutInflater.from(context).inflate(R.layout.photo_gallery, this);

		gallery = (LinearLayout) this
				.findViewById(R.id.photo_gallery_linear_layout);
		addDeletePhoto = (ImageView) this
				.findViewById(R.id.photo_gallery_action_add_delete_photo);

		size = context.getResources().getDimensionPixelSize(
				R.dimen.gallery_height);
		padding = context.getResources().getDimensionPixelSize(
				R.dimen.gallery_padding);

		photos = new ArrayList<ImageView>();
		selected = new ArrayList<ImageView>();

		// selectedColor = context.getResources().getColor(R.color.white);
		// notSelectedColor =
		// context.getResources().getColor(R.color.wine_light);

		add = context.getResources().getDrawable(R.drawable.action_add_photo);
		delete = context.getResources().getDrawable(R.drawable.action_delete);

		showAddPhoto();
	}

	private void showAddPhoto() {
		addDeletePhoto.setImageDrawable(add);
		addDeletePhoto
				.setOnClickListener(startFaceDetectionAndExtractionActivity);

	}

	private void showDeletePhoto() {
		addDeletePhoto.setImageDrawable(delete);
		addDeletePhoto.setOnClickListener(deleteSelectedPhotos);
	}

	/**
	 * Add a photo to the gallery
	 * 
	 * @param path
	 *            location of the image
	 * 
	 * @return view of the added image
	 */
	public ImageView addPhoto(String path, int position) {
		// the image is locally stored
		Bitmap b = getThumbnailFromFilePath(path, size);
		if (b == null)
			return null;

		ImageView v = addPhoto(path, b, position);
		v.setOnClickListener(selectOnClick);

		return v;
	}

	private ImageView addPhoto(String path, Bitmap photo, int position) {
		if (photo == null)
			throw new IllegalArgumentException("photo cannot be null");

		ImageView v = new ImageView(getContext());
		v.setTag(path);
		v.setImageBitmap(photo);
		v.setPadding(padding, padding, padding, padding);

		// add the container to the gallery, position 0 is for the button
		gallery.addView(v, position + 1);
		photos.add(v);

		return v;
	}

	/**
	 * Remove photo from the gallery
	 * 
	 * @param v
	 *            image to be removed
	 */
	public void removePhoto(int position) {
		ImageView photo = photos.remove(position);
		gallery.removeViewAt(position + 1);
		selected.remove(position);

		File f = new File(photo.getTag().toString());
		if (f != null)
			f.delete();

		if (selected.isEmpty())
			showAddPhoto();
	}

	private void removePhoto() {
		removePhoto(0);
	}

	/**
	 * Removes the images that are currently selected
	 */
	public void removeSelectedPhotos() {
		while (!selected.isEmpty())
			removePhoto();
	}

	/**
	 * Remove all the images from the gallery
	 */
	public void removeAllPhotos() {
		while (!photos.isEmpty())
			removePhoto();
	}

	/*
	 * Get a square thumbnail of a locally stored photo
	 */
	private Bitmap getThumbnailFromFilePath(String filePath, int size) {
		return ThumbnailUtils.extractThumbnail(
				BitmapFactory.decodeFile(filePath), size, size);
	}

	private void setSelected(ImageView photo, boolean select) {
		if (select) {
			if (selected.isEmpty())
				showDeletePhoto();

			selected.add(photo);
			photo.setBackgroundColor(selectedColor);
		} else {
			selected.remove(photo);
			photo.setBackgroundColor(notSelectedColor);

			if (selected.isEmpty())
				showAddPhoto();
		}
	}

	OnClickListener startFaceDetectionAndExtractionActivity = new OnClickListener() {
		@Override
		public void onClick(View v) {
			Intent intent = new Intent(getContext(),
					FaceDetectionAndExtractionActivity.class);
			((Activity) getContext()).startActivityForResult(intent,
					FACE_DETECTION_AND_EXTRACTION);
		}
	};

	OnClickListener deleteSelectedPhotos = new OnClickListener() {
		@Override
		public void onClick(View v) {
			removeSelectedPhotos();
		}
	};

	OnClickListener selectOnClick = new OnClickListener() {
		@Override
		public void onClick(View v) {
			// select the image if it is not selected and viceversa
			setSelected((ImageView) v, !selected.contains(v));
		}
	};

	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode != Activity.RESULT_OK)
			return;
		
		Log.e(TAG, "onActivityResult: " + requestCode + ", " + resultCode);

		switch (requestCode) {
		case FACE_DETECTION_AND_EXTRACTION:
			// TODO
			break;
		}
	}
}