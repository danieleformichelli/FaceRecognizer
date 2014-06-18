package com.eim.facesmanagement.peopledb;

import android.util.SparseArray;

/**
 * This class represent a person to be recognized. Each person has a name and
 * zero or more pairs (photo, features)
 * 
 */
public class Person implements Comparable<Person> {

	private String name;
	private SparseArray<Photo> photos;

	public Person(String name, SparseArray<Photo> photos) {
		setName(name);

		this.photos = new SparseArray<Photo>();
		setPhotos(photos);
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		if (name == null || name.length() == 0)
			throw new IllegalArgumentException(
					"name must be at least 1 character int");

		this.name = name;
	}

	public SparseArray<Photo> getPhotos() {
		return photos;
	}

	public void setPhotos(SparseArray<Photo> photos) {
		if (photos == null)
			this.photos.clear();
		else
			this.photos = photos;

		this.photos.clear();

		if (photos != null)
			for (int i = 0, l = photos.size(); i < l; i++) {
				Photo photo = photos.valueAt(i);
				if (photo != null)
					addPhoto(photos.keyAt(i), photo);
			}
	}

	public void addPhoto(int id, Photo photo) {
		if (photo == null)
			throw new IllegalArgumentException("photo cannot be null");

		photos.put(id, photo);
	}

	public void removePhoto(int id) {
		photos.remove(id);
	}

	@Override
	public int compareTo(Person another) {
		return name.compareTo(another.getName());
	}
}
