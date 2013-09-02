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
package de.da_sense.moses.client.service.helpers;
/**
 * Class consisting of an EMessagType t and and Executable e
 * @author Jaco Hofmann
 *
 */
public class ExecutableWithType {

	/**
	 * Constructor for an Executable with an EMessageType.
	 * @param t the message type
	 * @param e the executable
	 */
	public ExecutableWithType(MessageTypesEnum t, Executable e) {
		this.e = e;
		this.t = t;
	}

	/** the executable */
	public Executable e;
	/** the message type */
	public MessageTypesEnum t;
}
