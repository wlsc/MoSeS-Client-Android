package moses.client.service.helpers;

import java.net.UnknownHostException;
import java.util.concurrent.ConcurrentLinkedQueue;

import moses.client.com.ConnectionParam;
import moses.client.com.NetworkJSON.BackgroundException;
import moses.client.com.ReqTaskExecutor;
import moses.client.com.requests.RequestLogin;
import moses.client.service.MosesService;

import org.json.JSONException;
import org.json.JSONObject;

import android.os.Handler;
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
			executeAll(postExecuteFailure);
			if (e instanceof UnknownHostException || e instanceof JSONException) {
				Log.d("MoSeS.LOGIN",
						"No internet connection present (or DNS problems.)");
			} else
				Log.d("MoSeS.LOGIN", "FAILURE: " + e.getClass().toString()
						+ " " + e.getMessage());
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see moses.client.com.ReqTaskExecutor#postExecution(java.lang.String)
		 */
		@Override
		public void postExecution(String s) {
			Log.d("MoSeS.LOGIN", "Returned: " + s);
			JSONObject j = null;
			try {
				j = new JSONObject(s);
				if (RequestLogin.loginValid(j, uname)) {
					serv.loggedIn(j.getString("SESSIONID"));
					mHandler.removeCallbacks(logoutTask);
					mHandler.postDelayed(logoutTask, sessionAliveTime-1000);
					lastLoggedIn = System.currentTimeMillis();
					Log.d("MoSeS.LOGIN",
							"ACCESS GRANTED: " + j.getString("SESSIONID"));
					executeAll(postExecuteSuccess);
				} else {
					Log.d("MoSeS.LOGIN", "NOT GRANTED: " + j.toString());
					executeAll(postExecuteFailure);
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
				if(c.c == ConnectionParam.CONNECTING) {
					executeAll(loginStart);
				} else if(c.c == ConnectionParam.CONNECTED) {
					executeAll(loginEnd);
				}
			} else {
				handleException(c.e);
			}
		}

	}

	/** The serv. */
	private static MosesService serv;

	/** The uname. */
	private String uname;

	/** The pw. */
	private String pw;

	/** The e. */
	private ConcurrentLinkedQueue<Executor> postExecuteSuccess;
	private ConcurrentLinkedQueue<Executor> postExecuteFailure;
	private ConcurrentLinkedQueue<Executor> loginStart;
	private ConcurrentLinkedQueue<Executor> loginEnd;
	
	public static long lastLoggedIn = -1;
	
	private final long sessionAliveTime = 120000;

	private void executeAll(ConcurrentLinkedQueue<Executor> el) {
		for(Executor e : el) {
			e.execute();
		}
	}
	
	private static Handler mHandler = new Handler();
	
	private static Runnable logoutTask = new Runnable() {
		
		@Override
		public void run() {
			Log.d("MoSeS.LOGIN", "Session is now invalid.");
			serv.logout();
		}
	};
	
	public static void setService(MosesService s) {
		serv = s;
	}
	
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
	public Login(String username, String password, 
			ConcurrentLinkedQueue<Executor> postExecuteSuccess, ConcurrentLinkedQueue<Executor> postExecuteFailure,
			ConcurrentLinkedQueue<Executor> loginStart, ConcurrentLinkedQueue<Executor> loginEnd) {
		this.pw = password;
		this.uname = username;
		this.postExecuteSuccess = postExecuteSuccess;
		this.postExecuteFailure = postExecuteFailure;
		this.loginEnd = loginEnd;
		this.loginStart = loginStart;
		if(System.currentTimeMillis() - lastLoggedIn > sessionAliveTime) {
			new RequestLogin(new LoginFunc(), uname, pw).send();
		} else {
			executeAll(postExecuteSuccess);
			Log.d("MoSeS.LOGIN", "Session still active.");
		}
	}
}
