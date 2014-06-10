package com.eim.utilities;

public interface Swipeable {

	/**
	 * The fragment was selected and it has been swiped out.
	 * 
	 * @param toRight
	 *            true if the swiped in fragment is the one on the right, false
	 *            if it is the one on the left
	 */
	public abstract void swipeOut(boolean toRight);

	/**
	 * The fragment was not selected and it has been swiped in.
	 * 
	 * @param fromLeft
	 *            true if the swiped out fragment is the one on the left, false
	 *            if it is the one on the left
	 */
	public abstract void swipeIn(boolean fromLeft);
}
