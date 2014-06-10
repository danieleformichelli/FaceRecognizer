package com.eim.facesmanagement;

public interface PeopleAdapterListener {

	/**
	 * A person has been added to the list
	 * 
	 * @param name
	 *            name of the added person
	 */
	public void onPersonAdded(String name);

	/**
	 * The name of a person has been edited
	 * 
	 * @param oldName
	 *            old name of the person
	 * @param newName
	 *            new name of the person
	 */
	public void onPersonEdited(String oldName, String newName);

	/**
	 * A person has been removed from the list
	 * 
	 * @param name
	 *            name of the removed person
	 */
	public void onPersonRemoved(String name);

	/**
	 * A photo has been added to a person
	 * 
	 * @param name
	 *            name of the person
	 * @param photo
	 *            url of the photo
	 */
	public void onPhotoAdded(String name, String photo);

	/**
	 * A photo has been deleted
	 * 
	 * @param photo
	 *            url of the photo
	 */
	public void onPhotoRemoved(String photo);
}
