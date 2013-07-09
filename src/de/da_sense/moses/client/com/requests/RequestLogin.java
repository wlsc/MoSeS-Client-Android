package de.da_sense.moses.client.com.requests;

import org.json.JSONException;
import org.json.JSONObject;

import de.da_sense.moses.client.com.NetworkJSON;
import de.da_sense.moses.client.com.ReqTaskExecutor;

/**
 * This class is used for logging in. It provides some basic methods for it.
 * 
 * @author Jaco Hofmann
 * @author Zijad Maksuti
 * 
 */
public class RequestLogin {
	
	
	/**
	 * Checks if there currently exists a valid connection to the server
	 * corresponding to the user logged in on the device.
	 * @param j The JSONObject containing the connection information
	 * @param email The Email
	 * @return True if there is a valid session between the user and the server
	 * @throws JSONException
	 */
	public static boolean loginValid(JSONObject j, String email) throws JSONException {
		String sessionID = j.getString("SESSIONID");
		if (!sessionID.equals("NULL")) {
			SESSION_ID = sessionID;
			return j.getString("EMAIL").equals(email);
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
	private ReqTaskExecutor reqTaskExecutor;

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
	 * Constructor for a RequestLogin. Prepares all clients information
	 * to be send to the server.
	 * @param reqTaskExecutorGiven An implementation of ReqTaskExecuter (usually LoginFunc)
	 * @param email the email
	 * @param pw The password
	 */
	public RequestLogin(ReqTaskExecutor reqTaskExecutorGiven, String email, String pw) {
		j = new JSONObject();
		this.reqTaskExecutor = reqTaskExecutorGiven;
		try {
			j.put("MESSAGE", "LOGIN_REQUEST");
			j.put("EMAIL", email);
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
	
}
