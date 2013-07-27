package de.da_sense.moses.client;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;
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

	private static boolean isActivityFinished = false;

	private boolean mGooglePlayServicesOperational;
	private static String KEY_GOOGLE_PLAY_SERVICES_OPERATIONAL = "mGooglePlayServicesOperational";

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if (savedInstanceState != null)
			mGooglePlayServicesOperational = savedInstanceState.getBoolean(
					KEY_GOOGLE_PLAY_SERVICES_OPERATIONAL, false);
		else
			mGooglePlayServicesOperational = false;

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
					if (!isActivityFinished && mGooglePlayServicesOperational) {
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
			Dialog errorDialog = GooglePlayServicesUtil.getErrorDialog(result,
					this, 10);
			errorDialog.show();
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
				// splash screen does not needs to be shown, don't start the login
				// activity
				startWelcomeActivity();
			}
		}
	}

	public void onSaveInstanceState(Bundle savedInstanceState) {
		super.onSaveInstanceState(savedInstanceState);
		savedInstanceState.putBoolean(KEY_GOOGLE_PLAY_SERVICES_OPERATIONAL,
				mGooglePlayServicesOperational);
	}

	/**
	 * Finishes this activity and starts {@link WelcomeActivity}
	 */
	private void startWelcomeActivity() {
		Intent intent = new Intent(this, WelcomeActivity.class);
		startActivity(intent);
		isActivityFinished = true;
		finish();
	}

	/**
	 * This task starts starts the welcome activity after the specified time.
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
			if (!isActivityFinished)
				startWelcomeActivity();
		}

	}
}