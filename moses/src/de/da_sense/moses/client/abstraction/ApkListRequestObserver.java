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
package de.da_sense.moses.client.abstraction;

import java.util.List;

import de.da_sense.moses.client.abstraction.apks.ExternalApplication;

/**
 * Interface for getting notifications about an apk list request
 * 
 * @author Simon L
 * 
 */
public interface ApkListRequestObserver {
	/**
	 * Notifies the observer, that the request was finished. if the reference to
	 * the external application was set, it will be passed on as parameter,
	 * otherwise this parameter will be null.
	 * 
	 * @param result
	 *            the application list
	 */
	public void apkListRequestFinished(List<ExternalApplication> result);

	/**
	 * Notifies the observer that the request failed
	 * 
	 * @param e
	 *            an exception object; may be null
	 */
	public void apkListRequestFailed(Exception e);
}
