package moses.client.com.requests;

import org.json.JSONException;
import org.json.JSONObject;

import moses.client.com.NetworkJSON;
import moses.client.com.ReqTaskExecutor;

public class RequestLogin {
	private JSONObject j;
	ReqTaskExecutor e;
	
	public RequestLogin(ReqTaskExecutor e, String uname, String pw) {
		j = new JSONObject();
		this.e = e;
		try {
			j.put("MESSAGE", "LOGIN_REQUEST");
			j.put("LOGIN", uname);
			j.put("PASSWORD", pw);
		} catch (JSONException ex) {
			e.handleException(ex);
		}
	}
	
	public void send(String uname, String pw) {
		try {
			j.put("LOGIN", uname);
			j.put("PASSWORD", pw);
		} catch (JSONException e) {
			this.e.handleException(e);
		}
		send();
	}
	
	public void send() {
		NetworkJSON task = new NetworkJSON();
		NetworkJSON.APIRequest req;
		req = task.new APIRequest();
		req.request = j;
		req.e = this.e;
		task.execute(req);
	}
	
	public static boolean loginValid(JSONObject j, String uname) throws JSONException {
		if(!j.getString("SESSIONID").equals("NULL")) {
			return j.getString("LOGIN").equals(uname);
		}
		return false;
	}
}
