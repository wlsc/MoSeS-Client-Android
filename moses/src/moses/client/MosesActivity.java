package moses.client;

import java.util.HashMap;
import java.util.Map;

import moses.client.com.ConnectionParam;
import moses.client.com.NetworkJSON;
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
	}

	private class ReqClass implements ReqTaskExecutor {

		public void postExecution(String s) {
			JSONObject j = null;
			try {
				j = new JSONObject(s);
				if(RequestLogin.loginValid(j, txtUname.getText().toString())) {
					txtSuccess.setText("SUCCESS");
				} else {
					txtSuccess.setText("NOT GRANTED: " + j.toString());
				}
			} catch (JSONException e) {
				this.handleException(e);
			}
		}

		public void updateExecution(ConnectionParam c) {
			txtSuccess.setText(c.toString());
		}

		public void handleException(Exception e) {
			txtSuccess.setText("FAILURE: " + e.getMessage());
		}
	}
	
	private void connect() {
		NetworkJSON.url = txtWebAddr.getText().toString();
		RequestLogin r = new RequestLogin(new ReqClass(), txtUname.getText().toString(), txtPW.getText().toString());
		r.send();
	}
}