package de.da_sense.moses.client;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;

import de.da_sense.moses.client.preferences.MosesPreferences;
import de.da_sense.moses.client.util.Log;

/**
 * Splash screen shown to the user the first time the application is started.
 * 
 * @author Zijad Maksuti
 * 
 */
public class SplashScreen extends Activity {

	private static final String LOG_TAG = SplashScreen.class.getName();

	private static boolean isAsyncTaskRunning = false;

	/*
	 * True only if this activity is WelcomeActivity is started. 
	 */
	private boolean mIsWelcomeActivityStarted;
	private static String KEY_IS_WELCOME_ACTIVITY_STARTED = "mIsWelcomeActivityStarted";

	private boolean mGooglePlayServicesOperational;
	private static String KEY_GOOGLE_PLAY_SERVICES_OPERATIONAL = "mGooglePlayServicesOperational";
	
	private AlertDialog mErrorDialog;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if (savedInstanceState != null){
			mGooglePlayServicesOperational = savedInstanceState.getBoolean(
					KEY_GOOGLE_PLAY_SERVICES_OPERATIONAL, false);
			mIsWelcomeActivityStarted = savedInstanceState.getBoolean(KEY_IS_WELCOME_ACTIVITY_STARTED, false);
		}
		else{
			mGooglePlayServicesOperational = false;
			mIsWelcomeActivityStarted = false;
		}

		if (PreferenceManager.getDefaultSharedPreferences(this).getBoolean(MosesPreferences.PREF_SHOW_SPLASHSCREEN, true)) {
			// Splash screen needs to be shown, set the content view
			setContentView(R.layout.splashscreen);
			try {
				((TextView) findViewById(R.id.versiontextview))
						.setText(getPackageManager().getPackageInfo(
								getPackageName(), 0).versionName);
			} catch (NameNotFoundException e) {
				Log.d(LOG_TAG, "There's no MoSeS around here.");
			}

			/*
			 * The splash screen should disappear instantly, when the user taps
			 * on it.
			 */
			final View splashScreenContainer = findViewById(R.id.splash_screen_container);
			splashScreenContainer.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					if (mGooglePlayServicesOperational) {
						startWelcomeActivity();
					}
				}
			});
		}

	}

	protected void onResume() {
		super.onResume();

		/*
		 * Check if Google Play Service is operational, if not, inform the user.
		 * The services are needed for using Google Cloud Messaging (GCM).
		 */
		int result = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
		if (result != ConnectionResult.SUCCESS) {
			Log.d(LOG_TAG,
					"Google Play Service not operational, starting error dialog");
			mErrorDialog = (AlertDialog) GooglePlayServicesUtil.getErrorDialog(result,
					this, 10); // the request code is a dummy, we don't check the results in onResult()
			
			// prevent user from dismissing the dialog
			mErrorDialog.setCancelable(false);
			
			mErrorDialog.show();
		} else {
			mGooglePlayServicesOperational = true;
			Log.d(LOG_TAG, "Google Play Service operational");
			if (PreferenceManager.getDefaultSharedPreferences(this).getBoolean(
					MosesPreferences.PREF_SHOW_SPLASHSCREEN, true)){
				if (!isAsyncTaskRunning) {
					AsyncStartWelcomeActivity asyncTask = new AsyncStartWelcomeActivity();
					asyncTask.execute(getResources().getInteger(
							R.integer.splash_screen_duration_milliseconds));
				}
			}
			else{
				// splash screen does not needs to be shown,
				// start welcome activity
				startWelcomeActivity();
			}
		}
	}
	
	

	/* (non-Javadoc)
	 * @see android.app.Activity#onPause()
	 */
	@Override
	protected void onPause() {
		super.onPause();
		if(mErrorDialog != null){
			mErrorDialog.dismiss();
			mErrorDialog = null;
		}
	}

	public void onSaveInstanceState(Bundle savedInstanceState) {
		super.onSaveInstanceState(savedInstanceState);
		savedInstanceState.putBoolean(KEY_GOOGLE_PLAY_SERVICES_OPERATIONAL,
				mGooglePlayServicesOperational);
		savedInstanceState.putBoolean(KEY_IS_WELCOME_ACTIVITY_STARTED, mIsWelcomeActivityStarted);
	}

	/**
	 * Finishes this activity and starts {@link WelcomeActivity}. This method checks for the value of
	 * {@link #mIsWelcomeActivityStarted} to check if the WelcomeActivity was already started. If so, the method
	 * takes no action.
	 */
	private synchronized void startWelcomeActivity() {
		if(!mIsWelcomeActivityStarted){
			Log.d(LOG_TAG, "startWelcomeActivity() starting WelcomeActivity");
			Intent intent = new Intent(this, WelcomeActivity.class);
			startActivity(intent);
			mIsWelcomeActivityStarted = true;
			finish();
		}
		else
			Log.d(LOG_TAG, "startWelcomeActivity() skipped starting WelcomeActivity");
	}

	/**
	 * This task starts the welcome activity after the specified time.
	 * 
	 * @author Zijad Maksuti
	 * 
	 */
	private class AsyncStartWelcomeActivity extends
			AsyncTask<Integer, Void, Void> {

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			isAsyncTaskRunning = true;
		}

		protected Void doInBackground(Integer... params) {
			try {
				Thread.sleep(params[0]);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			return null;
		}

		@Override
		protected void onPostExecute(Void params) {
			isAsyncTaskRunning = false;
			startWelcomeActivity();
		}

	}
}