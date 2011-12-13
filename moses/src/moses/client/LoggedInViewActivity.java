package moses.client;

import moses.client.abstraction.HardwareAbstraction;
import moses.client.com.ConnectionParam;
import moses.client.com.NetworkJSON.BackgroundException;
import moses.client.com.ReqTaskExecutor;
import moses.client.com.requests.RequestLogin;
import moses.client.com.requests.RequestLogout;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

/**
 * This activity resembles the view after logging in
 * @author Jaco
 *
 */
public class LoggedInViewActivity extends Activity {
	
	private class ReqClassLogout implements ReqTaskExecutor {

		@Override
		public void handleException(Exception e) {
			txtSuccess.setText("FAILURE: " + e.getMessage());
		}

		@Override
		public void postExecution(String s) {
			JSONObject j = null;
			try {
				j = new JSONObject(s);
				if (RequestLogout.logoutValid(j)) {
					Intent loginScreen = new Intent();
					setResult(RESULT_OK, loginScreen);
					finish();
				} else {
					// TODO handling!!
					txtSuccess.setText("LOGOUT WAS REJECTED FROM SERVER"
							+ j.toString());
				}
			} catch (JSONException e) {
				this.handleException(e);
			}
		}

		@Override
		public void updateExecution(BackgroundException c) {
			if (c.c != ConnectionParam.EXCEPTION) {
				txtSuccess.setText(c.toString());
			} else {
				handleException(c.e);
			}
		}
	}
	private Button btnLogout;
	private Button btnSyncHW;
	private Button btnGetHW; // used for getting hw configuration stored on the server
	private Button btnSelectFilter;
	
	
	private TextView txtSuccess;
	
	private SharedPreferences settings;
	
	private void checkHardware(boolean force) {
		if(settings.getBoolean("synchw", true) || force) {
			HardwareAbstraction hw = new HardwareAbstraction(this);
			hw.checkHardwareParameters();
			SharedPreferences.Editor editor = settings.edit();
			editor.putBoolean("synchw", false);
			editor.commit();
		}
	}
	
	/**
	 * This method is called when getHW-Button is pushed
	 * @param force
	 */
	private void getHardwareParameters() {
		HardwareAbstraction hw = new HardwareAbstraction(this);
		hw.getHardwareParameters();
	}
	
	private void initControls() {
		
		txtSuccess = (TextView) findViewById(R.id.success);
		
		btnLogout = (Button) findViewById(R.id.logout_button);
		btnLogout.setOnClickListener(new Button.OnClickListener() {
			@Override
			public void onClick(View v) {
				logout();
			}
		});
		
		btnSyncHW = (Button) findViewById(R.id.synchw);
		btnSyncHW.setOnClickListener(new Button.OnClickListener() {
			@Override
			public void onClick(View v) {
				checkHardware(true);
			}
		});
		
		btnSelectFilter = (Button) findViewById(R.id.selectfilter);
		btnSelectFilter.setOnClickListener(new Button.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent chooseSensors = new Intent(getInstance(),
						ChooseSensorsActivity.class);
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
		
	}
	
	private Activity getInstance() {
		return this;
	}
	
	private void logout() {
		String sessionID = RequestLogin.getSessionID();
		RequestLogout rlogout = new RequestLogout(new ReqClassLogout(),
				sessionID);
		rlogout.send();
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.loggedinview);
		settings = getSharedPreferences("MoSeS.cfg", 0);
		initControls();
		checkHardware(false);
	}
	

}
