package moses.client.service;

import java.util.concurrent.ConcurrentLinkedQueue;

import moses.client.MosesAskForDeviceIDActivity;
import moses.client.R;
import moses.client.abstraction.HardwareAbstraction;
import moses.client.abstraction.apks.InstalledExternalApplicationsManager;
import moses.client.com.NetworkJSON;
import moses.client.service.helpers.C2DMManager;
import moses.client.service.helpers.Executor;
import moses.client.service.helpers.ExecutorWithObject;
import moses.client.service.helpers.Login;
import moses.client.service.helpers.Logout;
import moses.client.userstudy.UserstudyNotificationManager;

import org.json.JSONArray;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Binder;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;

/**
 * The Class MosesService.
 * 
 * @author Jaco Hofmann
 */
public class MosesService extends android.app.Service implements OnSharedPreferenceChangeListener {

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

		/** The username. */
		public String username = "";

		/** The password. */
		public String password = "";

		/** The sessionid. */
		public String sessionid = "";

		/** The logged in. */
		public boolean loggedIn = false;

		public boolean loggingIn = false;

		public boolean firstStart = true;

		public String deviceid = "";

		public Context activitycontext = null;

		/** Saves the used filter. */
		public JSONArray filter = new JSONArray();

		public ConcurrentLinkedQueue<Executor> postLoginSuccessPriorityHook = new ConcurrentLinkedQueue<Executor>();

		public ConcurrentLinkedQueue<Executor> postLoginSuccessHook = new ConcurrentLinkedQueue<Executor>();

		public ConcurrentLinkedQueue<Executor> postLoginFailureHook = new ConcurrentLinkedQueue<Executor>();

		public ConcurrentLinkedQueue<Executor> loginStartHook = new ConcurrentLinkedQueue<Executor>();

		public ConcurrentLinkedQueue<Executor> loginEndHook = new ConcurrentLinkedQueue<Executor>();

		public ConcurrentLinkedQueue<Executor> postLogoutHook = new ConcurrentLinkedQueue<Executor>();

		public String url = "http://www.da-sense.de/moses/test.php";

		public ConcurrentLinkedQueue<ExecutorWithObject> changeTextFieldHook = new ConcurrentLinkedQueue<ExecutorWithObject>();

