package moses.client.com.requests;

import org.json.JSONException;
import org.json.JSONObject;

import moses.client.com.NetworkJSON;
import moses.client.com.ReqTaskExecutor;

public class RequestInstalledAPK {
	ReqTaskExecutor e;
	JSONObject j;

	public RequestInstalledAPK(ReqTaskExecutor e, String sessionID, String appId) {
		j = new JSONObject();
		this.e = e;
		try {
			j.put("MESSAGE", "APK_INSTALLED");
			j.put("SESSIONID", sessionID);
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
