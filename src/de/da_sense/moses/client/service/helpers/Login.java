package de.da_sense.moses.client.service.helpers;

import java.net.UnknownHostException;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.json.JSONException;
import org.json.JSONObject;

import android.os.Handler;
import de.da_sense.moses.client.R;
import de.da_sense.moses.client.com.ConnectionParam;
import de.da_sense.moses.client.com.NetworkJSON.BackgroundException;
import de.da_sense.moses.client.com.ReqTaskExecutor;
import de.da_sense.moses.client.com.requests.RequestLogin;
import de.da_sense.moses.client.service.MosesService;
import de.da_sense.moses.client.util.Log;


/**
 * The Class Login.
 * 
 * @author Jaco Hofmann
 */
public class Login {

	/**
	 * The Class LoginFunc - implements ReqTaskExecutor. Handles the
	 * data the server returns to a login. More specific, this class
	 * checks if the login was successful or not. In the latter case
	 * it Logs information concerning the problem. In the first case
	 * it performs all steps to register the device as logged in (set
	 * the boolean values, set the current time for the session
	 * lifetime and start all Hooks, which were waiting for the device
	 * to log in).
	 */
	private class LoginFunc implements ReqTaskExecutor {

		/*
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

	/** The username. */
	private String uname;

	/** The password. */
	private String pw;

	/** postExecuteSuccess hooks. */
	private ConcurrentLinkedQueue<ExecutableWithType> postExecuteSuccess;
	/** postExecuteSuccessPriority hooks. */
	private ConcurrentLinkedQueue<ExecutableWithType> postExecuteSuccessPriority;
	/** postExecuteFailure hooks. */
	private ConcurrentLinkedQueue<ExecutableWithType> postExecuteFailure;
	/** loginStart hooks */
	private ConcurrentLinkedQueue<ExecutableWithType> loginStart;
	/** loginEnd hooks */
	private ConcurrentLinkedQueue<ExecutableWithType> loginEnd;

	/**
	 * Timestamp where the device last logged in to check if session is still valid.
	 */
	public static long lastLoggedIn = -1;

	/**
	 * The amount of milliseconds a session is valid.
	 */
	private static final long sessionAliveTime = 120000;

	/**
	 * Executes all hooks in the ConcurrentLinkedQueue
	 * @param el the ConcurrentLinkedQueue
	 */
	private void executeAll(ConcurrentLinkedQueue<ExecutableWithType> el) {
		for (ExecutableWithType e : el) {
			e.e.execute();
		}
	}

	/**
	 * The handler for this Thread.
	 */
	private static Handler mHandler = new Handler();

	/**
	 * Simple task that runs all postExecuteSuccess hooks
	 */
	private Runnable executeHooksTask = new Runnable() {

		@Override
		public void run() {
			Log.d("MoSeS.LOGIN", "Executing post login hooks:");
			executeAll(postExecuteSuccess);
		}
	};

	/**
	 * Simple task that logs out
	 */
	private static Runnable logoutTask = new Runnable() {

		@Override
		public void run() {
			Log.d("MoSeS.LOGIN", "Session is now invalid.");
			MosesService.getInstance().logout();
		}
	};

	/**
	 * removes all LogoutTask from the queue in this thread
	 */
	public static void removeLogoutTask() {
		lastLoggedIn = -1;
		mHandler.removeCallbacks(logoutTask);
	}

	/**
	 * refreshes the LastLoggedIn time and therefore the SessionAliveTIme
	 */
	public static void refresh() {
		lastLoggedIn = System.currentTimeMillis();
		mHandler.removeCallbacks(logoutTask);
		mHandler.postDelayed(logoutTask, sessionAliveTime - 10);
	}

	/**
	 * Instantiates a new login. This class sorts all hooks to be able
	 * to call them once their needed state is reached. Furthermore it
	 * checks if the device is logged in and if not loggs it in by
	 * creating a RequestLogin. If the device is still logged in it
	 * performs first all POSTLOGINSUCCESSPRIORITY hooks and afterwards
	 * all POSTLOGINSUCCESS hooks. If the device is not logged in this
	 * task is handled by LoginFunc.
	 * 
	 * @param username
	 *            the username
	 * @param password
	 *            the password
	 */
	public Login(String username, String password) {
		this.pw = password;
		this.uname = username;
		this.postExecuteSuccess = MosesService.getInstance().getHook(
				EHookTypes.POST_LOGIN_SUCCESS);
		this.postExecuteSuccessPriority = MosesService.getInstance().getHook(
				EHookTypes.POST_LOGIN_SUCCESS_PRIORITY);
		this.postExecuteFailure = MosesService.getInstance().getHook(
				EHookTypes.POST_LOGIN_FAILED);
		this.loginEnd = MosesService.getInstance().getHook(
				EHookTypes.POST_LOGIN_END);
		this.loginStart = MosesService.getInstance().getHook(
				EHookTypes.POST_LOGIN_START);
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
