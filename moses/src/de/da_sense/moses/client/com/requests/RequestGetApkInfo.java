package de.da_sense.moses.client.com.requests;

import org.json.JSONException;
import org.json.JSONObject;

import de.da_sense.moses.client.abstraction.HardwareAbstraction;
import de.da_sense.moses.client.com.NetworkJSON;
import de.da_sense.moses.client.com.ReqTaskExecutor;

/**
 * This class represents a Request for obtaining the list of avaliable APKs for
 * this device from the Server
 * 
 * @author Simon Leischnig
 * 
 */
public class RequestGetApkInfo {

	/**
	 * Returns true when the server has returned the success-response
	 * 
	 * @param j
	 * @return true when the server has returned a success-response, else false
	 * @throws JSONException
	 */
	public static boolean isInfoRetrieved(JSONObject j) throws JSONException {
		return j.getString("MESSAGE").equals("GET_APK_INFO_RESPONSE") && j.getString("STATUS").equals("SUCCESS");
	}

	private JSONObject j;

	private ReqTaskExecutor e;

	/**
	 * Generates a new Request for apk info
	 * 
	 * @param e
	 * @param apkId
	 * @param sessionID
	 */
	public RequestGetApkInfo(ReqTaskExecutor e, String apkId, String sessionID) {
		j = new JSONObject();
		this.e = e;
		try {
			j.put("MESSAGE", "GET_APK_INFO");
			j.put("ID", apkId);
			j.put("SESSIONID", sessionID);
			j.put("DEVICEID", HardwareAbstraction.extractDeviceIdFromSharedPreferences());
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
