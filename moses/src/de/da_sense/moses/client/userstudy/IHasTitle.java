package de.da_sense.moses.client.userstudy;

/**
 * A class for all objects that have a title coded as {@link String}.
 * @author Zijad Maksuti
 *
 */
public interface IHasTitle {
	
	/**
	 * Sets the title to this {@link IHasTitle} object.
	 * @param title the title to set
	 * @return old title assigned to this object if any
	 */
	public String setTitle(String title);
	
	/**
	 * Returns the title assigned to this object.
	 * @return the title assigned to this object if any.
	 */
	public String getTitle();
	
}
