package moses.client.com.requests;

import org.json.JSONException;
import org.json.JSONObject;

import moses.client.com.NetworkJSON;
import moses.client.com.ReqTaskExecutor;

/**
 * This class is used for logging out
 * it provides some basic methods for it
 * @author Zijad
 *
 */
public class RequestLogout {
	private JSONObject j;
	ReqTaskExecutor e;
	
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
		req.e = this.e;
		task.execute(req);
	}
	
	public static boolean logoutValid(JSONObject j) throws JSONException {
		if(j.getString("STATUS").equals("SUCCESS")) {
			return true;
		}
		return false;
	}
}
