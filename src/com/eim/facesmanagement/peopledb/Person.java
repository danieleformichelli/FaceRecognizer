package com.eim.facesmanagement.peopledb;

import android.util.LongSparseArray;

/**
 * This class represent a person to be recognized. Each person has a name and
 * zero or more pairs (photo, features)
 * 
 */
public class Person implements Comparable<Person> {

	private String name;
	private LongSparseArray<Photo> photos;

	public Person(String name, LongSparseArray<Photo> photos) {
		setName(name);

		this.photos = new LongSparseArray<Photo>();
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

	public LongSparseArray<Photo> getPhotos() {
		return photos;
	}

	public void setPhotos(LongSparseArray<Photo> photos) {
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

	public void addPhoto(long id, Photo photo) {
		if (photo != null)
			photos.put(id, photo);
	}

	public void removePhoto(long id) {
		photos.remove(id);
	}

	@Override
	public int compareTo(Person another) {
		return name.compareTo(another.getName());
	}
}
