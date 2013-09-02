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
 * Enum of all HookTypes
 * @author Jaco Hofmann, Wladimir Schmidt
 */
public enum HookTypesEnum {
	POST_LOGOUT, 
	POST_LOGIN_SUCCESS, 
	POST_LOGIN_SUCCESS_PRIORITY, 
	POST_LOGIN_FAILED, 
	POST_LOGIN_START, 
	POST_LOGIN_END
}
