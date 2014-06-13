package com.eim.facesmanagement.peopledb;

import java.util.ArrayList;
import java.util.List;

/**
 * This class represent a person to be recognized. Each person has a name and
 * zero or more pairs (photo, features)
 * 
 */
public class Person implements Comparable<Person> {

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

	public void setPhotos(List<Photo> photos) {

		if (photos == null)
			this.photos.clear();
		else
			this.photos = photos;
	}

	public void addPhoto(Photo photo) {
		if (photo != null)
			this.photos.add(photo);
	}

	public void removePhoto(Integer photoIndex) {
		photos.remove(photoIndex);
	}

	public void removePhoto(Photo photo) {
		photos.remove(photo);
	}

	@Override
	public int compareTo(Person another) {
		return name.compareTo(another.getName());
	}

}
