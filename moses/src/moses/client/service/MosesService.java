package moses.client.service;

import java.io.FileDescriptor;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

import moses.client.MosesAskForDeviceIDActivity;
import moses.client.R;
import moses.client.abstraction.HardwareAbstraction;
import moses.client.abstraction.apks.InstalledExternalApplicationsManager;
import moses.client.com.NetworkJSON;
import moses.client.service.helpers.C2DMManager;
import moses.client.service.helpers.EHookTypes;
import moses.client.service.helpers.EMessageTypes;
import moses.client.service.helpers.Executor;
import moses.client.service.helpers.ExecutorWithObject;
import moses.client.service.helpers.ExecutorWithType;
import moses.client.service.helpers.Login;
import moses.client.service.helpers.Logout;
import moses.client.userstudy.UserstudyNotificationManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

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
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import android.preference.PreferenceManager;
import android.util.Log;

/**
 * The Class MosesService.
 * 
 * @author Jaco Hofmann
 */
public class MosesService extends android.app.Service implements OnSharedPreferenceChangeListener {

	/**
	 * The Class MosesSettings.
	 */
	public class MosesSettings {

		/** This variable holds the username during runtime. */
		public String username = "";

		/** This variable holds the password during runtime */
		public String password = "";

		/** The current session id. */
		public String sessionid = "";

		/** This variable is true if the service is currently logged in. */
		public boolean loggedIn = false;

		/** True if the service currently tries to log in. */
		public boolean loggingIn = false;

		/** The device id currently in use. */
		public String deviceid = "";

		/** The context of the currently used activity. */
		public Context activitycontext = null;

		/** Saves the used filter. */
		public JSONArray filter = new JSONArray();

		public HashMap<EHookTypes, ConcurrentLinkedQueue<ExecutorWithType>> hooks = new HashMap<EHookTypes, ConcurrentLinkedQueue<ExecutorWithType>>();

		public ConcurrentLinkedQueue<ExecutorWithObject> changeTextFieldHook = new ConcurrentLinkedQueue<ExecutorWithObject>();

		/** The projects url. */
		public String url = "http://www.da-sense.de/moses/test.php";

		/**
		 * True if an update from server is not required after some shared
		 * preference has changed.
		 */
		public boolean nopreferenceupdate = false;

	}

	/** Local settings. */
	private MosesSettings mset = new MosesSettings();

	/** The current instance is saved in here. */
	private static MosesService thisInstance = null;

	/** Returns the current instance (singleton) */
	public static MosesService getInstance() {
		return thisInstance;
	}

	/**
	 * Execute something as a logged in user. If the service is currently not
	 * logged in a login request will be issued first.
	 * 
	 * @param e
	 *            The task to be executed.
	 */
	public void executeLoggedIn(EHookTypes h, EMessageTypes t, Executor e) {
		if (isLoggedIn() && isOnline())
			e.execute();
		else {
			registerOneTimeHook(h, t, e);
			login();
		}
	}

	/** Returns the currently selected activity context. */
	public Context getActivityContext() {
		return mset.activitycontext;
	}

	/**
	 * Returns the current session id.
	 * 
	 * @return The current session id.
	 */
	public String getSessionID() {
		return mset.sessionid;
	}

	/**
	 * Reads the configuration file into the local settings.
	 * 
	 * @throws JSONException
	 */
	private void initConfig() {
		SharedPreferences settingsFile = PreferenceManager.getDefaultSharedPreferences(this);
		mset.username = settingsFile.getString("username_pref", "");
		mset.password = settingsFile.getString("password_pref", "");
		mset.deviceid = settingsFile.getString("deviceid_pref", "");
		try {
			mset.filter = new JSONArray(settingsFile.getString("sensor_data", "[]"));
		} catch (JSONException e) {
			Log.d("MoSeS.SERVICE", "Filter not a valid JSONArray.");
		}
	}

