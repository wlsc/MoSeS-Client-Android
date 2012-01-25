package moses.client;

import java.io.IOException;

import moses.client.abstraction.apks.InstalledExternalApplicationsManager;
import moses.client.com.NetworkJSON;
import moses.client.service.MosesService;
import moses.client.service.MosesService.LocalBinder;
import moses.client.service.helpers.Executor;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;


/**
 * This activity shows a login field to the user.
 * 
 * It's the first activity a user sees who starts our app.
 * 
 * @author Jaco
 * 
 */
public class MosesActivity extends Activity {

	public enum results {
		RS_DONE, RS_CLOSE, RS_LOGGEDOUT
	};

	/** Shows the currently typed in username **/
	private EditText txtUname;

	/** Shows the currently typed in password **/
	private EditText txtPW;

	/** A click on this button connects the user to our server **/
	private Button btnconnect;

	/** Closes the app **/
	private Button btnExit;

	/**
	 * If this box is checked the application doesn't wait for user input to
	 * connect to our server
	 **/
	private CheckBox chkLoginAuto;

	/** If this box is checked username and password are saved on the device. **/
	private CheckBox chkSaveUnamePW;

	/** All our settings are handled and saved to filesystem over this object **/
	private SharedPreferences settings;

	/** This Object represents the underlying service. **/
	public MosesService mService;

	/** If this variable is true the activity is connected to the service. **/
	public static boolean mBound = false;

	/**
	 * This variable is true if the activity is accessed with a return value and
	 * the app isn't intended to log back in. E.g. after a logout has been
	 * performed.
	 */
	private boolean overrideAutologin = false;

	/** This object handles connection and disconnection of the service **/
	private ServiceConnection mConnection = new ServiceConnection() {

		@Override
		public void onServiceConnected(ComponentName className, IBinder service) {
			// We've bound to LocalService, cast the IBinder and get
			// LocalService instance
			LocalBinder binder = (LocalBinder) service;
			mService = binder.getService();
			mBound = true;
			btnconnect.setEnabled(true);
			loadConfig();

			// We want, that the logged in view is shown to the user after a
			// login has been successful.
			mService.postLoginHook(new Executor() {

				@Override
				public void execute() {
					Intent mainDialog = new Intent(MosesActivity.this,
							LoggedInViewActivity.class);
					startActivityForResult(mainDialog, 0);
				}
			});
			// If we're already logged in or the user wants auto login start
			// logged in view
			if (chkLoginAuto.isChecked() && !overrideAutologin)
				connect();
			else if (mService.isLoggedIn() && !overrideAutologin) {
				Intent mainDialog = new Intent(MosesActivity.this,
						LoggedInViewActivity.class);
				startActivityForResult(mainDialog, 0);
			}
		}

		@Override
		public void onServiceDisconnected(ComponentName arg0) {
			mBound = false;
		}
	};

	private Handler mHandler = new Handler();

	private boolean stopPosting;

	private Runnable mIsServiceAliveTask = new Runnable() {

		@Override
		public void run() {
			if (!isMosesServiceRunning())
				startAndBindService();
			if (!stopPosting)
				mHandler.postDelayed(this, 1000);
		}

	};

	/**
	 * Connect to the server and save (changed) settings
	 */
	private void connect() {
		SharedPreferences.Editor editor = settings.edit();
		if (chkSaveUnamePW.isChecked()) {
			editor.putString("uname", txtUname.getText().toString());
			editor.putString("password", txtPW.getText().toString());
		}
		editor.putBoolean("loginauto", chkLoginAuto.isChecked());
		editor.putBoolean("saveunamepw", chkSaveUnamePW.isChecked());
		editor.commit();
		mService.reloadSettings();
		// Login if not already or just start logged in view if present
		if (!mService.isLoggedIn()) {
			mService.login();
		} else {
			stopPosting = true;
			Intent mainDialog = new Intent(MosesActivity.this,
					LoggedInViewActivity.class);
			startActivityForResult(mainDialog, 0);
		}
	}

