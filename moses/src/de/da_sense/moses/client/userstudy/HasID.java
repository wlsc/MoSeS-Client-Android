/*******************************************************************************
 * Copyright 2013
 * Telecooperation (TK) Lab
 * Technische Universität Darmstadt
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
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
