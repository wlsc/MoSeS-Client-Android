package moses.client.com.requests;

import org.json.JSONException;
import org.json.JSONObject;

import moses.client.com.NetworkJSON;
import moses.client.com.ReqTaskExecutor;

/**
 * This class is used for setting hardware parameters
 * it provides some basic methods for this
 * @author Zijad
 *
 */
public class RequestSetHardwareParameters {
	private JSONObject j;
	ReqTaskExecutor e;
	
	public RequestSetHardwareParameters(ReqTaskExecutor e, String sessionID, String deviceID, String androidVersion, String sensors) {
		j = new JSONObject();
		this.e = e;
		try {
			j.put("MESSAGE", "SET_HARDWARE_PARAMS");
			j.put("SESSIONID", sessionID);
			j.put("DEVICEID", deviceID);
			j.put("ANDVER", androidVersion);
			j.put("SENSORS", sensors);
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
	
	public static boolean parameterSetOnServer(JSONObject j) throws JSONException {
		if(j.getString("STATUS").equals("SUCCESS")) {
			return true;
		}
		return false;
	}
}
