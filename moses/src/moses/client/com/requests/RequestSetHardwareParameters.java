package moses.client.com.requests;

import java.util.List;

import moses.client.com.NetworkJSON;
import moses.client.com.ReqTaskExecutor;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * This class is used for setting hardware parameters
 * it provides some basic methods for this
 * @author Zijad
 *
 */
public class RequestSetHardwareParameters {
	public static boolean parameterSetOnServer(JSONObject j) throws JSONException {
		if(j.getString("STATUS").equals("SUCCESS")) {
			return true;
		}
		return false;
	}
	private JSONObject j;
	
	ReqTaskExecutor e;
	
	
	public RequestSetHardwareParameters(ReqTaskExecutor e, String sessionID, String deviceID, String androidVersion, List<Integer> sensors) {
		j = new JSONObject();
		this.e = e;
		try {
			j.put("MESSAGE", "SET_HARDWARE_PARAMS");
			j.put("SESSIONID", sessionID);
			j.put("DEVICEID", deviceID);
			j.put("ANDVER", androidVersion);
			j.put("SENSORS", new JSONArray(sensors));
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
