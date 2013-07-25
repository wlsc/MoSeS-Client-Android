package de.da_sense.moses.client.com.requests;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import de.da_sense.moses.client.abstraction.HardwareAbstraction.HardwareInfo;
import de.da_sense.moses.client.com.NetworkJSON;
import de.da_sense.moses.client.com.ReqTaskExecutor;
import de.da_sense.moses.client.util.Log;

/**
 * This class is used for setting hardware parameters it provides some basic
 * methods for this.
 * 
 * @author Zijad Maksuti
 * 
 */
public class RequestSetHardwareParameters {
	public static boolean parameterSetOnServer(JSONObject j) throws JSONException {
		if (j.getString("STATUS").equals("SUCCESS")) {
			return true;
		}
		return false;
	}

	private JSONObject j;

	private ReqTaskExecutor e;

	public RequestSetHardwareParameters(ReqTaskExecutor e, HardwareInfo hwInfo, String sessionID) {
		j = new JSONObject();
		this.e = e;
		try {
			j.put("MESSAGE", "SET_HARDWARE_PARAMS");
			j.put("SESSIONID", sessionID);
			j.put("VENDOR_NAME", hwInfo.getDeviceVendor());
			j.put("MODEL_NAME", hwInfo.getDeviceModel());
			j.put("DEVICEID", hwInfo.getDeviceID());
			j.put("ANDVER", hwInfo.getSdkbuildversion());
			j.put("SENSORS", (new JSONArray(hwInfo.getSensors())));
			j.put("DEVICENAME", hwInfo.getDeviceName());
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
		req.reqTaskExecutor = this.e;
		task.execute(req);
	}
}
