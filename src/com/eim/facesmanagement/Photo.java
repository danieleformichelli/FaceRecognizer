package com.eim.facesmanagement;

import android.graphics.Bitmap;

public class Photo {
	String url;
	Bitmap bitmap;
	Object features;

	public Photo(String url, Bitmap bitmap, Object features) {
		if (url == null)
			throw new IllegalArgumentException("url cannot be null");
		if (features == null)
			throw new IllegalArgumentException("features cannot be null");

		this.url = url;
		this.bitmap = bitmap;
		this.features = features;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		if (url == null)
			throw new IllegalArgumentException("url cannot be null");
		
		this.url = url;
	}

	public Bitmap getBitmap() {
		return bitmap;
	}

	public void setBitmap(Bitmap bitmap) {
		this.bitmap = bitmap;
	}

	public Object getFeatures() {
		return features;
	}

	public void setFeatures(Object features) {
		if (features == null)
			throw new IllegalArgumentException("features cannot be null");

		this.features = features;
	}
}
