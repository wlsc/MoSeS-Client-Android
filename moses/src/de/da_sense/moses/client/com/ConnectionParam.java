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
 * Connection Parameter - ENUM States {INIT, CONNECTING, CONNECTED, POSTEXECUTE, EXCEPTION}
 * 
 * @author Jaco Hofmann
 */
public enum ConnectionParam {

	/** The INIT. */
	INIT,
	/** The CONNECTING. */
	CONNECTING,
	/** The CONNECTED. */
	CONNECTED,
	/** The POSTEXECUTE. */
	POSTEXECUTE,
	/** The EXCEPTION. */
	EXCEPTION
}
