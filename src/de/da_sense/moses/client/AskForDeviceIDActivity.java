package de.da_sense.moses.client;

import android.app.ActionBar;
import android.app.Activity;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Activity to ask for an ID for the device.
 * 
 * @author Jaco Hofmann, Sandra Amend, Wladimir Schmidt
 * 
 */
public class AskForDeviceIDActivity extends Activity {
	/**
	 * Called when the activity is first created.
	 * 
	 * @param savedInstanceState
	 *            the saved instance state
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		ActionBar ab = getActionBar();
		this.setTitle("MoSeS - Device ID");
		ab.setDisplayShowTitleEnabled(true);

		setContentView(R.layout.askfordeviceid);

		TextView t = (TextView) findViewById(R.id.askfordeviceid_deviceID_text);
		t.setText(PreferenceManager.getDefaultSharedPreferences(this)
				.getString("deviceid_pref", ""));
		if (getIntent().getBooleanExtra("showFailedDialog", false)) {
			((TextView) findViewById(R.id.change_deviceID_text))
					.setText(R.string.deviceID_change_text);
		}
	}

	/**
	 * We ignore presses on the back button.
	 */
	@Override
	public void onBackPressed() {
		return;
	}

	/**
	 * Handles the click on the "Set Device ID"-Button.
	 * @param v View
	 */
	public void handleClick(View v) {
		TextView t = (TextView) findViewById(R.id.askfordeviceid_deviceID_text);
		String deviceID = t.getText().toString();
		if (deviceID.equals("")) {
			Toast.makeText(getApplicationContext(),
					"Please enter a non-empty string.",
					Toast.LENGTH_SHORT).show();
		} else {
			Log.d("AskForDeviceID", "Device ID set to: " + t.getText());
			PreferenceManager
					.getDefaultSharedPreferences(this).edit()
					.putString("deviceid_pref", t.getText().toString())
					.commit();
			finish();
		}
	}

}
