/**
 * 
 */
package moses.client.abstraction;

import moses.client.com.ConnectionParam;
import moses.client.com.NetworkJSON.BackgroundException;
import moses.client.com.ReqTaskExecutor;
import moses.client.com.requests.RequestLogin;
import moses.client.com.requests.RequestPing;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;

/**
 * This class offers methods for staying in touch with the server
 * @author Zijad Maksuti
 *
 */

public class PingSender {
	
	private static AlertDialog infoDialog; // used for showing the results
	
	/**
	 * Constructs a new PingSender
	 * which methods can be used for periodical sending of pings to the server
	 * @param c the Context in which the pinger should operate
	 */
	public PingSender(Context c) {
		infoDialog = new AlertDialog.Builder(c).create();
		infoDialog.setTitle("INFO:");
		infoDialog.setButton(DialogInterface.BUTTON_POSITIVE, "Ok",
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						infoDialog.dismiss();
					}
				});
	}
	
	
	/**
	 * This method sends a Ping to the server in order
	 * to refresh the session stored on the server
	 */
	public void sendPing() {
		String sessionID = RequestLogin.getSessionID(); // obtain the session id
		
		RequestPing rPing = new RequestPing(
				new ReqClassPing(), sessionID);
		rPing.send();
	}
	
	
	
	private class ReqClassPing implements ReqTaskExecutor {

		@Override
		public void handleException(Exception e) {
			infoDialog.setMessage("FAILURE ON SENDING PING: " + e.getMessage());
			infoDialog.show();
		}

		@Override
		public void postExecution(String s) {
			JSONObject j = null;
			try {
				j = new JSONObject(s);
				// TODO handling
				if (RequestPing.pingAccepted(j)) {
					infoDialog.setMessage("Ping set successfully, server returned positive response");
					infoDialog.show();
				} else {
					// TODO handling
					infoDialog.setMessage("Ping NOT set successfully, server returned NEGATIVE response");
					infoDialog.show();
				}
			} catch (JSONException e) {
				this.handleException(e);
			}
		}

		@Override
		public void updateExecution(BackgroundException c) {
			if (c.c != ConnectionParam.EXCEPTION) {
				infoDialog.setMessage(c.c.toString());
				infoDialog.show();
			} else {
				handleException(c.e);
			}
		}
	}
	
	
}
