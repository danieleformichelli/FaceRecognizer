package com.eim.facesmanagement;

public interface PhotoGalleryListener {

	/**
	 * A new photo must be added
	 * 
	 * @param gallery
	 *            source of the request
	 */
	public void addPhoto(PhotoGallery gallery);

	/**
	 * Selected photos must be deleted
	 * 
	 * @param gallery
	 *            source of the request
	 */
	public void removeSelectedPhotos(PhotoGallery gallery);
}
