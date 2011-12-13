package moses.client.com.requests;

import moses.client.com.NetworkJSON;
import moses.client.com.ReqTaskExecutor;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * This class is used for getting hardware parameters
 * for this device and user, stored on the website
 * (not to be confused with filter)
 * it provides some basic methods for this
 * @author Zijad
 *
 */
public class RequestGetHardwareParameters {
	
	/**
	 * Returns true when the server has returned
	 * the success-response
	 * @param j
	 * @return true when the server has returned a success-response, else false
	 * @throws JSONException
	 */
	public static boolean parameterAcquiredFromServer(JSONObject j) throws JSONException {
		if(j.getString("STATUS").equals("SUCCESS")) 
			return true;
		return false;
	}
	private JSONObject j;
	
	ReqTaskExecutor e;
	
	
	/**
	 * Generates RequestGetHardwareParameters used for retrieving the hardware parameters stored for a device on the server
	 * @param e
	 * @param sessionID id of the session with the server
	 * @param deviceID id of the device
	 */
	public RequestGetHardwareParameters(ReqTaskExecutor e, String sessionID, String deviceID) {
		j = new JSONObject();
		this.e = e;
		try {
			j.put("MESSAGE", "GET_HARDWARE_PARAMS");
			j.put("SESSIONID", sessionID);
			j.put("DEVICEID", deviceID);
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
