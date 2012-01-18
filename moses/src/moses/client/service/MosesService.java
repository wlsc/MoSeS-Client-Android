package moses.client.service;

import org.json.JSONArray;

import moses.client.abstraction.HardwareAbstraction;
import moses.client.service.helpers.CheckForNewApplications;
import moses.client.service.helpers.Executor;
import moses.client.service.helpers.KeepSessionAlive;
import moses.client.service.helpers.Login;
import moses.client.service.helpers.Logout;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

/**
 * The Class MosesService.
 *
 * @author Jaco Hofmann
 */
public class MosesService extends android.app.Service {

	/**
	 * The Class LocalBinder.
	 */
	public class LocalBinder extends Binder {

		/**
		 * Gets the service.
		 *
		 * @return the service
		 */
		public MosesService getService() {
			return MosesService.this;
		}
	}

	/**
	 * The Class MosesSettings.
	 */
	public class MosesSettings {

		/** The loginauto. */
		public boolean loginauto;

		/** The saveunamepw. */
		public boolean saveunamepw;

		/** The username. */
		public String username;

		/** The password. */
		public String password;

		/** The sessionid. */
		public String sessionid = "";

		/** The logged in. */
		public boolean loggedIn = false;

		public boolean loggingIn = false;

		/** Saves the used filter. */
		public JSONArray filter = null;

		public Executor postLoginHook = null;

	}

	/** The m binder. */
	private final IBinder mBinder = new LocalBinder();

	/** The settings file. */
	private SharedPreferences settingsFile;

	/** The mset. */
	private MosesSettings mset = new MosesSettings();

	private KeepSessionAlive cKeepAlive;

	private CheckForNewApplications checkForNewApplications;

	/**
	 * Gets the session id.
	 *
	 * @return the session id
	 */
	public String getSessionID() {
		return mset.sessionid;
	}

	/**
	 * Inits the config.
	 */
	private void initConfig() {
		settingsFile = getSharedPreferences("MoSeS.cfg", 0);
		mset.saveunamepw = settingsFile.getBoolean("saveunamepw", false);
		mset.loginauto = settingsFile.getBoolean("loginauto", false);
		mset.username = settingsFile.getString("uname", "");
		mset.password = settingsFile.getString("password", "");
		if (mset.loginauto)
			login();
	}

	/**
	 * Checks if is logged in.
	 *
	 * @return true, if is logged in
	 */
	public boolean isLoggedIn() {
		return mset.loggedIn;
	}

	public void setFilter(JSONArray filter) {
		mset.filter = filter;
	}

	public JSONArray getFilter() {
		return mset.filter;
	}

	/**
	 * Logged in.
	 *
	 * @param sessionid
	 *            the sessionid
	 */
	public void loggedIn(String sessionid) {
		mset.loggedIn = true;
		mset.loggingIn = false;
		mset.sessionid = sessionid;
		new HardwareAbstraction(this).getFilter();
	}

	/**
	 * Logged out.
	 */
	public void loggedOut() {
		mset.loggedIn = false;
		mset.sessionid = "";
	}

	/**
	 * Login.
	 *
	 * @param e
	 *            the e
	 */
	public void login() {
		if (!mset.loggedIn && !mset.loggingIn) {
			mset.loggingIn = true;
			new Login(mset.username, mset.password, this, mset.postLoginHook);
			keepSessionAlive(true);
			checkForNewApplications.startChecking(true);
		}
	}

	/**
	 * Logout.
	 *
	 * @param e
	 *            the e
	 */
	public void logout(Executor e) {
		new Logout(this, e);
		keepSessionAlive(false);
		checkForNewApplications.startChecking(false);
	}

	public void keepSessionAlive(boolean b) {
		cKeepAlive.keepAlive(b);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see android.app.Service#onBind(android.content.Intent)
	 */
	@Override
	public IBinder onBind(Intent arg0) {
		return mBinder;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see android.app.Service#onCreate()
	 */
	@Override
	public void onCreate() {
		super.onCreate();
		cKeepAlive = new KeepSessionAlive(new Executor() {

			@Override
			public void execute() {
				loggedOut();
				login();
			}
		});
		checkForNewApplications = new CheckForNewApplications(this);
		initConfig();
		Log.d("MoSeS.SERVICE", "Service Created");
	}

	public void postLoginHook(Executor e) {
		mset.postLoginHook = e;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see android.app.Service#onDestroy()
	 */
	@Override
	public void onDestroy() {
		super.onDestroy();

		Log.d("MoSeS.SERVICE", "Service Destroyed");

	}

	/*
	 * (non-Javadoc)
	 *
	 * @see android.app.Service#onStart(android.content.Intent, int)
	 */
	@Override
	public void onStart(Intent intent, int startId) {

		super.onStart(intent, startId);

		Log.d("MoSeS.SERVICE", "Service Started");
	}

	/**
	 * Reload settings.
	 */
	public void reloadSettings() {
		initConfig();
	}

}
