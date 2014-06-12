package com.eim.facesmanagement;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageView;

import com.eim.R;
import com.eim.facedetection.FaceDetectionAndExtractionActivity;

/**
 * A Gallery is an horizontal LinearLayout that can contain zero or more photos
 * and allows to add or delete them.
 */
public class PhotoGallery extends GridView implements PhotoSelectionListener {
	private static final String TAG = "PhotoGallery";
	private static final int colCount = 5;
	public static final int FACE_DETECTION_AND_EXTRACTION = 1;

	ImageView addDeletePhoto;
	PhotoAdapter galleryAdapter;
	int photoCount;
	Bitmap add, delete;
	Context context;
	boolean addOrDelete;

	public PhotoGallery(Context context) {
		super(context);
		initView(context);
	}

	public PhotoGallery(Context context, AttributeSet attrs) {
		super(context, attrs);
		initView(context);
	}

	public PhotoGallery(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		initView(context);
	}

	private void initView(Context context) {
		this.context = context;
		setNumColumns(colCount);

		add = BitmapFactory.decodeResource(context.getResources(),
				R.drawable.action_add_photo);
		delete = BitmapFactory.decodeResource(context.getResources(),
				R.drawable.action_delete);

		galleryAdapter = new PhotoAdapter(context, null, this);
		addPhoto(add);
		setAdapter(galleryAdapter);

		setOnItemClickListener(galleryOnItemClickListener);
		photoCount = 0;

		showAddPhoto();
	}

	private void showAddPhoto() {
		galleryAdapter.replacePhoto(0, add);
		addOrDelete = true;
	}

	private void showDeletePhoto() {
		galleryAdapter.replacePhoto(0, delete);
		addOrDelete = false;
	}

	/**
	 * Add a photo to the gallery
	 * 
	 * @param path
	 *            location of the image
	 * 
	 * @return view of the added image
	 */
	public void addPhoto(String path) {
		addPhoto(BitmapFactory.decodeFile(path));
	}

	public void addPhoto(Bitmap photo) {
		photoCount++;
		galleryAdapter.addPhoto(photo);
	}

	/**
	 * Remove photo from the gallery
	 * 
	 * @param v
	 *            image to be removed
	 */
	public void removePhoto(int position) {
		if (position < 0)
			throw new IllegalArgumentException("position must be positive");

		photoCount--;
		galleryAdapter.removePhoto(position + 1);
	}

	private void removePhoto() {
		removePhoto(0);
	}

	/**
	 * Removes the images that are currently selected
	 */
	public void removeSelectedPhotos() {
		galleryAdapter.removeSelectedPhotos();
	}

	/**
	 * Remove all the images from the gallery
	 */
	public void removeAllPhotos() {
		while (photoCount > 0)
			removePhoto();
	}

	void addPhoto() {
		Intent intent = new Intent(context,
				FaceDetectionAndExtractionActivity.class);
		((Activity) context).startActivityForResult(intent,
				FACE_DETECTION_AND_EXTRACTION);
	}

	OnItemClickListener galleryOnItemClickListener = new OnItemClickListener() {
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position,
				long id) {
			if (position == 0) {
				if (addOrDelete)
					addPhoto();
				else
					removeSelectedPhotos();

				return;
			}

			galleryAdapter.setSelected(position,
					!galleryAdapter.isSelected(position));
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

	@Override
	public void photosSelectionChanged(boolean selected) {
		if (galleryAdapter == null)
			return;
		
		if (selected && galleryAdapter.getItem(0) == add)
			showDeletePhoto();
		else if (!selected && galleryAdapter.getItem(0) == delete)
			showAddPhoto();
	}
}