package de.da_sense.moses.client.service;

import java.util.HashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.json.JSONArray;
import org.json.JSONException;

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
import de.da_sense.moses.client.AskForDeviceIDActivity;
import de.da_sense.moses.client.R;
import de.da_sense.moses.client.abstraction.HardwareAbstraction;
import de.da_sense.moses.client.abstraction.apks.HistoryExternalApplicationsManager;
import de.da_sense.moses.client.abstraction.apks.InstalledExternalApplicationsManager;
import de.da_sense.moses.client.com.NetworkJSON;
import de.da_sense.moses.client.service.helpers.C2DMManager;
import de.da_sense.moses.client.service.helpers.EHookTypes;
import de.da_sense.moses.client.service.helpers.EMessageTypes;
import de.da_sense.moses.client.service.helpers.Executable;
import de.da_sense.moses.client.service.helpers.ExecutableForObject;
import de.da_sense.moses.client.service.helpers.ExecutableWithType;
import de.da_sense.moses.client.service.helpers.Login;
import de.da_sense.moses.client.service.helpers.Logout;
import de.da_sense.moses.client.userstudy.UserstudyNotificationManager;
import de.da_sense.moses.client.util.Log;

/**
 * The Class MosesSettings contains all information concerning communication 
 * with the server in a running system. This contains, but is not limited to
 * information regarding the current user, session and if the device is 
 * connected to the Internet.
 * Furthermore the class also functions as an controller for communication
 * with the server, enabling the use of Hooks. A hook is triple (EHookTypes h,
 * EMessageTypes t, Executable e) and functions as an communication buffer.
 * Hooks can be created while offline and will be executed once online, as well
 * as allowing the GUI to continue functioning while waiting for the server to
 * respond.
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

		/** A HashMap of EHookType => ConcurrentLinkedQueue<ExecutorWithType> */
		public HashMap<EHookTypes, ConcurrentLinkedQueue<ExecutableWithType>> hooks = new HashMap<EHookTypes, ConcurrentLinkedQueue<ExecutableWithType>>();

		/**
		 * A ConcurrentLinkedQueue consisting of Implementations of the Interface
		 * ExecutableForObject, containing an execute(Object o) Method. 
		 */
		public ConcurrentLinkedQueue<ExecutableForObject> changeTextFieldHook = new ConcurrentLinkedQueue<ExecutableForObject>();

		/** The projects url. */
		public String url = "http://www.da-sense.de/moses/api.php";

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
	 * For all ExecutorWithObject in changeTextFieldHook do e.execute(s)
	 * @param s
	 * 			The string given to executeableForObject.execute(s)
	 */
	public void executeChangeTextFieldHook(String s) {
		for (ExecutableForObject executableForObject : mset.changeTextFieldHook) {
			executableForObject.execute(s);
		}
	}

	/**
	 * Execute something as a logged in user. If the service is currently not
	 * logged in a login request will be issued first.
	 * 
	 * @param executable
	 *            The task to be executed.
	 */
	public void executeLoggedIn(EHookTypes hookType, EMessageTypes messageType, Executable executable) {
		Log.d("Moses.SERVICE", "executeLoggedIn called");
		if (isLoggedIn() && isOnline()) {
			executable.execute();
		} else {
			registerOneTimeHook(hookType, messageType, executable);
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
	public static boolean isOnline(Context context) {
		ConnectivityManager cm = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);
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
		Log.d("Moses.SERVICE", "login() called");
		Log.d("Moses.SERVICE", "mset.loggedIn = " + mset.loggedIn);
		Log.d("Moses.SERVICE", "mset.loggingIn = " + mset.loggingIn);
		Log.d("Moses.SERVICE", "mset.username = " + mset.username);
		Log.d("Moses.SERVICE", "mset.password = " + mset.password);
		Log.d("Moses.SERVICE", "deviceID exists? " + PreferenceManager.getDefaultSharedPreferences(this).contains("deviceid_pref"));
		
		if (mset.username.equals("") || mset.password.equals("")) {
			for (ExecutableForObject executableForObject : mset.changeTextFieldHook) {
				executableForObject.execute(getString(de.da_sense.moses.client.R.string.no_username_password));
			}
			return;
		}
		if (!PreferenceManager.getDefaultSharedPreferences(this).contains("deviceid_pref")) {
			for (ExecutableForObject executableForObject : mset.changeTextFieldHook) {
				executableForObject.execute(getString(de.da_sense.moses.client.R.string.no_deviceid));
			}
			return;
		}
		if (!isOnline()) {
			Log.d("MoSeS.SERVICE", "Tried logging in but no internet connection was present.");
			for (ExecutableForObject executableForObject : mset.changeTextFieldHook) {
				executableForObject.execute(getString(R.string.no_internet_connection));
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
			Intent mainDialog = new Intent(mset.activitycontext, AskForDeviceIDActivity.class);
			mset.activitycontext.startActivity(mainDialog);
		}
	}

	/*
	 * @see android.app.Service#onCreate()
	 */
	@Override
	public void onCreate() {
		super.onCreate();
		
		thisInstance = this;

		for (EHookTypes hookType : EHookTypes.values()) {
			mset.hooks.put(hookType, new ConcurrentLinkedQueue<ExecutableWithType>());
		}

		registerHook(EHookTypes.POSTLOGINFAILED, EMessageTypes.SPAMMABLE, new Executable() {
			@Override
			public void execute() {
				mset.loggingIn = false;
				loggedOut();
			}
		});

		if (HistoryExternalApplicationsManager.getInstance() == null) {
			HistoryExternalApplicationsManager.init(this);
		}
		if (InstalledExternalApplicationsManager.getInstance() == null) {
			InstalledExternalApplicationsManager.init(this);
		}
		if (UserstudyNotificationManager.getInstance() == null) {
			UserstudyNotificationManager.init(this);
		}

		if (PreferenceManager.getDefaultSharedPreferences(this).getString("c2dm_pref", "").equals(""))
			C2DMManager.requestC2DMId(MosesService.this);

		if (PreferenceManager.getDefaultSharedPreferences(this).contains("url_pref")) {
			mset.url = PreferenceManager.getDefaultSharedPreferences(this).getString("url_pref", "");
		} else {
			PreferenceManager.getDefaultSharedPreferences(this).edit().putString("url_pref", mset.url);
		}

		NetworkJSON.url = mset.url;

		PreferenceManager.getDefaultSharedPreferences(this).registerOnSharedPreferenceChangeListener(this);
		if (mset.filter == null)
			new HardwareAbstraction(MosesService.this).getFilter();
		initConfig();

		Log.d("MoSeS.SERVICE", "Service Created");
	}

	/*
	 * @see android.app.Service#onDestroy()
	 */
	@Override
	public void onDestroy() {
		super.onDestroy();
		thisInstance = null;
		Log.d("MoSeS.SERVICE", "Service Destroyed");
	}

	/*
	 * @see android.content.SharedPreferences.OnSharedPreferenceChangeListener#
	 * onSharedPreferenceChanged(android.content.SharedPreferences,
	 * java.lang.String)
	 */
	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
		Log.d("Moses.SERVICE", "onSharedPreferenceChanged called with " + key);
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
				if (sharedPreferences.getBoolean("deviceidsetsuccessfully", false)) {
					new HardwareAbstraction(this).changeDeviceID(false);
				} else {
					syncDeviceInformation(false);
				}
			}
		}
	}

	/**
	 * Get all Executables with the specified hook type.
	 * @param hookType the hook type
	 * @return all executables with hook type hookType
	 */
	public ConcurrentLinkedQueue<ExecutableWithType> getHook(EHookTypes hookType) {
		return mset.hooks.get(hookType);
	}

	/**
	 * Creates a new Hook.
	 * @param hookType EHookType The Type of Hook
	 * @param messageType EMessageType The type of message
	 * @param executable The Executable for the Hook
	 */
	public void registerHook(EHookTypes hookType, EMessageTypes messageType, Executable executable) {
		ConcurrentLinkedQueue<ExecutableWithType> hook = getHook(hookType);
		if (messageType != EMessageTypes.SPAMMABLE) {
			for (ExecutableWithType et : hook) {
				if (messageType.equals(et.t)) {
					Log.d("MoSeS.SERVICE",
							"Removed a duplicated message of type " + messageType.toString() + " from hook " + hookType.toString());
					unregisterHook(hookType, et.e);
				}
			}
		}
		hook.add(new ExecutableWithType(messageType, executable));
	}

	/**
	 * Creates a Hook(hookType, messageType, newExecutable) with a new Executable n, who upon called
	 * calls executable.execute and afterwards unregisters the Hook, e.g. the hook
	 * only works once
	 * @param hookType EHookType The Type of Hook
	 * @param messageType EMessageType The type of message
	 * @param executable Executor The Executor for the Hook
	 */
	public void registerOneTimeHook(final EHookTypes hookType, EMessageTypes messageType, final Executable executable) {
		Executable newExecuteable = new Executable() {
			@Override
			public void execute() {
				executable.execute();
				unregisterHook(hookType, this);
			}
		};
		registerHook(hookType, messageType, newExecuteable);
	}

	/**
	 * This hook is used for status updates that shall be shown to the user.
	 * 
	 * @param executableForObject
	 *            The task to be executed.
	 */
	public void registerChangeTextFieldHook(ExecutableForObject executableForObject) {
		if (!mset.changeTextFieldHook.contains(executableForObject))
			mset.changeTextFieldHook.add(executableForObject);
	}

	/**
	 * Reload settings from shared preferences.
	 */
	public void reloadSettings() {
		initConfig();
	}

	/**
	 * Sets the activityContext to context
	 * @param context The context
	 */
	public void setActivityContext(Context context) {
		mset.activitycontext = context;
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
	 * @param context
	 *            The context under which the dialog is shown.
	 */
	private void showWelcomeDialog(final Context context) {
		AlertDialog a = new AlertDialog.Builder(context).create();
		a.setIcon(R.drawable.ic_launcher);
		a.setCancelable(false); // This blocks the 'BACK' button
		a.setMessage(getString(R.string.welcome_to_moses_string));
		a.setTitle(getString(R.string.welcome_to_moses_title_string));
		a.setButton("OK", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				Intent mainDialog = new Intent(context, AskForDeviceIDActivity.class);
				mainDialog.putExtra("firststart", true);
				context.startActivity(mainDialog);
				dialog.dismiss();
			}
		});
		a.show();
		Log.d("MoSeS.SERVICE", "First login.");
		PreferenceManager.getDefaultSharedPreferences(this).edit().putBoolean("first_start", false).commit();
	}

	/**
	 * Calls another function when it's the first start of the app.
	 * @param context the context
	 */
	public void startedFirstTime(Context context) {
		showWelcomeDialog(context);
	}

	/**
	 * Sends device information to the moses server.
	 */
	public void syncDeviceInformation(boolean force) {
		executeLoggedIn(EHookTypes.POSTLOGINSUCCESSPRIORITY, EMessageTypes.REQUESTUPDATEHARDWAREPARAMETERS,
				new Executable() {
					@Override
					public void execute() {
						new HardwareAbstraction(MosesService.this).syncDeviceInformation(false);
					}
				});
	}

	/**
	 * This hook is no longer used for status updates that shall be shown to the user.
	 * 
	 * @param executableForObject the executable to unregister
	 */
	public void unregisterChangeTextFieldHook(ExecutableForObject executableForObject) {
		if (mset.changeTextFieldHook.contains(executableForObject))
			mset.changeTextFieldHook.remove(executableForObject);
	}

	/**
	 * Checks all hooks with type == hookType for the one containing the Executor executable. 
	 * If found it removes this hook.
	 * @param hookType The type of Hook to unregister
	 * @param executable The Executer to unregister
	 */
	public void unregisterHook(EHookTypes hookType, Executable executable) {
		ConcurrentLinkedQueue<ExecutableWithType> hook = getHook(hookType);
		ExecutableWithType exeCutableWithTypeCurrent = null;
		for (ExecutableWithType executableWithType : hook) {
			if (executableWithType.e.equals(executable)) {
				exeCutableWithTypeCurrent = executableWithType;
				break;
			}
		}
		if (exeCutableWithTypeCurrent != null)
			hook.remove(exeCutableWithTypeCurrent);
	}

	/**
	 * After logging in this method uploads a filter for the sensor data
	 * retrieved from the {@link PreferenceManager}.
	 */
	public void uploadFilter() {
		this.executeLoggedIn(EHookTypes.POSTLOGINSUCCESS, 
				EMessageTypes.REQUESTSETFILTER, new Executable() {
			@Override
			public void execute() {
				new HardwareAbstraction(MosesService.this)
				.setFilter(PreferenceManager.getDefaultSharedPreferences(
						MosesService.this).getString("sensor_data", "[]"));
			}
		});
	}

	/**
	 * Extends Binder, a simple dummyclass to get the currently
	 * active MosesService.
	 */
	public class LocalBinder extends Binder {
		/**
		 * Gets the currently active MosesService
		 * @return MosesService.this
		 */
		public MosesService getService() {
			return MosesService.this;
		}
	}

	/** Binder for the currently active MosesService */
	private final IBinder mBinder = new LocalBinder();

	/*
	 * @see android.app.Service#onBind(android.content.Intent)
	 */
	@Override
	public IBinder onBind(Intent arg0) {
		return mBinder;
	}
}
