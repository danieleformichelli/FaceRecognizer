package com.eim.facesmanagement;

import java.util.ArrayList;
import java.util.List;

import android.graphics.Bitmap;

/**
 * This class represent a person to be recognized. Each person has a name and
 * zero or more pairs (photo, features)
 * 
 */
public class Person implements Comparable<Person>  {
	
	String name;
	List<Photo> photos;

	public Person(String name, List<Photo> photos) {
		setName(name);

		this.photos = new ArrayList<Photo>();
		setPhotos(photos);
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		if (name == null || name.length() == 0)
			throw new IllegalArgumentException(
					"name must be at least 1 character long");

		this.name = name;
	}

	public List<Photo> getPhotos() {
		return photos;
	}

	public List<String> getUrls() {
		List<String> urls = new ArrayList<String>();
		for (Photo photo: photos)
			urls.add(photo.getUrl());
		
		return urls;
	}

	public List<Bitmap> getBitmaps() {
		List<Bitmap> bitmaps = new ArrayList<Bitmap>();
		for (Photo photo: photos)
			bitmaps.add(photo.getBitmap());
		
		return bitmaps;
	}

	public List<Object> getFeatures() {
		List<Object> features = new ArrayList<Object>();
		for (Photo photo: photos)
			features.add(photo.getFeatures());
		
		return features;
	}

	public void setPhotos(List<Photo> photos) {
		this.photos.clear();

		if (photos != null)
			for (Photo photo : photos)
				if (photo.getUrl() != null && photo.getFeatures() != null)
					this.photos.add(photo);
	}

	public void addPhoto(Photo photo) {
		if (photo != null && photo.getUrl() != null && photo.getFeatures() != null)
			this.photos.add(photo);
	}

	public void removePhoto(Photo photo) {
		photos.remove(photo);
	}

	@Override
	public int compareTo(Person another) {
		return name.compareTo(another.getName());
	}

}
