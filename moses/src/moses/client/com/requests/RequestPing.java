package moses.client.com.requests;

import moses.client.com.NetworkJSON;
import moses.client.com.ReqTaskExecutor;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * This class is used for sending "i am alive" messages to the server it
 * provides some basic methods for it
 * 
 * @author Zijad Maksuti
 * 
 */

public class RequestPing {
	public static boolean pingAccepted(JSONObject j) throws JSONException {
		String messageTitle = j.getString("MESSAGE");
		String pingStatus = j.getString("STATUS");

		return messageTitle.equals("HELLO_THERE")
				&& pingStatus.equals("SUCCESS");
	}

	private JSONObject j;
	ReqTaskExecutor e;

	/**
	 * @param e the executor
	 * @param sessionID the session id
	 * @param c2dmId (null if the c2dm id is not know yet) the c2dm id
	 */
	public RequestPing(ReqTaskExecutor e, String sessionID, String c2dmId) {
		j = new JSONObject();
		this.e = e;
		try {
			j.put("MESSAGE", "STILL_ALIVE");
//			j.put("C2DMID", c2dmId==null?"NOID":c2dmId);
			j.put("SESSIONID", sessionID);
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
