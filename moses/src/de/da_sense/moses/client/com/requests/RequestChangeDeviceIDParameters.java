package de.da_sense.moses.client.com.requests;

import org.json.JSONException;
import org.json.JSONObject;

import de.da_sense.moses.client.com.NetworkJSON;
import de.da_sense.moses.client.com.ReqTaskExecutor;

import de.da_sense.moses.client.util.Log;

/**
 * This class is used for setting hardware parameters it provides some basic
 * methods for this
 * 
 * @author Zijad
 * 
 */
public class RequestChangeDeviceIDParameters {
	public static boolean parameterSetOnServer(JSONObject j) throws JSONException {
		if (j.getString("STATUS").equals("SUCCESS")) {
			return true;
		}
		return false;
	}

	private JSONObject j;

	ReqTaskExecutor e;

	public RequestChangeDeviceIDParameters(ReqTaskExecutor e, boolean force, String deviceid, String sessionID) {
		j = new JSONObject();
		this.e = e;
		try {
			j.put("MESSAGE", "CHANGE_DEVICE_ID");
			j.put("SESSIONID", sessionID);
			j.put("FORCE", force);
			j.put("DEVICEID", deviceid);
		} catch (JSONException ex) {
			e.handleException(ex);
		}
	}

	public void send() {
		Log.d("MoSeS.HARDWARE_ABSTRACTION", j.toString());
		NetworkJSON task = new NetworkJSON();
		NetworkJSON.APIRequest req;
		req = task.new APIRequest();
		req.request = j;
		req.e = this.e;
		task.execute(req);
	}
}
