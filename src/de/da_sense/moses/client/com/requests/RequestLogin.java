package de.da_sense.moses.client.com.requests;

import org.json.JSONException;
import org.json.JSONObject;

import de.da_sense.moses.client.com.NetworkJSON;
import de.da_sense.moses.client.com.ReqTaskExecutor;

/**
 * This class is used for logging in. It provides some basic methods for it.
 * 
 * @author Jaco Hofmann
 * 
 */
public class RequestLogin {
	
	
	/**
	 * Checks if there currently exists a valid connection to the server
	 * corresponding to the user logged in on the device.
	 * @param j The JSONObject containing the connection information
	 * @param uname The Username
	 * @return True if there is a valid session between the user and the server
	 * @throws JSONException
	 */
	public static boolean loginValid(JSONObject j, String uname) throws JSONException {
		String sessionID = j.getString("SESSIONID");
		if (!sessionID.equals("NULL")) {
			SESSION_ID = sessionID;
			return j.getString("LOGIN").equals(uname);
		}
		return false;
	}

	/**
	 * The JSONObject that shall be send to the server.
	 */
	private JSONObject j;
	/**
	 * The instance of ReqTaskExecutor
	 */
	ReqTaskExecutor reqTaskExecutor;

	/**
	 * The current ID of the login given by the server
	 */
	private static String SESSION_ID = null; 

	/**
	 * Gives the current SessionID
	 * @return SESSION_ID
	 */
	public static String getSessionID() {
		return SESSION_ID;
	}

	/**
	 * Constructor for a RequestLogin. Prepares all clientsided information
	 * to be send to the server.
	 * @param reqTaskExecutorGiven An implementation of ReqTaskExecuter (usually LoginFunc)
	 * @param uname The Username
	 * @param pw The password
	 */
	public RequestLogin(ReqTaskExecutor reqTaskExecutorGiven, String uname, String pw) {
		j = new JSONObject();
		this.reqTaskExecutor = reqTaskExecutorGiven;
		try {
			j.put("MESSAGE", "LOGIN_REQUEST");
			j.put("LOGIN", uname);
			j.put("PASSWORD", pw);
		} catch (JSONException ex) {
			reqTaskExecutor.handleException(ex);
		}
	}

	/**
	 * Sends the current information to the server
	 */
	public void send() {
		NetworkJSON task = new NetworkJSON();
		NetworkJSON.APIRequest req;
		req = task.new APIRequest();
		req.request = j;
		req.reqTaskExecutor = this.reqTaskExecutor;
		task.execute(req);
	}

	/**
	 * Sets the Login and Password and sends the current information
	 * to the server
	 * @param uname The Username
	 * @param pw The Password
	 */
	public void send(String uname, String pw) {
		try {
			j.put("LOGIN", uname);
			j.put("PASSWORD", pw);
		} catch (JSONException e) {
			this.reqTaskExecutor.handleException(e);
		}
		send();
	}
}
