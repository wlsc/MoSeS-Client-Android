package moses.client;

import moses.client.service.MosesService;
import moses.client.service.MosesService.LocalBinder;
import moses.client.service.helpers.Executor;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

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

	/**
	 * Login hooks
	 */
	Executor postLoginSuccessHook = new Executor() {
		@Override
		public void execute() {
			Log.d("MoSeS.ACTIVITY", "PostLoginSuccessHook");
			((TextView) findViewById(R.id.success)).setText("Online");
		}
	};

	Executor postLoginFailureHook = new Executor() {
		@Override
		public void execute() {
			Log.d("MoSeS.ACTIVITY", "PostLoginFailureHook");
			((TextView) findViewById(R.id.success))
					.setText("Error while logging in.");
		}
	};

	Executor loginStartHook = new Executor() {
		@Override
		public void execute() {
			Log.d("MoSeS.ACTIVITY", "LoginStartHook");
			((ProgressBar) findViewById(R.id.main_spinning_progress_bar))
					.setVisibility(View.VISIBLE);
			((TextView) findViewById(R.id.success)).setText("Connecting");
		}
	};

	Executor loginEndHook = new Executor() {
		@Override
		public void execute() {
			Log.d("MoSeS.ACTIVITY", "LoginEndHook");
			((ProgressBar) findViewById(R.id.main_spinning_progress_bar))
					.setVisibility(View.GONE);
			((TextView) findViewById(R.id.success)).setText("Connected");
		}
	};

	Executor postLogoutHook = new Executor() {

		@Override
		public void execute() {
			Log.d("MoSeS.ACTIVITY", "postLogoutHook");
			((TextView) findViewById(R.id.success)).setText("Offline");
		}
	};

	/** This Object represents the underlying service. **/
	public MosesService mService;

	/** If this variable is true the activity is connected to the service. **/
	public static boolean mBound = false;

	/** This object handles connection and disconnection of the service **/
	private ServiceConnection mConnection = new ServiceConnection() {

		@Override
		public void onServiceConnected(ComponentName className, IBinder service) {
			// We've bound to LocalService, cast the IBinder and get
			// LocalService instance
			LocalBinder binder = (LocalBinder) service;
			mService = binder.getService();
			mBound = true;

			// Add hooks
			mService.registerPostLoginSuccessHook(postLoginSuccessHook);

			mService.registerPostLoginFailureHook(postLoginFailureHook);

			mService.registerLoginStartHook(loginStartHook);

			mService.registerLoginEndHook(loginEndHook);

			mService.registerPostLogoutHook(postLogoutHook);

			// If we're already logged in or the user wants auto login start
			// logged in view
			if (mService.isAutoLogin())
				connect();
			if (mService.isLoggedIn()) {
				((TextView) findViewById(R.id.success)).setText("Online");
			}
		}

		@Override
		public void onServiceDisconnected(ComponentName arg0) {
			mService.unregisterPostLoginSuccessHook(postLoginSuccessHook);

			mService.unregisterPostLoginFailureHook(postLoginFailureHook);

			mService.unregisterLoginStartHook(loginStartHook);

			mService.unregisterLoginEndHook(loginEndHook);

			mService.unregisterPostLogoutHook(postLogoutHook);

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
		// Login if not already or just start logged in view if present
		Log.d("MoSeS.ACTIVITY", "Connect button pressed.");
		if (mService != null) {
			if (!mService.isLoggedIn()) {
				mService.login();
			}
		}
	}

	/**
	 * Disconnect from the service if it is connected and stop logged in check
	 */
	private void disconnectService() {
		stopPosting = true;
		mHandler.removeCallbacks(mIsServiceAliveTask);
		if (mBound) {
			unbindService(mConnection);
		}
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
	 * User comes back from another activity
	 */
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (!isMosesServiceRunning())
			startAndBindService();
		if (requestCode == 0) {
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
		((TextView) findViewById(R.id.success)).setText("Offline");
		((Button) findViewById(R.id.testfield_button))
				.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						Intent mainDialog = new Intent(MosesActivity.this,
								LoggedInViewActivity.class);
						startActivityForResult(mainDialog, 0);
					}
				});
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
		((ProgressBar) findViewById(R.id.main_spinning_progress_bar))
				.setVisibility(View.GONE);
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
		if (!isMosesServiceRunning())
			startService(intent);
		bindService(intent, mConnection, 0);
	}

	public void settings() {
		Intent mainDialog = new Intent(MosesActivity.this,
				MosesPreferences.class);
		startActivityForResult(mainDialog, 0);
	}

	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.main_menu, menu);
		return true;
	}

	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.item_connect:
			connect();
			break;
		case R.id.item_settings:
			settings();
			break;
		case R.id.item_exit:
			finish();
			break;
		}
		return true;
	}

}