	/**
	 * Checks if the service is logged in.
	 * 
	 * @return true, if the service is logged in.
	 */
	public boolean isLoggedIn() {
		return mset.loggedIn;
	}

	/**
	 * Checks if the service is logging in.
	 * 
	 * @return true, if the service is logging in.
	 */
	public boolean isLoggingIn() {
		return mset.loggingIn;
	}

	/**
	 * Checks if the device is connected to the Internet.
	 * 
	 * @return true, if the device is online.
	 */
	public boolean isOnline() {
		return isOnline(this);
	}
	
	/**
	 * Checks if the device is connected to the Internet.
	 * 
	 * @return true, if the device is online.
	 */
	public static boolean isOnline(Context c) {
		ConnectivityManager cm = (ConnectivityManager) c.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo netInfo = cm.getActiveNetworkInfo();
		if (netInfo != null && netInfo.isConnectedOrConnecting()) {
			return true;
		}
		return false;
	}

	/**
	 * This function is to be called after the service is logged in.
	 * 
	 * @param sessionid
	 *            The new session id.
	 */
	public void loggedIn(String sessionid) {
		mset.loggedIn = true;
		mset.loggingIn = false;
		mset.sessionid = sessionid;
	}

	/**
	 * This function is to be called after the service has been logged out.
	 */
	public void loggedOut() {
		Login.removeLogoutTask();
		mset.loggedIn = false;
		mset.loggingIn = false;
		mset.sessionid = "";
	}

	/**
	 * Login to the MoSeS server.
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
			loggedOut();
			return;
		}
		if (!mset.loggedIn && !mset.loggingIn) {
			Log.d("MoSeS.SERVICE", "Logging in...");
			mset.loggingIn = true;
			new Login(mset.username, mset.password);
		}
	}

	/**
	 * Call this function if you want to log out from MoSeS.
	 */
	public void logout() {
		new Logout(this, getHook(EHookTypes.POSTLOGOUT));
	}

	/**
	 * Enable/disable the response to preference changes.
	 * 
	 * @param t
	 *            true, if you want no reaction on preference changes.
	 */
	public void noOnSharedPreferenceChanged(boolean t) {
		this.mset.nopreferenceupdate = t;
	}

