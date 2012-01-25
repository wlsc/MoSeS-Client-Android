package moses.client.service;

import org.json.JSONArray;

import moses.client.abstraction.HardwareAbstraction;
import moses.client.com.C2DMReceiver;
import moses.client.com.ReqTaskExecutor;
import moses.client.com.NetworkJSON.BackgroundException;
import moses.client.com.requests.RequestC2DM;
import moses.client.com.NetworkJSON;
import moses.client.service.helpers.CheckForNewApplications;
import moses.client.service.helpers.Executor;
import moses.client.service.helpers.KeepSessionAlive;
import moses.client.service.helpers.Login;
import moses.client.service.helpers.Logout;
import moses.client.userstudy.UserStudyNotification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Binder;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

/**
 * The Class MosesService.
 * 
 * @author Jaco Hofmann
 */
public class MosesService extends android.app.Service implements
		OnSharedPreferenceChangeListener {

	private static final String MOSES_TUD_GOOGLEMAIL_COM = "moses.tud@googlemail.com";

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
		
		public String url = "http://www.da-sense.de/moses/test.php";

	}

	/** The m binder. */
	private final IBinder mBinder = new LocalBinder();

	/** The settings file. */
	private SharedPreferences settingsFile;

	/** The mset. */
	private MosesSettings mset = new MosesSettings();

	private KeepSessionAlive cKeepAlive;

	private CheckForNewApplications checkForNewApplications;

	private String c2dmRegistrationId;

	private boolean alreadySuccessfullySentC2DMID;

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
		settingsFile = PreferenceManager.getDefaultSharedPreferences(this);
		mset.loginauto = settingsFile.getBoolean("autologin_pref", false);
		mset.username = settingsFile.getString("username_pref", "");
		mset.password = settingsFile.getString("password_pref", "");
		if (mset.loginauto)
			login();
	}

	public boolean isAutoLogin() {
		return mset.loginauto;
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

	public Context getServiceContext() {
		return this;
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
		syncDeviceInformation();
		new HardwareAbstraction(this).getFilter();
		keepSessionAlive(true);
		checkForNewApplications.startChecking(true);
		if(! alreadySuccessfullySentC2DMID && this.c2dmRegistrationId != null && !(getSessionID().equals("")||getSessionID()==null)) {
			sendC2DMIdToServer(this.c2dmRegistrationId);
		}
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
		if (isOnline()) {
			if (!mset.loggedIn && !mset.loggingIn) {
				Log.d("MoSeS.SERVICE", "Logging in...");
				mset.loggingIn = true;
				new Login(mset.username, mset.password, this, new Executor() {
					@Override
					public void execute() {
						mset.postLoginHook.execute();
						if (cKeepAlive.isPingTimeShortened()) {
							cKeepAlive.shortenPingTime(false);
						}
					}
				});
			}
		} else {
			Log.d("MoSeS.SERVICE",
					"Tried logging in but no internet connection was present.");
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
				cKeepAlive.shortenPingTime(true);
				login();
			}
		});
		NetworkJSON.url = mset.url;
		PreferenceManager.getDefaultSharedPreferences(this)
				.registerOnSharedPreferenceChangeListener(this);
		checkForNewApplications = new CheckForNewApplications(this);
		registerC2DM();
		initConfig();
		Log.d("MoSeS.SERVICE", "Service Created");
	}

	public void postLoginHook(Executor e) {
		mset.postLoginHook = e;
	}

	private void registerC2DM() {
		Intent registrationIntent = new Intent(
				"com.google.android.c2dm.intent.REGISTER");
		registrationIntent.putExtra("app",
				PendingIntent.getBroadcast(this, 0, new Intent(), 0)); // boilerplate
		registrationIntent.putExtra("sender", MOSES_TUD_GOOGLEMAIL_COM);
		startService(registrationIntent);
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

	public void setC2DMReceiverId(String registrationId) {
		// TODO: if the c2dm id changed, resend?
		boolean setNewC2DMID = false;
		if (registrationId != null) {
			if (this.c2dmRegistrationId == null) {
				setNewC2DMID = true;
			} else {
				if (!registrationId.equals(this.c2dmRegistrationId)) {
					setNewC2DMID = true;
				}
			}
		} else {
			// TODO: unexpected error.. registrationId == null (should never
			// occur thou)
		}
		if (setNewC2DMID) {
			this.c2dmRegistrationId = registrationId;
			if(! alreadySuccessfullySentC2DMID && !(getSessionID().equals("")||getSessionID()==null)) {
				sendC2DMIdToServer(registrationId);
			}
				
		}
	}

	//TODO: make very sure that the id is really sent to the server!
	//TODO: what if session id is yet unknown?
	private void sendC2DMIdToServer(String registrationId) {
		RequestC2DM request = new RequestC2DM(new ReqTaskExecutor() {
			@Override
			public void updateExecution(BackgroundException c) {
			}
			@Override
			public void postExecution(String s) {
				//request sent!
				alreadySuccessfullySentC2DMID = true;
			}
			@Override
			public void handleException(Exception e) {
				//TODO: make very sure that the id is really sent to the server!
				Toast.makeText(getApplicationContext(), "sendC2DM failed: "+e.getMessage(), Toast.LENGTH_LONG).show();
			}
		}, getSessionID(), HardwareAbstraction.extractDeviceId(), registrationId);
		
		request.send();
		Toast.makeText(getApplicationContext(), "C2DM ID sent!", Toast.LENGTH_LONG).show();
	}

	/**
	 * sends device information to the moses server
	 * 
	 * @param c2dmRegistrationId
	 *            the id which identifies this device in the c2dm
	 * @param force
	 *            the Force is a binding, metaphysical and ubiquitous power in
	 *            the fictional universe of the Star Wars galaxy created by
	 *            George Lucas.
	 */
	private void syncDeviceInformation() {
		HardwareAbstraction hw = new HardwareAbstraction(this);
		hw.syncDeviceInformation(getSessionID());
	}

	public boolean isOnline() {
		ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo netInfo = cm.getActiveNetworkInfo();
		if (netInfo != null && netInfo.isConnectedOrConnecting()) {
			return true;
		}
		return false;
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		if (intent != null) {
			String c2dmIdExtra = intent
					.getStringExtra(C2DMReceiver.EXTRAFIELD_C2DM_ID);
			if (c2dmIdExtra != null) {
				Toast.makeText(getApplicationContext(),
						"C2DM-ID: " + c2dmIdExtra, Toast.LENGTH_LONG).show();
				setC2DMReceiverId(c2dmIdExtra);
			}
		}
		return super.onStartCommand(intent, flags, startId);
	}

	private void showUserStudyNotification(UserStudyNotification notification) {

	}

	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
			String key) {
		Log.d("MoSeS.SERVICE", "Settings changed - reloading them");
		reloadSettings();
	}
}
