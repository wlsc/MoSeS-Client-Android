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
package de.da_sense.moses.client.com.requests;

import org.json.JSONException;
import org.json.JSONObject;

import de.da_sense.moses.client.com.NetworkJSON;
import de.da_sense.moses.client.com.ReqTaskExecutor;

/**
 * This class is used for logging in. It provides some basic methods for it.
 * 
 * @author Jaco Hofmann
 * @author Zijad Maksuti
 * 
 */
public class RequestLogin {
	
	
	/**
	 * Checks if there currently exists a valid connection to the server
	 * corresponding to the user logged in on the device.
	 * @param j The JSONObject containing the connection information
	 * @param email The Email
	 * @return True if there is a valid session between the user and the server
	 * @throws JSONException
	 */
	public static boolean loginValid(JSONObject j, String email) throws JSONException {
		String sessionID = j.getString("SESSIONID");
		if (!sessionID.equals("NULL")) {
			SESSION_ID = sessionID;
			return j.getString("EMAIL").equals(email);
		}
		return false;
	}

	/**
	 * The JSONObject that shall be send to the server.
	 */
	private JSONObject j;
	/**
	 * The instance of ReqTaskExecutor
	 */
	private ReqTaskExecutor reqTaskExecutor;

	/**
	 * The current ID of the login given by the server
	 */
	private static String SESSION_ID = null; 

	/**
	 * Gives the current SessionID
	 * @return SESSION_ID
	 */
	public static String getSessionID() {
		return SESSION_ID;
	}

	/**
	 * Constructor for a RequestLogin. Prepares all clients information
	 * to be send to the server.
	 * @param reqTaskExecutorGiven An implementation of ReqTaskExecuter (usually LoginFunc)
	 * @param email the email
	 * @param pw The password
	 * @param deviceID the id of the device 
	 */
	public RequestLogin(ReqTaskExecutor reqTaskExecutorGiven, String email, String pw, String deviceID) {
		j = new JSONObject();
		this.reqTaskExecutor = reqTaskExecutorGiven;
		try {
			j.put("MESSAGE", "LOGIN_REQUEST");
			j.put("EMAIL", email);
			j.put("PASSWORD", pw);
			j.put("DEVICEID", deviceID);
		} catch (JSONException ex) {
			reqTaskExecutor.handleException(ex);
		}
	}

	/**
	 * Sends the current information to the server
	 */
	public void send() {
		NetworkJSON task = new NetworkJSON();
		NetworkJSON.APIRequest req;
		req = task.new APIRequest();
		req.request = j;
		req.reqTaskExecutor = this.reqTaskExecutor;
		task.execute(req);
	}
	
}
