package de.da_sense.moses.client.com.requests;

import org.json.JSONException;
import org.json.JSONObject;

import android.preference.PreferenceManager;
import de.da_sense.moses.client.abstraction.HardwareAbstraction;
import de.da_sense.moses.client.com.NetworkJSON;
import de.da_sense.moses.client.com.ReqTaskExecutor;
import de.da_sense.moses.client.service.MosesService;

public class RequestInstalledAPK {
	ReqTaskExecutor e;
	JSONObject j;

	public RequestInstalledAPK(ReqTaskExecutor e, String sessionID, String appId) {
		j = new JSONObject();
		this.e = e;
		try {
			String userid = PreferenceManager.getDefaultSharedPreferences(MosesService.getInstance()).getString(
					"username_pref", "");
			j.put("MESSAGE", "APK_INSTALLED");
			j.put("SESSIONID", sessionID);
			j.put("DEVICEID", HardwareAbstraction.extractDeviceId());
			j.put("USERID", userid);
			j.put("APKID", appId);
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
