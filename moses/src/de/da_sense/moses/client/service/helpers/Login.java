package de.da_sense.moses.client.service.helpers;

import java.net.UnknownHostException;
import java.util.concurrent.ConcurrentLinkedQueue;

import de.da_sense.moses.client.R;

import org.json.JSONException;
import org.json.JSONObject;

import de.da_sense.moses.client.com.ConnectionParam;
import de.da_sense.moses.client.com.ReqTaskExecutor;
import de.da_sense.moses.client.com.NetworkJSON.BackgroundException;
import de.da_sense.moses.client.com.requests.RequestLogin;
import de.da_sense.moses.client.service.MosesService;

import android.os.Handler;
import de.da_sense.moses.client.util.Log;

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
			if (e instanceof UnknownHostException) {
				Log.d("MoSeS.LOGIN",
						MosesService.getInstance().getString(
								R.string.no_internet_connection));
				MosesService.getInstance().executeChangeTextFieldHook(
						MosesService.getInstance().getString(
								R.string.no_internet_connection));
			} else if (e instanceof JSONException) {
				Log.d("MoSeS.LOGIN",
						MosesService.getInstance().getString(
								R.string.unknown_response_from_server));
				MosesService.getInstance().executeChangeTextFieldHook(
						MosesService.getInstance().getString(
								R.string.unknown_response_from_server));
			} else {
				Log.d("MoSeS.LOGIN", "Unknown failure: "
						+ e.getClass().toString() + " " + e.getMessage());
				MosesService.getInstance().executeChangeTextFieldHook(
						"Unknown failure during login.");
			}
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
					MosesService.getInstance().loggedIn(
							j.getString("SESSIONID"));
					mHandler.removeCallbacks(logoutTask);
					mHandler.postDelayed(logoutTask, sessionAliveTime - 1000);
					lastLoggedIn = System.currentTimeMillis();
					Log.d("MoSeS.LOGIN",
							"ACCESS GRANTED: " + j.getString("SESSIONID"));
					Log.d("MoSeS.LOGIN", "Executing post login priority hooks:");
					executeAll(postExecuteSuccessPriority);
					mHandler.removeCallbacks(executeHooksTask);
					mHandler.postDelayed(executeHooksTask, 500);
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
				if (c.c == ConnectionParam.CONNECTING) {
					executeAll(loginStart);
				} else if (c.c == ConnectionParam.CONNECTED) {
					executeAll(loginEnd);
				}
			} else {
				handleException(c.e);
			}
		}

	}

	/** The uname. */
	private String uname;

	/** The pw. */
	private String pw;

	/** The e. */
	private ConcurrentLinkedQueue<ExecutableWithType> postExecuteSuccess;
	private ConcurrentLinkedQueue<ExecutableWithType> postExecuteSuccessPriority;
	private ConcurrentLinkedQueue<ExecutableWithType> postExecuteFailure;
	private ConcurrentLinkedQueue<ExecutableWithType> loginStart;
	private ConcurrentLinkedQueue<ExecutableWithType> loginEnd;

	public static long lastLoggedIn = -1;

	private static final long sessionAliveTime = 120000;

	private void executeAll(ConcurrentLinkedQueue<ExecutableWithType> el) {
		for (ExecutableWithType e : el) {
			e.e.execute();
		}
	}

	private static Handler mHandler = new Handler();

	private Runnable executeHooksTask = new Runnable() {

		@Override
		public void run() {
			Log.d("MoSeS.LOGIN", "Executing post login hooks:");
			executeAll(postExecuteSuccess);
		}
	};

	private static Runnable logoutTask = new Runnable() {

		@Override
		public void run() {
			Log.d("MoSeS.LOGIN", "Session is now invalid.");
			MosesService.getInstance().logout();
		}
	};

	public static void removeLogoutTask() {
		lastLoggedIn = -1;
		mHandler.removeCallbacks(logoutTask);
	}

	public static void refresh() {
		lastLoggedIn = System.currentTimeMillis();
		mHandler.removeCallbacks(logoutTask);
		mHandler.postDelayed(logoutTask, sessionAliveTime - 10);
	}

	/**
	 * Instantiates a new login.
	 * 
	 * @param username
	 *            the username
	 * @param password
	 *            the password
	 * @param e
	 *            the e
	 */
	public Login(String username, String password) {
		this.pw = password;
		this.uname = username;
		this.postExecuteSuccess = MosesService.getInstance().getHook(
				EHookTypes.POSTLOGINSUCCESS);
		this.postExecuteSuccessPriority = MosesService.getInstance().getHook(
				EHookTypes.POSTLOGINSUCCESSPRIORITY);
		this.postExecuteFailure = MosesService.getInstance().getHook(
				EHookTypes.POSTLOGINFAILED);
		this.loginEnd = MosesService.getInstance().getHook(
				EHookTypes.POSTLOGINEND);
		this.loginStart = MosesService.getInstance().getHook(
				EHookTypes.POSTLOGINSTART);
		if (System.currentTimeMillis() - lastLoggedIn > sessionAliveTime) {
			new RequestLogin(new LoginFunc(), uname, pw).send();
		} else {
			Log.d("MoSeS.LOGIN", "Session still active.");
			Log.d("MoSeS.LOGIN", "Post login success priority: ");
			executeAll(postExecuteSuccessPriority);
			Log.d("MoSeS.LOGIN", "Post login success: ");
			executeAll(postExecuteSuccess);
			MosesService.getInstance().loggedIn(
					MosesService.getInstance().getSessionID());
		}
	}
}