	/**
	 * Disconnect from the service if it is connected and stop logged in check
	 */
	private void disconnectService() {
		stopPosting = true;
		mHandler.removeCallbacks(mIsServiceAliveTask);
		if (mBound) {
			mService.postLoginHook(null);
			unbindService(mConnection);
		}
	}

	/**
	 * Initialise controls.
	 */
	private void initControls() {
		txtUname = (EditText) findViewById(R.id.uname);
		txtPW = (EditText) findViewById(R.id.pword);

		chkLoginAuto = (CheckBox) findViewById(R.id.loginauto);
		chkSaveUnamePW = (CheckBox) findViewById(R.id.saveunamepw);

		btnconnect = (Button) findViewById(R.id.connect_button);
		btnconnect.setEnabled(false);
		btnconnect.setOnClickListener(new Button.OnClickListener() {
			@Override
			public void onClick(View v) {
				connect();
				/*Intent mainDialog = new Intent(MosesActivity.this,
						MosesPreferences.class);
				startActivityForResult(mainDialog, 0);*/
			}
		});

		btnExit = (Button) findViewById(R.id.exitbutton);
		btnExit.setOnClickListener(new Button.OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
			}
		});
	}

	/**
	 * Checks if is MoSeS service running.
	 * 
	 * @return true, if is moses service running
	 */
	private boolean isMosesServiceRunning() {
		ActivityManager manager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
		for (RunningServiceInfo service : manager
				.getRunningServices(Integer.MAX_VALUE)) {
			if ("moses.client.service.MosesService".equals(service.service
					.getClassName())) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Load configuration from file MoSeS.cfg using SharedPreferences
	 */
	private void loadConfig() {
		settings = PreferenceManager.getDefaultSharedPreferences(mService.getServiceContext());
		txtUname.setText(settings.getString("uname", ""));
		txtPW.setText(settings.getString("password", ""));
		NetworkJSON.url = settings.getString("url",
				"http://212.72.183.71:80/moses/test.php");
		chkLoginAuto.setChecked(settings.getBoolean("loginauto", false));
		chkSaveUnamePW.setChecked(settings.getBoolean("saveunamepw", false));

		try {
			InstalledExternalApplicationsManager.init(getApplicationContext());
		} catch (IOException e) {
			Log.d("MoSeS.LOGIN_ACTIVITY",
					"Could not load installed applications");
		}
	}

	/**
	 * User comes back from another activity
	 */
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (!isMosesServiceRunning())
			startAndBindService();
		if (requestCode == 0) {
			if (resultCode == results.RS_CLOSE.ordinal()) {
				// User wants to close the app without logging out
				finish();
			} else if (resultCode == results.RS_LOGGEDOUT.ordinal()) {
				overrideAutologin = true;
			}
		}
	}

	/**
	 * We're back, so get everything going again.
	 */
	public void onAttachedToWindow() {
		super.onAttachedToWindow();
		startAndBindService();
		mHandler.removeCallbacks(mIsServiceAliveTask);
		mHandler.postDelayed(mIsServiceAliveTask, 1000);
	}

	/**
	 * Called when the activity is first created.
	 * 
	 * @param savedInstanceState
	 *            the saved instance state
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		initControls();
	}

	/**
	 * When first started this activity stars a Task that keeps the connection
	 * with the service alive and restarts it if necessary.
	 */
	@Override
	protected void onStart() {
		super.onStart();
		stopPosting = false;
		mHandler.removeCallbacks(mIsServiceAliveTask);
		mHandler.postDelayed(mIsServiceAliveTask, 1000);
		startAndBindService();
	}

	/**
	 * Disconnect service so android won't get angry.
	 */
	@Override
	protected void onStop() {
		super.onStop();
		disconnectService();
	}

	/**
	 * Start and bind service.
	 */
	private void startAndBindService() {
		Intent intent = new Intent(this, MosesService.class);
		if (!isMosesServiceRunning()) {
			startService(intent);
		}
		bindService(intent, mConnection, 0);
	}

}