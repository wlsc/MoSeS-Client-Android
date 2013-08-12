package de.da_sense.moses.client.userstudy;

/**
 * A class to be extended from all classes that represent an object with an id coded as an integer.
 * 
 * @author Zijad Maksuti
 *
 */
public class HasID {
	
	private int mID;
	
	/**
	 * Sets the id to this object.
	 * @param id the id to be set
	 * @return old id assigned to this object or null if the object did not have an id.
	 */
	public int setId(int id){
		int oldId = this.mID;
		this.mID = id;
		return oldId;
	}
	
	/**
	 * Returns an id assigned to this object.
	 * @return an id assigned to this object. If the id is not set this method returns null
	 */
	public int getId(){
		return mID;
	}
	
}
