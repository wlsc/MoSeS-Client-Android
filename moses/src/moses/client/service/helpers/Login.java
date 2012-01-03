package moses.client.service.helpers;

import org.json.JSONException;
import org.json.JSONObject;

import android.widget.Toast;
import moses.client.com.NetworkJSON.BackgroundException;
import moses.client.com.requests.RequestLogin;
import moses.client.com.ConnectionParam;
import moses.client.com.ReqTaskExecutor;
import moses.client.service.MosesService;

public class Login {

	private MosesService serv;
	private String uname;
	private String pw;

	public Login(String username, String password, MosesService serv) {
		this.serv = serv;
		this.pw = password;
		this.uname = username;
		RequestLogin r = new RequestLogin(new LoginFunc(), uname, pw);
		r.send();
	}

	private class LoginFunc implements ReqTaskExecutor {

		@Override
		public void handleException(Exception e) {
			Toast.makeText(serv, "FAILURE: " + e.getMessage(),
					Toast.LENGTH_LONG).show();
		}

		@Override
		public void postExecution(String s) {
			JSONObject j = null;
			try {
				j = new JSONObject(s);
				if (RequestLogin.loginValid(j, uname)) {
					serv.loggedIn(j.getString("SESSIONID"));
				} else {
					Toast.makeText(serv, "NOT GRANTED: " + j.toString(),
							Toast.LENGTH_LONG).show();
				}
			} catch (JSONException e) {
				this.handleException(e);
			}

		}

		@Override
		public void updateExecution(BackgroundException c) {
			if (c.c != ConnectionParam.EXCEPTION) {
				Toast.makeText(serv, c.c.toString(), Toast.LENGTH_LONG).show();
			} else {
				handleException(c.e);
			}
		}

	}
}
