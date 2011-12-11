package moses.client;

import java.util.HashMap;
import java.util.Map;
import java.util.Timer;

import moses.client.com.ConnectionParam;
import moses.client.com.NetworkJSON;
import moses.client.com.NetworkJSON.BackgroundException;
import moses.client.com.ReqTaskExecutor;
import moses.client.com.requests.RequestLogin;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class MosesActivity extends Activity {
	private EditText txtUname;
	private EditText txtPW;
	private EditText txtWebAddr;

	private TextView txtSuccess;

	private Button btnconnect;
	
	private NetworkJSON task;
	private Button btnLogout;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		initControls();
	}

	private void initControls() {
		txtUname = (EditText) findViewById(R.id.uname);
		txtPW = (EditText) findViewById(R.id.pword);
		txtWebAddr = (EditText) findViewById(R.id.websiteaddr);

		txtSuccess = (TextView) findViewById(R.id.success);
		
		btnconnect = (Button) findViewById(R.id.connect_button);
		btnconnect.setOnClickListener(new Button.OnClickListener() {
			public void onClick(View v) {
				connect();
			}
		});
		
		btnLogout = (Button) findViewById(R.string.logoutText);
		btnLogout.setVisibility(View.INVISIBLE);
		btnLogout.setOnClickListener(new Button.OnClickListener() {
			public void onClick(View v) {
				logout();
			}
		});
	}
	
	private void connect() {
		NetworkJSON.url = txtWebAddr.getText().toString();
		RequestLogin r = new RequestLogin(new ReqClass(), txtUname.getText().toString(), txtPW.getText().toString());
		r.send();
	}
	
	private void logout(){
		
	}
	
	private class ReqClass implements ReqTaskExecutor {

		public void postExecution(String s) {
			JSONObject j = null;
			try {
				j = new JSONObject(s);
				if(RequestLogin.loginValid(j, txtUname.getText().toString())) {
					txtSuccess.setText("SUCCESS");
					
					
					txtSuccess.setVisibility(View.INVISIBLE);
					txtWebAddr.setVisibility(View.INVISIBLE);
					txtUname.setVisibility(View.INVISIBLE);
					txtPW.setVisibility(View.INVISIBLE);
					
					btnconnect.setVisibility(View.INVISIBLE);
					btnLogout.setVisibility(View.VISIBLE);
					
				} else {
					txtSuccess.setText("NOT GRANTED: " + j.toString());
				}
			} catch (JSONException e) {
				this.handleException(e);
			}
		}

		public void updateExecution(BackgroundException c) {
			if(c.c != ConnectionParam.EXCEPTION) {
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