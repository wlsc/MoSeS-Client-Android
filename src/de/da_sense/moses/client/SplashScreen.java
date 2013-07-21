package de.da_sense.moses.client;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;
import de.da_sense.moses.client.util.Log;

/**
 * Splash screen shown to the user the first time the application is started.
 * 
 * @author Zijad Maksuti
 * 
 */
public class SplashScreen extends Activity {

	/**
	 * Boolean for the splash screen, used for debugging purposes only
	 */
	private static boolean showsplash = true;
	private static final String LOG_TAG = SplashScreen.class.getName();
	
	private static boolean isAsyncTaskRunning=false;
	
	private static boolean isActivityFinished=false;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		boolean isShowScreenSetInPrefs = PreferenceManager
				.getDefaultSharedPreferences(this).getBoolean(
						"splashscreen_pref", true);

		if (!showsplash || !isShowScreenSetInPrefs || isActivityFinished) {
			// No splash screen needs to be shown, start the welcome activity
			startWelcomeActivity();
		} else {
			// Proceed with showing the splash screen
			setContentView(R.layout.splashscreen);
			try {
				((TextView) findViewById(R.id.versiontextview))
						.setText(getPackageManager().getPackageInfo(
								getPackageName(), 0).versionName);
			} catch (NameNotFoundException e) {
				Log.d(LOG_TAG, "There's no MoSeS around here.");
			}

			/*
			 * The splash screen should disappear instantly, when the user
			 * taps on it.
			 */
			final View splashScreenContainer = findViewById(R.id.splash_screen_container);
			splashScreenContainer.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					if(!isActivityFinished){
						startWelcomeActivity();
					}
				}
			});
			
			if(!isAsyncTaskRunning){
				AsyncStartWelcomeActivity asyncTask = new AsyncStartWelcomeActivity();
				asyncTask.execute(getResources().getInteger(R.integer.splash_screen_duration_milliseconds));
			}

		}

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
			isAsyncTaskRunning=false;
			if(!isActivityFinished)
				startWelcomeActivity();
		}

	}
}