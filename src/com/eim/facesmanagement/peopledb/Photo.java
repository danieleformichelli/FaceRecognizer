package com.eim.facesmanagement.peopledb;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

public class Photo {
	private String url;
	private Bitmap mBitmap;

	public Photo(String url, Bitmap mBitmap) {
		if (url == null && mBitmap == null)
			throw new IllegalArgumentException(
					"url and bitmap cannot be both null");

		this.url = url;
		this.mBitmap = mBitmap;
	}

	public Photo(String url) {
		if (url == null)
			throw new IllegalArgumentException(
					"url cannot be null");

		this.url = url;
	}

	public Photo(Bitmap bitmap) {
		if (bitmap == null)
			throw new IllegalArgumentException(
					"bitmap cannot be null");

		this.mBitmap = bitmap;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		if (url == null && mBitmap == null)
			throw new IllegalArgumentException(
					"url and bitmap cannot be both null");

		this.url = url;
	}

	public Bitmap getBitmap() {
		if (mBitmap == null)
			mBitmap = BitmapFactory.decodeFile(getUrl());
		return mBitmap;
	}

	public void setBitmap(Bitmap mBitmap) {
		if (url == null && mBitmap == null)
			throw new IllegalArgumentException(
					"url and bitmap cannot be both null");

		this.mBitmap = mBitmap;
	}

}