		public boolean nopreferenceupdate = false;

	}

	/** The m binder. */
	private final IBinder mBinder = new LocalBinder();

	/** The settings file. */
	private SharedPreferences settingsFile;

	/** The mset. */
	private MosesSettings mset = new MosesSettings();

	private static MosesService thisInstance = null;

	public static MosesService getInstance() {
		return thisInstance;
	}

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
		mset.username = settingsFile.getString("username_pref", "");
		mset.password = settingsFile.getString("password_pref", "");
		mset.deviceid = settingsFile.getString("deviceid_pref", "");
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
		settingsFile = PreferenceManager.getDefaultSharedPreferences(this);
		settingsFile.edit().putString("sensor_data", filter.toString()).commit();
		Log.d("MoSeS.SERVICE", "Set data to: " + settingsFile.getString("sensor_data", "[]"));
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
	}

	/**
	 * Logged out.
	 */
	public void loggedOut() {
		mset.loggedIn = false;
		mset.sessionid = "";
	}

	public void registerChangeTextFieldHook(ExecutorWithObject e) {
		if (!mset.changeTextFieldHook.contains(e))
			mset.changeTextFieldHook.add(e);
	}

	public void unregisterChangeTextFieldHook(ExecutorWithObject e) {
		mset.changeTextFieldHook.remove(e);
	}

	/**
	 * Login.
	 * 
	 * @param e
	 *            the e
	 */
	public void login() {
		if (mset.username.equals("") || mset.password.equals("")) {
			for (ExecutorWithObject e : mset.changeTextFieldHook) {
				e.execute(getString(moses.client.R.string.no_username_password));
			}
			return;
		}
		if (!PreferenceManager.getDefaultSharedPreferences(this).contains("deviceid_pref")) {
			for (ExecutorWithObject e : mset.changeTextFieldHook) {
				e.execute(getString(moses.client.R.string.no_deviceid));
			}
			return;
		}
		if (!isOnline()) {
			Log.d("MoSeS.SERVICE", "Tried logging in but no internet connection was present.");
			for (ExecutorWithObject e : mset.changeTextFieldHook) {
				e.execute("No internet connection.");
			}
			return;
		}
		if (!mset.loggedIn && !mset.loggingIn) {
			Log.d("MoSeS.SERVICE", "Logging in...");
			mset.loggingIn = true;
			Login.setService(this);
			new Login(mset.username, mset.password, mset.postLoginSuccessHook, mset.postLoginSuccessPriorityHook,
					mset.postLoginFailureHook, mset.loginStartHook, mset.loginEndHook);
		}
	}

	/**
	 * Logout.
	 * 
	 * @param e
	 *            the e
	 */
	public void logout() {
		new Logout(this, mset.postLogoutHook);
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
		thisInstance = this;
		registerPostLoginFailureHook(new Executor() {

			@Override
			public void execute() {
				mset.loggingIn = false;
			}
		});

		if (InstalledExternalApplicationsManager.getInstance() == null) {
			InstalledExternalApplicationsManager.init(this);
		}
		if (UserstudyNotificationManager.getInstance() == null) {
			UserstudyNotificationManager.init(this);
		}

		if (PreferenceManager.getDefaultSharedPreferences(this).getString("c2dm_pref", "").equals(""))
			C2DMManager.requestC2DMId(MosesService.this);

		NetworkJSON.url = mset.url;
		PreferenceManager.getDefaultSharedPreferences(this).registerOnSharedPreferenceChangeListener(this);
		firstLogin();
		initConfig();

		Log.d("MoSeS.SERVICE", "Service Created");
	}

	public void startedFirstTime(Context c) {
		showWelcomeDialog(c);
	}

	public void executeLoggedIn(Executor e) {
		if (isLoggedIn())
			e.execute();
		else {
			registerPostLoginSuccessOneTimeHook(e);
			login();
		}
	}

	public void executeLoggedInPriority(Executor e) {
		if (isLoggedIn())
			e.execute();
		else {
			registerPostLoginSuccessOneTimePriorityHook(e);
			login();
		}
	}

	public void registerPostLoginSuccessHook(Executor e) {
		if (!mset.postLoginSuccessHook.contains(e))
			mset.postLoginSuccessHook.add(e);
	}

	public void registerPostLoginSuccessOneTimeHook(final Executor e) {
		Executor n = new Executor() {
			@Override
			public void execute() {
				e.execute();
				unregisterPostLoginSuccessHook(this);
			}
		};
		mset.postLoginSuccessHook.add(n);
	}

	public void registerPostLoginSuccessOneTimePriorityHook(final Executor e) {
		Executor n = new Executor() {
			@Override
			public void execute() {
				e.execute();
				unregisterPostLoginSuccessPriorityHook(this);
			}
		};
		mset.postLoginSuccessPriorityHook.add(n);
	}

	public void unregisterPostLoginSuccessHook(Executor e) {
		mset.postLoginSuccessHook.remove(e);
	}

	public void unregisterPostLoginSuccessPriorityHook(Executor e) {
		mset.postLoginSuccessPriorityHook.remove(e);
	}

	public void registerPostLoginFailureHook(Executor e) {
		if (!mset.postLoginFailureHook.contains(e))
			mset.postLoginFailureHook.add(e);
	}

	public void unregisterPostLoginFailureHook(Executor e) {
		mset.postLoginFailureHook.remove(e);
	}

	public void registerLoginStartHook(Executor e) {
		if (!mset.loginStartHook.contains(e))
			mset.loginStartHook.add(e);
	}

	public void unregisterLoginStartHook(Executor e) {
		mset.loginStartHook.remove(e);
	}

	public void registerLoginEndHook(Executor e) {
		if (!mset.loginEndHook.contains(e))
			mset.loginEndHook.add(e);
	}

	public void unregisterLoginEndHook(Executor e) {
		mset.loginEndHook.remove();
	}

	public void registerPostLogoutHook(Executor e) {
		if (!mset.postLogoutHook.contains(e))
			mset.postLogoutHook.add(e);
	}

	public void unregisterPostLogoutHook(Executor e) {
		mset.postLogoutHook.remove(e);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Service#onDestroy()
	 */
	@Override
	public void onDestroy() {
		super.onDestroy();
		thisInstance = null;
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

	/**
	 * sends device information to the moses server
	 * 
	 */
	public void syncDeviceInformation(boolean force) {
		new HardwareAbstraction(this).syncDeviceInformation(force);
	}

	public void showWelcomeDialog(final Context c) {
		AlertDialog a = new AlertDialog.Builder(c).create();
		a.setIcon(R.drawable.ic_launcher);
		a.setCancelable(false); // This blocks the 'BACK' button
		a.setMessage(getString(R.string.welcome_to_moses_string));
		a.setTitle(getString(R.string.welcome_to_moses_title_string));
		a.setButton("OK", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				Intent mainDialog = new Intent(c, MosesAskForDeviceIDActivity.class);
				c.startActivity(mainDialog);
				dialog.dismiss();
			}
		});
		a.show();
		Log.d("MoSeS.SERVICE", "First login.");
		PreferenceManager.getDefaultSharedPreferences(this).edit().putBoolean("first_start", false).commit();
	}

	public boolean isOnline() {
		ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo netInfo = cm.getActiveNetworkInfo();
		if (netInfo != null && netInfo.isConnectedOrConnecting()) {
			return true;
		}
		return false;
	}

	private void firstLogin() {
		new HardwareAbstraction(MosesService.this).getFilter();
	}

	public void uploadFilter() {
		settingsFile = PreferenceManager.getDefaultSharedPreferences(this);
		String s = settingsFile.getString("sensor_data", "[]");
		HardwareAbstraction ha = new HardwareAbstraction(this);
		ha.setFilter(s);
	}

	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
		if (!this.mset.nopreferenceupdate) {
			if (key.equals("sensor_data")) {
				Log.d("MoSeS.SERVICE", "Sensor filter changed to: " + sharedPreferences.getString("sensor_data", ""));
				uploadFilter();
			} else if (key.equals("username_pref")) {
				Log.d("MoSeS.SERVICE", "Username changed - getting new data.");
				mset.username = sharedPreferences.getString("username_pref", "");
			} else if (key.equals("password_pref")) {
				Log.d("MoSeS.SERVICE", "Username changed - getting new data.");
				mset.password = sharedPreferences.getString("password_pref", "");
			} else if (key.equals("deviceid_pref")) {
				Log.d("MoSeS.SERVICE", "Device id changed - updating it on server.");
				syncDeviceInformation(false);
			}
		}
	}

	public void setActivityContext(Context c) {
		mset.activitycontext = c;
	}

	public Context getActivityContext() {
		return mset.activitycontext;
	}

	public void noOnSharedPreferenceChanged(boolean t) {
		this.mset.nopreferenceupdate = t;
	}

	public void notSetDeviceID() {
		Intent mainDialog = new Intent(mset.activitycontext, MosesAskForDeviceIDActivity.class);
		mset.activitycontext.startActivity(mainDialog);
	}
}
