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
/**
 * 
 */
package de.da_sense.moses.client.util;

/**
 * Fragments implementing this interface are interested in change
 * of the Internet connection: connection loss and connection establishment.
 * Fragments that implement this interface offer a possibility to their parent
 * activity to directly inform them about the changes regarding the Internet
 * connection. 
 * 
 * @author Zijad Maksuti
 *
 */
public interface InternetConnectionChangeListener {
	
	/**
	 * This method is called when Internet connection is lost.
	 */
	public void onConnectionLost();
	
	/**
	 * This method is called when Internet connection is established.
	 */
	public void onConnectionEstablished();
	
}
