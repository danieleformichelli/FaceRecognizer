package com.eim.facesmanagement;

import com.eim.facesmanagement.peopledb.Photo;

public interface PeopleAdapterListener {

	/**
	 * A person has been added to the list
	 * 
	 * @param name
	 *            name of the added person
	 */
	public void addPerson(String name);

	/**
	 * The name of a person has been edited
	 * 
	 * @param id
	 *            old name of the person
	 * @param newName
	 *            new name of the person
	 */
	public void editPersonName(long id, String newName);

	/**
	 * A person has been removed from the list
	 * 
	 * @param id
	 *            name of the removed person
	 */
	public void removePerson(long id);

	/**
	 * A photo has been added to a person
	 * 
	 * @param id
	 *            id of the person
	 * @param photo
	 *            url of the photo
	 */
	public void addPhoto(long personId, Photo photo);

	/**
	 * A photo has been deleted
	 * 
	 * @param photoId
	 */
	public void removePhoto(long personId, long photoId);
}
