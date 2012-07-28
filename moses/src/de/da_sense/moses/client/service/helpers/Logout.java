package de.da_sense.moses.client.service.helpers;

import java.net.UnknownHostException;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.json.JSONException;
import org.json.JSONObject;

import de.da_sense.moses.client.com.ConnectionParam;
import de.da_sense.moses.client.com.ReqTaskExecutor;
import de.da_sense.moses.client.com.NetworkJSON.BackgroundException;
import de.da_sense.moses.client.com.requests.RequestLogout;
import de.da_sense.moses.client.service.MosesService;

import de.da_sense.moses.client.util.Log;

// TODO: Auto-generated Javadoc
/**
 * The Class Logout.
 * 
 * @author Jaco Hofmann
 */
public class Logout {

	/**
	 * The Class LogoutFunc.
	 */
	private class LogoutFunc implements ReqTaskExecutor {

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * moses.client.com.ReqTaskExecutor#handleException(java.lang.Exception)
		 */
		@Override
		public void handleException(Exception e) {
			if (e instanceof UnknownHostException || e instanceof JSONException) {
				Log.d("MoSeS.LOGOUT", "No internet connection present (or DNS problems.)");
			} else
				Log.d("MoSeS.LOGOUT", "FAILURE: " + e.getMessage());
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
				// TODO: Handle unsuccessful logout
				if (j.getString("MESSAGE").equals("LOGOUT_RESPONSE")) {
					serv.loggedOut();
					Login.lastLoggedIn = -1;
					for (ExecutableWithType ex : e) {
						ex.e.execute();
					}
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
			if (c.c == ConnectionParam.EXCEPTION) {
				handleException(c.e);
			}
		}

	}

	/** The serv. */
	private MosesService serv;

	/** The e. */
	private ConcurrentLinkedQueue<ExecutableWithType> e;

	/**
	 * Instantiates a new logout.
	 * 
	 * @param serv
	 *            the serv
	 * @param postLogoutHook
	 *            the e
	 */
	public Logout(MosesService serv, ConcurrentLinkedQueue<ExecutableWithType> postLogoutHook) {
		this.serv = serv;
		this.e = postLogoutHook;
		new RequestLogout(new LogoutFunc(), serv.getSessionID()).send();
	}
}
