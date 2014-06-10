package com.eim.facesmanagement;

import java.util.ArrayList;
import java.util.List;

import android.util.Pair;

/**
 * This class represent a person to be recognized.
 * Each person has a name and zero or more pairs (photo, features)
 *
 */
public class Person {
	String name;
	List<Pair<String, Object>> photos;

	public Person(String name, List<Pair<String, Object>> photos) {
		setName(name);
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

	public List<Pair<String, Object>> getPhotos() {
		return photos;
	}

	public void setPhotos(List<Pair<String, Object>> photos) {
		this.photos = new ArrayList<Pair<String, Object>>();

		if (photos != null)
			for (Pair<String, Object> photo : photos)
				if (photo.first != null && photo.second != null)
					this.photos.add(photo);
	}
	
	public void addPhoto(Pair<String, Object> photo) {
		if (photo != null && photo.first != null && photo.second != null)
			this.photos.add(photo);
	}

	public void removePhoto(Pair<String, Object> photo) {
		photos.remove(photo);
	}

}
