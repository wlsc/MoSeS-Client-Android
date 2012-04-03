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
	ReqTaskExecutor e;

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
		req.e = this.e;
		task.execute(req);
	}
}
