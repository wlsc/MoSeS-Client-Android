package moses.client.service;

import moses.client.service.helpers.NotifyAboutNewApksActivity;
import moses.client.service.helpers.CheckForNewApplications;
import moses.client.service.helpers.Executor;
import moses.client.service.helpers.KeepSessionAlive;
import moses.client.service.helpers.Login;
import moses.client.service.helpers.Logout;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Binder;
import android.os.IBinder;
import android.widget.Toast;

// TODO: Auto-generated Javadoc
/**
 * The Class MosesService.
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
	}

	/**
	 * Checks if is logged in.
	 *
	 * @return true, if is logged in
	 */
	public boolean isLoggedIn() {
		return mset.loggedIn;
	}

	/**
	 * Logged in.
	 *
	 * @param sessionid the sessionid
	 */
	public void loggedIn(String sessionid) {
		mset.loggedIn = true;
		mset.sessionid = sessionid;
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
	 * @param e the e
	 */
	public void login(Executor e) {
		new Login(mset.username, mset.password, this, e);
		keepSessionAlive(true);
		checkForNewApplications.startChecking(true);
	}

	/**
	 * Logout.
	 *
	 * @param e the e
	 */
	public void logout(Executor e) {
		new Logout(this, e);
		keepSessionAlive(false);
	}
	
	public void keepSessionAlive(boolean b) {
		cKeepAlive.keepAlive(b);
	}

	/* (non-Javadoc)
	 * @see android.app.Service#onBind(android.content.Intent)
	 */
	@Override
	public IBinder onBind(Intent arg0) {
		return mBinder;
	}

	/* (non-Javadoc)
	 * @see android.app.Service#onCreate()
	 */
	@Override
	public void onCreate() {
		super.onCreate();
		initConfig();
		cKeepAlive = new KeepSessionAlive();
		checkForNewApplications = new CheckForNewApplications(this);
		Toast.makeText(this, "Service Created", Toast.LENGTH_LONG).show();
	}


	/* (non-Javadoc)
	 * @see android.app.Service#onDestroy()
	 */
	@Override
	public void onDestroy() {
		super.onDestroy();

		Toast.makeText(this, "Service Destroyed", Toast.LENGTH_LONG).show();

	}

	/* (non-Javadoc)
	 * @see android.app.Service#onStart(android.content.Intent, int)
	 */
	@Override
	public void onStart(Intent intent, int startId) {

		super.onStart(intent, startId);

		Toast.makeText(this, "Service Started", Toast.LENGTH_LONG).show();
	}

	/**
	 * Reload settings.
	 */
	public void reloadSettings() {
		initConfig();
	}

}
