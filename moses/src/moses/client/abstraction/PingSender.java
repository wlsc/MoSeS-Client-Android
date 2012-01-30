/**
 * 
 */
package moses.client.abstraction;

import java.net.UnknownHostException;

import moses.client.com.ConnectionParam;
import moses.client.com.NetworkJSON.BackgroundException;
import moses.client.com.ReqTaskExecutor;
import moses.client.com.requests.RequestLogin;
import moses.client.com.requests.RequestPing;
import moses.client.service.helpers.Executor;

import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

/**
 * This class offers methods for staying in touch with the server
 * 
 * @author Zijad Maksuti
 * 
 */

public class PingSender {

	private Executor e;
	private String c2dmId;

	private class ReqClassPing implements ReqTaskExecutor {

		@Override
		public void handleException(Exception e) {
			if (e instanceof UnknownHostException || e instanceof JSONException) {
				Log.d("MoSeS.PING",
						"No internet connection present (or DNS problems.)");
			} else {
				Log.d("MoSeS.PING", "FAILURE WHILE SENDING PING: " + e.getMessage());
				PingSender.this.e.execute();
			}
		}

		@Override
		public void postExecution(String s) {
			JSONObject j = null;
			try {
				j = new JSONObject(s);
				if (RequestPing.pingAccepted(j)) {
					Log.d("MoSeS.PING", "Ping set successfully, server returned positive response");
				} else {
					Log.d("MoSeS.PING", "Ping NOT set successfully, server returned NEGATIVE response");
					PingSender.this.e.execute();
				}
			} catch (JSONException e) {
				this.handleException(e);
			}
		}

		@Override
		public void updateExecution(BackgroundException c) {
			if (c.c == ConnectionParam.EXCEPTION) {
				handleException(c.e);
			}
		}
	}

	/**
	 * Constructs a new PingSender which methods can be used for periodical
	 * sending of pings to the server
	 * 
	 * @param c
	 *            the Context in which the pinger should operate
	 */
	public PingSender(Executor e) {
		this(e, null);
	}

	/**
	 * @param e2 the context
	 * @param c2dmId the c2dmId that can be sent along with every ping (set null to not send it along; c2dmId can be set later in the setter {@link #setC2dmId(String)} 
	 */
	public PingSender(Executor e, String c2dmId) {
		this.e=e;
		this.c2dmId = c2dmId;
	}

	public void setC2dmId(String c2dmId) {
		this.c2dmId = c2dmId;
	}

	/**
	 * This method sends a Ping to the server in order to refresh the session
	 * stored on the server
	 */
	public void sendPing() {
		String sessionID = RequestLogin.getSessionID(); // obtain the session id

		new RequestPing(new ReqClassPing(), sessionID, c2dmId).send();
	}

}
