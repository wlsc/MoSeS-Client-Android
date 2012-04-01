package moses.client;

import moses.client.preferences.MosesPreferences;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.TextView;

public class MosesAskForDeviceIDActivity extends Activity {
	/**
	 * Called when the activity is first created.
	 * 
	 * @param savedInstanceState
	 *            the saved instance state
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.askfordeviceid);
		TextView t = (TextView) findViewById(R.id.askfordeviceid_deviceid_text);
		t.setText(PreferenceManager.getDefaultSharedPreferences(this).getString("deviceid_pref", ""));
		if(getIntent().getBooleanExtra("showFailedDialog", false)) {
			((TextView) findViewById(R.id.changedeviceidtext)).setText(R.string.change_dev_id_text);
		}
			
		((ImageButton) findViewById(R.id.askfordeviceid_forward_btn)).setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				TextView t = (TextView) findViewById(R.id.askfordeviceid_deviceid_text);
				if (t.getText().equals(""))
					return;
				PreferenceManager.getDefaultSharedPreferences(MosesAskForDeviceIDActivity.this).edit()
						.putString("deviceid_pref", t.getText().toString()).commit();
				if (getIntent().getBooleanExtra("firststart", false)) {
					Intent startPreference = new Intent(getApplicationContext(), MosesPreferences.class);
					startPreference.putExtra("startSensors", true);
					startActivity(startPreference);
				}
				finish();
			}
		});
	}

	@Override
	public void onBackPressed() {
		return;
	}
}
