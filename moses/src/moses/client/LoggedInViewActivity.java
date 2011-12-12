package moses.client;

import org.json.JSONException;
import org.json.JSONObject;

import moses.client.com.ConnectionParam;
import moses.client.com.ReqTaskExecutor;
import moses.client.com.NetworkJSON.BackgroundException;
import moses.client.com.requests.RequestLogin;
import moses.client.com.requests.RequestLogout;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class LoggedInViewActivity extends Activity {
	
	private Button btnLogout;
	private TextView txtSuccess;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.loggedinview);
		initControls();
	}
	
	private void initControls() {
		
		txtSuccess = (TextView) findViewById(R.id.success);
		
		btnLogout = (Button) findViewById(R.id.logout_button);
		btnLogout.setOnClickListener(new Button.OnClickListener() {
			public void onClick(View v) {
				logout();
			}
		});
	}
	
	private void logout() {
		String sessionID = RequestLogin.getSessionID();
		RequestLogout rlogout = new RequestLogout(new ReqClassLogout(),
				sessionID);
		rlogout.send();
	}
	
	private class ReqClassLogout implements ReqTaskExecutor {

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

		public void updateExecution(BackgroundException c) {
			if (c.c != ConnectionParam.EXCEPTION) {
				txtSuccess.setText(c.toString());
			} else {
				handleException(c.e);
			}
		}

		public void handleException(Exception e) {
			txtSuccess.setText("FAILURE: " + e.getMessage());
		}
	}
}
