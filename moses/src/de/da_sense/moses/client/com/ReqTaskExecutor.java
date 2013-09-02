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
package de.da_sense.moses.client.com;
/**
 * This interface is used for NetworkJSON calls.
 * 
 * @author Jaco Hofmann
 */
public interface ReqTaskExecutor {

	/**
	 * Specifies how to handle an exception.
	 * 
	 * @param e
	 *            The Exception to handle
	 */
	public void handleException(Exception e);

	/**
	 * Specifies what to do after the execution.
	 * 
	 * @param s
	 *            The Resultstring from the Execution
	 */
	public void postExecution(String s);

	/**
	 * Update execution.
	 * 
	 * @param c
	 *            The NetworkJSON.BackgroundException
	 */
	public void updateExecution(NetworkJSON.BackgroundException c);
}
