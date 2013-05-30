package de.da_sense.moses.client.com.requests;

import org.json.JSONException;
import org.json.JSONObject;

import de.da_sense.moses.client.abstraction.HardwareAbstraction;
import de.da_sense.moses.client.com.NetworkJSON;
import de.da_sense.moses.client.com.ReqTaskExecutor;
import de.da_sense.moses.client.util.Log;

/**
 * This class represents a Request for obtaining the list of avaliable APKs for
 * this device from the Server
 * 
 * @author Zijad Maksuti
 * 
 */
public class RequestGetListAPK {

	/**
	 * Returns true when the server has returned the success-response
	 * 
	 * @param j
	 * @return true when the server has returned a success-response, else false
	 * @throws JSONException
	 */
	public static boolean isListRetrieved(JSONObject j) throws JSONException {
		return j.getString("MESSAGE").equals("GET_APK_LIST_RESPONSE") && j.getString("STATUS").equals("SUCCESS");
	}

	private JSONObject j;

	ReqTaskExecutor e;

	/**
	 * Generates a new Request for obtaining the the list of avaliable APKs from
	 * the website
	 * 
	 * @param e
	 * @param sessionID
	 *            id of the session with the server
	 */
	public RequestGetListAPK(ReqTaskExecutor e, String sessionID) {
		j = new JSONObject();
		this.e = e;
		try {
			j.put("MESSAGE", "GET_APK_LIST_REQUEST");
			j.put("SESSIONID", sessionID);
			j.put("DEVICEID", HardwareAbstraction.extractDeviceId());
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
		Log.d("MoSeS.REQGETLISTAPK", "sent request: " + j.toString());
		task.execute(req);
	}
}
