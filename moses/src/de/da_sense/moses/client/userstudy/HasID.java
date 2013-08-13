package de.da_sense.moses.client.userstudy;

/**
 * A class to be extended from all classes that represent an object with an id coded as an integer.
 * 
 * @author Zijad Maksuti
 *
 */
public class HasID implements Comparable<HasID>{
	
	/*
	 * Default value of an ID is -1
	 */
	private int mID = -1;
	
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

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object o) {
		if (this == o)
            return true;
          if (!(o instanceof HasID))
            return false;
          HasID otherHasID = (HasID) o;
          return this.mID == otherHasID.mID;   
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() { 
        int hash = 1;
        hash = hash * 31 + mID;
        return hash;
    }

	@Override
	public int compareTo(HasID another) {
		return this.mID - another.mID;
	}
	
	
	
}
