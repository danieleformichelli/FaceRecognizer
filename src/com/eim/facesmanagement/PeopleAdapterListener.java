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
	 * @param oldName
	 *            old name of the person
	 * @param newName
	 *            new name of the person
	 */
	public void editPerson(String oldName, String newName);

	/**
	 * A person has been removed from the list
	 * 
	 * @param name
	 *            name of the removed person
	 */
	public void removePerson(String name);

	/**
	 * A photo has been added to a person
	 * 
	 * @param name
	 *            name of the person
	 * @param photo
	 *            url of the photo
	 */
	public void addPhoto(String name, Photo photo);

	/**
	 * A photo has been deleted
	 * 
	 * @param name
	 *            name of the person
	 * @param photo
	 *            url of the photo
	 */
	public void removePhoto(String name, Photo photo);
}