	/**
	 * Show a device id selection screen if no device id is set.
	 */
	public void notSetDeviceID() {
		if (mset.activitycontext != null) {
			Intent mainDialog = new Intent(mset.activitycontext, MosesAskForDeviceIDActivity.class);
			mset.activitycontext.startActivity(mainDialog);
		}
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

		for(EHookTypes h : EHookTypes.values()) {
			mset.hooks.put(h, new ConcurrentLinkedQueue<ExecutorWithType>());
		}

		registerHook(EHookTypes.POSTLOGINFAILED,EMessageTypes.SPAMMABLE,new Executor() {
			@Override
			public void execute() {
				mset.loggingIn = false;
				loggedOut();
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
		if (mset.filter == null)
			new HardwareAbstraction(MosesService.this).getFilter();
		initConfig();

		Log.d("MoSeS.SERVICE", "Service Created");
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
	 * @see android.content.SharedPreferences.OnSharedPreferenceChangeListener#
	 * onSharedPreferenceChanged(android.content.SharedPreferences,
	 * java.lang.String)
	 */
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
				if(sharedPreferences.getBoolean("deviceidsetsuccessfully", false)) {
					new HardwareAbstraction(this).changeDeviceID(false);
				} else {
					syncDeviceInformation(false);
				}
			}
		}
	}

	public ConcurrentLinkedQueue<ExecutorWithType> getHook(EHookTypes h) {
		return mset.hooks.get(h);
	}

	public void registerHook(EHookTypes h, EMessageTypes t, Executor e) {
		ConcurrentLinkedQueue<ExecutorWithType> hook = getHook(h);
		if (t != EMessageTypes.SPAMMABLE) {
			for (ExecutorWithType et : hook) {
				if (t.equals(et.t)) {
					Log.d("MoSeS.SERVICE", "Removed a duplicated message of type " + t.toString() + " from hook " + h.toString());
					unregisterHook(h, et.e);
				}
			}
		}
		hook.add(new ExecutorWithType(t, e));
	}

	public void registerOneTimeHook(final EHookTypes h, EMessageTypes t, final Executor e) {
		Executor n = new Executor() {
			@Override
			public void execute() {
				e.execute();
				unregisterHook(h, this);
			}
		};
		registerHook(h, t, n);
	}

	/**
	 * This hook is used for status updates that shall be shown to the user.
	 *
	 * @param e
	 *            The task to be executed.
	 */
	public void registerChangeTextFieldHook(ExecutorWithObject e) {
		if (!mset.changeTextFieldHook.contains(e))
			mset.changeTextFieldHook.add(e);
	}

	/**
	 * Reload settings from shared preferences.
	 */
	public void reloadSettings() {
		initConfig();
	}

	public void setActivityContext(Context c) {
		mset.activitycontext = c;
	}

	/**
	 * Set the filter in the program and in the shared preferences.
	 * 
	 * @param filter
	 */
	public void setFilter(JSONArray filter) {
		mset.filter = filter;
		SharedPreferences settingsFile = PreferenceManager.getDefaultSharedPreferences(this);
		settingsFile.edit().putString("sensor_data", filter.toString()).commit();
		Log.d("MoSeS.SERVICE", "Set filter to: " + settingsFile.getString("sensor_data", "[]"));
	}

	/**
	 * This function will be executed on first run and shows some welcome
	 * dialog.
	 * 
	 * @param c
	 *            The context under which the dialog is shown.
	 */
	private void showWelcomeDialog(final Context c) {
		AlertDialog a = new AlertDialog.Builder(c).create();
		a.setIcon(R.drawable.ic_launcher);
		a.setCancelable(false); // This blocks the 'BACK' button
		a.setMessage(getString(R.string.welcome_to_moses_string));
		a.setTitle(getString(R.string.welcome_to_moses_title_string));
		a.setButton("OK", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				Intent mainDialog = new Intent(c, MosesAskForDeviceIDActivity.class);
				mainDialog.putExtra("firststart", true);
				c.startActivity(mainDialog);
				dialog.dismiss();
			}
		});
		a.show();
		Log.d("MoSeS.SERVICE", "First login.");
		PreferenceManager.getDefaultSharedPreferences(this).edit().putBoolean("first_start", false).commit();
	}

	public void startedFirstTime(Context c) {
		showWelcomeDialog(c);
	}

	/**
	 * sends device information to the moses server
	 * 
	 */
	public void syncDeviceInformation(boolean force) {
		new HardwareAbstraction(this).syncDeviceInformation(force);
	}

	public void unregisterChangeTextFieldHook(ExecutorWithObject e) {
		if (mset.changeTextFieldHook.contains(e))
			mset.changeTextFieldHook.remove(e);
	}

	public void unregisterHook(EHookTypes h, Executor e) {
		ConcurrentLinkedQueue<ExecutorWithType> hook = getHook(h);
		ExecutorWithType n = null;
		for (ExecutorWithType t : hook) {
			if (t.e.equals(e)) {
				n = t;
				break;
			}
		}
		if (n != null)
			hook.remove(n);
	}

	public void uploadFilter() {
		SharedPreferences settingsFile = PreferenceManager.getDefaultSharedPreferences(this);
		String s = settingsFile.getString("sensor_data", "[]");
		HardwareAbstraction ha = new HardwareAbstraction(this);
		ha.setFilter(s);
	}

	public class LocalBinder extends Binder {
		public MosesService getService() {
			return MosesService.this;
		}
	}

	private final IBinder mBinder = new LocalBinder();

	@Override
	public IBinder onBind(Intent arg0) {
		return mBinder;
	}
}
