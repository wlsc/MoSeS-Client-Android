package de.da_sense.moses.client.com.requests;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.provider.Settings.Secure;
import de.da_sense.moses.client.abstraction.HardwareAbstraction.HardwareInfo;
import de.da_sense.moses.client.com.NetworkJSON;
import de.da_sense.moses.client.com.ReqTaskExecutor;
import de.da_sense.moses.client.service.MosesService;
import de.da_sense.moses.client.util.Log;

/**
 * This class is used for setting hardware parameters it provides some basic
 * methods for this
 * 
 * @author Zijad
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

	ReqTaskExecutor e;

	public RequestSetHardwareParameters(ReqTaskExecutor e, HardwareInfo hwInfo, boolean force, String sessionID) {
		j = new JSONObject();
		this.e = e;
		String uniqueid = Secure.getString(MosesService.getInstance().getContentResolver(), Secure.ANDROID_ID);
		try {
			j.put("MESSAGE", "SET_HARDWARE_PARAMS");
			j.put("SESSIONID", sessionID);
			j.put("FORCE", force);
			j.put("VENDOR_NAME", hwInfo.getDeviceVendor());
			j.put("MODEL_NAME", hwInfo.getDeviceModel());
			j.put("DEVICEID", hwInfo.getDeviceID());
			j.put("ANDVER", hwInfo.getSdkbuildversion());
			j.put("SENSORS", (new JSONArray(hwInfo.getSensors())));
			j.put("UNIQUEID", uniqueid);
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
