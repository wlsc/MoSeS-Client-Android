/**
 * 
 */
package moses.client.abstraction;

import moses.client.com.ConnectionParam;
import moses.client.com.NetworkJSON.BackgroundException;
import moses.client.com.ReqTaskExecutor;
import moses.client.com.requests.RequestLogin;
import moses.client.com.requests.RequestPing;
import moses.client.service.helpers.Executor;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;

/**
 * This class offers methods for staying in touch with the server
 * 
 * @author Zijad Maksuti
 * 
 */

public class PingSender {

	private String lastMessage;
	private Executor e;

	private class ReqClassPing implements ReqTaskExecutor {

		@Override
		public void handleException(Exception e) {
			lastMessage = "FAILURE ON SENDING PING: " + e.getMessage();
			PingSender.this.e.execute();
		}

		@Override
		public void postExecution(String s) {
			JSONObject j = null;
			try {
				j = new JSONObject(s);
				// TODO handling
				if (RequestPing.pingAccepted(j)) {
					lastMessage = "Ping set successfully, server returned positive response";
					PingSender.this.e.execute();
				} else {
					// TODO handling
					lastMessage = "Ping NOT set successfully, server returned NEGATIVE response";
					PingSender.this.e.execute();
				}
			} catch (JSONException e) {
				this.handleException(e);
			}
		}

		@Override
		public void updateExecution(BackgroundException c) {
			if (c.c != ConnectionParam.EXCEPTION) {
				lastMessage = c.c.toString();
				PingSender.this.e.execute();
			} else {
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
		this.e = e;
	}

	public String getLastMessage() {
		return lastMessage;
	}

	/**
	 * This method sends a Ping to the server in order to refresh the session
	 * stored on the server
	 */
	public void sendPing() {
		String sessionID = RequestLogin.getSessionID(); // obtain the session id

		new RequestPing(new ReqClassPing(), sessionID).send();
	}

}
