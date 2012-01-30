package moses.client;

import java.io.IOException;

import moses.client.abstraction.APKAbstraction;
import moses.client.abstraction.HardwareAbstraction;
import moses.client.abstraction.PingSender;
import moses.client.abstraction.apks.InstalledExternalApplicationsManager;
import moses.client.service.MosesService;
import moses.client.service.MosesService.LocalBinder;
import moses.client.service.helpers.CheckForNewApplications;
import moses.client.service.helpers.Executor;
import moses.client.service.helpers.NotifyAboutNewApksActivity;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.IBinder;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

// TODO: Auto-generated Javadoc
/**
 * 
 * DEPRECATED!!!!!
 * 
 * This activity resembles the view after logging in.
 * 
 * @author Jaco
 */
public class LoggedInViewActivity extends Activity {

	private static String YOURAPP_NOTIFICATION_ID = "new_installed_apk_notification_id";
	
	/** The btn logout. */
	private Button btnLogout;

	/** The btn sync hw. */
	private Button btnSyncHW;

	/** The btn get hw. */
	private Button btnGetHW; // used for getting hw configuration stored on the
								// server

	/** The btn select filter. */
	private Button btnSelectFilter;

	/** The btn ping. */
	private Button btnPing; // used for sending "i am alive" messages

	/** The btn list apk. */
	private Button btnListAPK; // used for obtaining the list of APKs

	/** The apk abstraction. */
	private APKAbstraction apkAbstraction;

	/** The settings. */
	private SharedPreferences settings;

	/** The m service. */
	public MosesService mService;

	/** The m bound. */
	public static boolean mBound = false;

	/** The m connection. */
	private ServiceConnection mConnection = new ServiceConnection() {

		@Override
		public void onServiceConnected(ComponentName className, IBinder service) {
			// We've bound to LocalService, cast the IBinder and get
			// LocalService instance
			LocalBinder binder = (LocalBinder) service;
			mService = binder.getService();
			mBound = true;
		}

		@Override
		public void onServiceDisconnected(ComponentName arg0) {
			mBound = false;
		}
	};

	private Button btnShowAvailableApks;

	private Button btnShowInstalledApps;

	private Button btnResetInstalledApps;

	/**
	 * Bind service.
	 */
	private void bindService() {
		Intent intent = new Intent(this, MosesService.class);
		bindService(intent, mConnection, 0);
	}

	/**
	 * This method is called when getHW-Button is pushed.
	 * 
	 * @return the hardware parameters
	 */
	private void getHardwareParameters() {
		HardwareAbstraction hw = new HardwareAbstraction(this);
		hw.getHardwareParameters();
	}

	/**
	 * Inits the controls.
	 */
	private void initControls() {

		btnLogout = (Button) findViewById(R.id.logout_button);
		btnLogout.setOnClickListener(new Button.OnClickListener() {
			@Override
			public void onClick(View v) {
				mService.logout();
			}
		});

		btnSyncHW = (Button) findViewById(R.id.synchw);
		btnSyncHW.setOnClickListener(new Button.OnClickListener() {
			@Override
			public void onClick(View v) {
				//checkHardware(true);
			}
		});

		btnSelectFilter = (Button) findViewById(R.id.selectfilter);
		btnSelectFilter.setOnClickListener(new Button.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent chooseSensors = new Intent(LoggedInViewActivity.this, ChooseSensorsActivity.class);
				startActivity(chooseSensors);
			}
		});

		btnGetHW = (Button) findViewById(R.id.gethw_button);
		btnGetHW.setOnClickListener(new Button.OnClickListener() {
			@Override
			public void onClick(View v) {
				getHardwareParameters();
			}
		});

		/*
		 * Implementing the functionality of the ping button
		 */
		btnPing = (Button) findViewById(R.id.ping_button);
		btnPing.setOnClickListener(new Button.OnClickListener() {
			@Override
			public void onClick(View v) {
				// pinger.sendPing();
				setResult(MosesActivity.results.RS_CLOSE.ordinal());
				finish();
			}
		});

		/*
		 * Implementing the functionality of the getListAPK button
		 */
		btnListAPK = (Button) findViewById(R.id.listAPK_button);
		btnListAPK.setOnClickListener(new Button.OnClickListener() {
			@Override
			public void onClick(View v) {
				apkAbstraction.getAPKs();
			}
		});

		btnShowAvailableApks = (Button) findViewById(R.id.btnShowSensingApplications);
		btnShowAvailableApks.setOnClickListener(new Button.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent showAvailableApkList = new Intent(LoggedInViewActivity.this, ViewAvailableApkActivity.class);
				startActivity(showAvailableApkList);

			}
		});

		btnShowInstalledApps = (Button) findViewById(R.id.btnShowInstalledApplications);
		btnShowInstalledApps.setOnClickListener(new Button.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent showInstalledAppsList = new Intent(LoggedInViewActivity.this,
					ViewInstalledApplicationsActivity.class);
				startActivity(showInstalledAppsList);

			}
		});
		btnResetInstalledApps = (Button) findViewById(R.id.btnResetInstalledApplications);
		btnResetInstalledApps.setOnClickListener(new Button.OnClickListener() {
			@Override
			public void onClick(View v) {
				InstalledExternalApplicationsManager.getDefault().reset();
				try {
					InstalledExternalApplicationsManager.getDefault().saveToDisk(getApplicationContext());
				} catch (IOException e) {
					e.printStackTrace();
				}
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
		for (RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
			if ("moses.client.service.MosesService".equals(service.service.getClassName())) { return true; }
		}
		return false;
	}
	
	public void showNotificationHandler(View v) {
//		showNotification("New sensing applications are available!\nClick here to view all applications", "MoSeS", false, 123);
		Intent intent = new Intent(this, NotifyAboutNewApksActivity.class);
		this.startActivity(intent);
	}
	
	

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.loggedinview);
		settings = getSharedPreferences("MoSeS.cfg", 0);
		initControls();
		apkAbstraction = new APKAbstraction(this);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onStart()
	 */
	@Override
	protected void onStart() {
		super.onStart();
		bindService();
//		Toast.makeText(LoggedInViewActivity.this, "" + isMosesServiceRunning(), Toast.LENGTH_LONG).show();
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
}
