package com.eim.facesmanagement;

public interface PhotoSelectionListener {

	/**
	 * The state of the selection is changed
	 * 
	 * @param selected
	 *            true if at least one photo is selected, false otherwise
	 */
	public void photosSelectionChanged(boolean selected);
}
