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
import android.os.IBinder;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

// TODO: Auto-generated Javadoc
/**
 * This activity is the main activity of our app providing login etc.
 * 
 * @author Jaco
 * 
 */
public class MosesActivity extends Activity {

	public enum results {
		RS_DONE, RS_CLOSE, RS_LOGGEDOUT
	};

	/** The txt uname. */
	private EditText txtUname;

	/** The txt pw. */
	private EditText txtPW;

	/** The btnconnect. */
	private Button btnconnect;

	/** The btn exit. */
	private Button btnExit;

	/** The chk login auto. */
	private CheckBox chkLoginAuto;

	/** The chk save uname pw. */
	private CheckBox chkSaveUnamePW;

	/** The settings. */
	private SharedPreferences settings;

	/** The m service. */
	public MosesService mService;

	/** The m bound. */
	public static boolean mBound = false;

	/**
	 * This variable is true if the user has used the logout button so he won't
	 * get logged in again
	 */
	private boolean overrideAutologin = false;

	/** The m connection. */
	private ServiceConnection mConnection = new ServiceConnection() {

		@Override
		public void onServiceConnected(ComponentName className, IBinder service) {
			// We've bound to LocalService, cast the IBinder and get
			// LocalService instance
			LocalBinder binder = (LocalBinder) service;
			mService = binder.getService();
			mBound = true;
			btnconnect.setEnabled(true);
			if (mService.isLoggedIn()) {
				Intent mainDialog = new Intent(MosesActivity.this,
						LoggedInViewActivity.class);
				startActivityForResult(mainDialog, 0);
			} else if (chkLoginAuto.isChecked() && !overrideAutologin)
				connect();
		}

		@Override
		public void onServiceDisconnected(ComponentName arg0) {
			mBound = false;
		}
	};

	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if(!isMosesServiceRunning()) startAndBindService();
		if (requestCode == 0) {
			if (resultCode == results.RS_CLOSE.ordinal()) {
				// User wants to close the app without logging out
				finish();
			} else if(resultCode == results.RS_LOGGEDOUT.ordinal()) {
				overrideAutologin = true;
			}
		}
	}

	/**
	 * Connect.
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
		mService.login(new Executor() {

			@Override
			public void execute() {
				Intent mainDialog = new Intent(MosesActivity.this,
						LoggedInViewActivity.class);
				startActivityForResult(mainDialog, 0);
			}

		});
	}

	/**
	 * Inits the controls.
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
	 * Checks if is moses service running.
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
	 * Load config.
	 */
	private void loadConfig() {
		settings = getSharedPreferences("MoSeS.cfg", 0);
		txtUname.setText(settings.getString("uname", ""));
		txtPW.setText(settings.getString("password", ""));
		NetworkJSON.url = settings.getString("url",
				"http://212.72.183.71:80/moses/test.php");
		chkLoginAuto.setChecked(settings.getBoolean("loginauto", false));
		chkSaveUnamePW.setChecked(settings.getBoolean("saveunamepw", false));
		
		try {
			InstalledExternalApplicationsManager.init(getApplicationContext());
		} catch (IOException e) {
			Toast.makeText(getApplicationContext(), "Could not load installed applications", Toast.LENGTH_LONG);
		}
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
		loadConfig();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onStart()
	 */
	@Override
	protected void onStart() {
		super.onStart();
		startAndBindService();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onStop()
	 */
	@Override
	protected void onStop() {
		super.onStop();
		if (mBound) {
			unbindService(mConnection);
		}
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