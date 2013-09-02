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
 * Enum of all Message Types
 * @author Wladimir Schmidt
 *
 */
public enum MessageTypesEnum {
	SPAMMABLE, 
	REQUEST_C2DM, 
	REQUEST_INSTALLED_APK, 
	REQUEST_UNINSTALLED_APK, 
	REQUEST_GET_APK_INFO,
	ACTIVITY_PRINT_MESSAGE, 
	REQUEST_GET_LIST_APK, 
	REQUEST_DOWNLOAD_LINK, 
	REQUEST_GET_HARDWARE_PARAMETERS,
	REQUEST_SET_HARDWARE_PARAMETERS, 
	REQUEST_UPDATE_HARDWARE_PARAMETERS, 
	REQUEST_SET_QUESTIONNAIRE_ANSWERS
}
