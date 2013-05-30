package de.da_sense.moses.client.com.requests;

import org.json.JSONException;
import org.json.JSONObject;

import de.da_sense.moses.client.com.NetworkJSON;
import de.da_sense.moses.client.com.ReqTaskExecutor;

/**
 * This class is used for logging out it provides some basic methods for it
 * 
 * @author Zijad
 * 
 */
public class RequestLogout {
	public static boolean logoutValid(JSONObject j) throws JSONException {
		if (j.getString("STATUS").equals("SUCCESS")) {
			return true;
		}
		return false;
	}

	/**
	 * Instance of JSONObject to give t
	 */
	private JSONObject j;

	ReqTaskExecutor e;

	/**
	 * Creates a new Logout request
	 * @param e - A ReqTaskExecutor specifying what to do after the Logout and
	 * how to handle Exceptions. 
	 * @param sessionID - The current Session ID
	 */
	public RequestLogout(ReqTaskExecutor e, String sessionID) {
		j = new JSONObject();
		this.e = e;
		try {
			j.put("MESSAGE", "LOGOUT_REQUEST");
			j.put("SESSIONID", sessionID);
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
