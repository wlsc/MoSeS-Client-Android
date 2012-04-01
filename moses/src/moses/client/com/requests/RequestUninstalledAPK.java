package moses.client.com.requests;

import moses.client.com.NetworkJSON;
import moses.client.com.ReqTaskExecutor;

import org.json.JSONException;
import org.json.JSONObject;

public class RequestUninstalledAPK {
	ReqTaskExecutor e;
	JSONObject j;

	public RequestUninstalledAPK(ReqTaskExecutor e, String sessionID, String appId) {
		j = new JSONObject();
		this.e = e;
		try {
			j.put("MESSAGE", "APK_UNINSTALLED");
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