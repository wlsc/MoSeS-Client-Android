package moses.client.com.requests;

import moses.client.com.NetworkJSON;
import moses.client.com.ReqTaskExecutor;
import moses.client.service.MosesService;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * This is for requesing the download link to an external application
 * 
 * @author Simon L
 * 
 */

public class RequestDownloadlink {
	public static boolean downloadLinkRequestAccepted(JSONObject j) throws JSONException {
		String messageTitle = j.getString("MESSAGE");
		return messageTitle.equals("DOWNLOAD_RESPONSE");
	}

	private JSONObject j;
	ReqTaskExecutor e;

	public RequestDownloadlink(ReqTaskExecutor e, String sessionID, String appId) {
		j = new JSONObject();
		this.e = e;
		try {
			j.put("MESSAGE", "DOWNLOAD_REQUEST");
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
