package moses.client.service.helpers;

import moses.client.com.ConnectionParam;
import moses.client.com.NetworkJSON.BackgroundException;
import moses.client.com.ReqTaskExecutor;
import moses.client.com.requests.RequestLogin;
import moses.client.service.MosesService;

import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

// TODO: Auto-generated Javadoc
/**
 * The Class Login.
 *
 * @author Jaco Hofmann
 */
public class Login {

	/**
	 * The Class LoginFunc.
	 */
	private class LoginFunc implements ReqTaskExecutor {

		/*
		 * (non-Javadoc)
		 *
		 * @see
		 * moses.client.com.ReqTaskExecutor#handleException(java.lang.Exception)
		 */
		@Override
		public void handleException(Exception e) {
			Log.d("MoSeS.LOGIN", "FAILURE: " + e.getMessage());
		}

		/*
		 * (non-Javadoc)
		 *
		 * @see moses.client.com.ReqTaskExecutor#postExecution(java.lang.String)
		 */
		@Override
		public void postExecution(String s) {
			JSONObject j = null;
			try {
				j = new JSONObject(s);
				if (RequestLogin.loginValid(j, uname)) {
					serv.loggedIn(j.getString("SESSIONID"));
					Log.d("MoSeS.LOGIN", "ACCESS GRANTED: " + j.getString("SESSIONID"));
					e.execute();
				} else {
					Log.d("MoSeS.LOGIN", "NOT GRANTED: " + j.toString());
				}
			} catch (JSONException e) {
				this.handleException(e);
			}

		}

		/*
		 * (non-Javadoc)
		 *
		 * @see
		 * moses.client.com.ReqTaskExecutor#updateExecution(moses.client.com
		 * .NetworkJSON.BackgroundException)
		 */
		@Override
		public void updateExecution(BackgroundException c) {
			if (c.c != ConnectionParam.EXCEPTION) {
				Log.d("MoSeS.LOGIN", c.c.toString());
			} else {
				handleException(c.e);
			}
		}

	}

	/** The serv. */
	private MosesService serv;

	/** The uname. */
	private String uname;

	/** The pw. */
	private String pw;

	/** The e. */
	private Executor e;

	/**
	 * Instantiates a new login.
	 *
	 * @param username
	 *            the username
	 * @param password
	 *            the password
	 * @param serv
	 *            the serv
	 * @param e
	 *            the e
	 */
	public Login(String username, String password, MosesService serv, Executor e) {
		this.serv = serv;
		this.pw = password;
		this.uname = username;
		this.e = e;
		new RequestLogin(new LoginFunc(), uname, pw).send();
	}
}
