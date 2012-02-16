package moses.client.com.requests;

import moses.client.abstraction.HardwareAbstraction.HardwareInfo;
import moses.client.com.NetworkJSON;
import moses.client.com.ReqTaskExecutor;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

/**
 * This class is used for setting hardware parameters it provides some basic
 * methods for this
 * 
 * @author Zijad
 * 
 */
public class RequestSetHardwareParameters {
	public static boolean parameterSetOnServer(JSONObject j)
			throws JSONException {
		if (j.getString("STATUS").equals("SUCCESS")) {
			return true;
		}
		return false;
	}

	private JSONObject j;

	ReqTaskExecutor e;

	public RequestSetHardwareParameters(ReqTaskExecutor e, HardwareInfo hwInfo,
			boolean force, String sessionID) {
		j = new JSONObject();
		this.e = e;
		try {
			j.put("MESSAGE", "SET_HARDWARE_PARAMS");
			j.put("SESSIONID", sessionID);
			j.put("FORCE", force);
			j.put("VENDOR_NAME", hwInfo.getDeviceVendor());
			j.put("MODEL_NAME", hwInfo.getDeviceModel());
			j.put("DEVICEID", hwInfo.getDeviceID());
			j.put("ANDVER", hwInfo.getSdkbuildversion());
			j.put("SENSORS", (new JSONArray(hwInfo.getSensors())));
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
