package moses.client.com.requests;

import moses.client.abstraction.HardwareAbstraction;
import moses.client.com.NetworkJSON;
import moses.client.com.ReqTaskExecutor;
import moses.client.service.MosesService;

import org.json.JSONException;
import org.json.JSONObject;

import android.preference.PreferenceManager;

public class RequestUninstalledAPK {
	ReqTaskExecutor e;
	JSONObject j;

	public RequestUninstalledAPK(ReqTaskExecutor e, String sessionID, String appId) {
		j = new JSONObject();
		this.e = e;
		try {
			String userid = PreferenceManager.getDefaultSharedPreferences(MosesService.getInstance()).getString(
					"username_pref", "");
			j.put("MESSAGE", "APK_UNINSTALLED");
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
		req.e = this.e;
		task.execute(req);
	}
}
