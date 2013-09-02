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
 * This is for sending the C2DM id
 * 
 * @author Simon L
 * 
 */

public class RequestC2DM {
	public static boolean C2DMRequestAccepted(JSONObject j) throws JSONException {
		String messageTitle = j.getString("MESSAGE");
		return messageTitle.equals("C2DM") && j.getString("STATUS").equals("SUCCESS");
	}

	private JSONObject j;
	private ReqTaskExecutor e;

	public RequestC2DM(ReqTaskExecutor e, String sessionID, String deviceId, String c2dmId) {
		j = new JSONObject();
		this.e = e;
		try {
			j.put("MESSAGE", "C2DM");
			j.put("SESSIONID", sessionID);
			j.put("DEVICEID", deviceId);
			j.put("C2DM", c2dmId);
		} catch (JSONException ex) {
			e.handleException(ex);
		}
	}

	public void send() {

		NetworkJSON task = new NetworkJSON();
		NetworkJSON.APIRequest req;
		req = task.new APIRequest();
		req.request = j;
		req.reqTaskExecutor = this.e;
		task.execute(req);
	}
}
