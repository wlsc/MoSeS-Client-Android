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
