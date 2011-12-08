package moses.client;

import java.util.HashMap;
import java.util.Map;

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

	private void connect() {

		JSONObject j = new JSONObject();
		try {
			j.put("MESSAGE", "LOGIN_REQUEST");
			j.put("LOGIN", txtUname.getText().toString());
			j.put("PASSWORD", txtPW.getText().toString());
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		task = new NetworkJSON();

		NetworkJSON.APIRequest req;

		req = task.new APIRequest();
		req.url = txtWebAddr.getText().toString();
		req.request = j;
		req.txtv = txtSuccess;

		task.execute(req);
	}
}