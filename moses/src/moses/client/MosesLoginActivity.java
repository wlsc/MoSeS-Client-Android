package moses.client;

import org.json.JSONException;
import org.json.JSONObject;

import moses.client.com.ConnectionParam;
import moses.client.com.NetworkJSON.BackgroundException;
import moses.client.com.ReqTaskExecutor;
import moses.client.com.requests.RequestLogin;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

public class MosesLoginActivity extends Activity {
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.login);

		final EditText username = (EditText) findViewById(R.id.login_username);
		final EditText password = (EditText) findViewById(R.id.login_password);

		Button login = (Button) findViewById(R.id.login_login_button);
		login.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (username.getText().toString().equals("") || password.getText().toString().equals(""))
					return;
				d = new ProgressDialog(MosesLoginActivity.this, ProgressDialog.STYLE_SPINNER);
				d.setTitle(getString(R.string.checking_credentials));
				d.setMessage(getString(R.string.verifying_credentials));
				d.show();
				new RequestLogin(new ReqLogin(), username.getText().toString(), password.getText().toString()).send();
			}
		});
		Button exit = (Button) findViewById(R.id.login_exit_button);
		exit.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent resultData = new Intent();
				setResult(Activity.RESULT_CANCELED, resultData);
				finish();
			}
		});
		
	}

	private void valid() {
		d.dismiss();
		final EditText username = (EditText) findViewById(R.id.login_username);
		final EditText password = (EditText) findViewById(R.id.login_password);
		Intent resultData = new Intent();
		resultData.putExtra("username_pref", username.getText().toString());
		resultData.putExtra("password_pref", password.getText().toString());
		setResult(Activity.RESULT_OK, resultData);
		finish();
	}

	private void invalid() {
		d.dismiss();
	}

	private ProgressDialog d = null;
	private Handler h = new Handler();

	@Override
	public void onBackPressed() {
		return;
	}

	private class ReqLogin implements ReqTaskExecutor {

		@Override
		public void handleException(Exception e) {
			d.setMessage(getString(R.string.check_internet));
			h.postDelayed(new Runnable() {

				@Override
				public void run() {
					invalid();
				}
			}, 2000);
		}

		@Override
		public void postExecution(String s) {
			try {
				JSONObject j = new JSONObject(s);
				if(j.getString("SESSIONID").equals("NULL")) {
					d.setMessage(getString(R.string.wrong_credentials));
					h.postDelayed(new Runnable() {
						@Override
						public void run() {
							invalid();
						}
					}, 2000);
				} else {
					valid();
				}
			} catch (JSONException e) {
				handleException(e);
			}
		}

		@Override
		public void updateExecution(BackgroundException c) {
			if(c.c == ConnectionParam.EXCEPTION) {
				handleException(c.e);
			}
		}

	}
}
