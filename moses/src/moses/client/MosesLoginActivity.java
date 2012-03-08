package moses.client;

import moses.client.userstudy.UserstudyNotificationManager;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

public class MosesLoginActivity extends Activity {
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.login);

		final EditText username = (EditText)findViewById(R.id.login_username);
		final EditText password = (EditText)findViewById(R.id.login_password);

		Button login = (Button)findViewById(R.id.login_login_button);
		login.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent resultData = new Intent();
				if(username.getText().toString().equals("") || password.getText().toString().equals("")) return;
				resultData.putExtra("username_pref", username.getText().toString());
				resultData.putExtra("password_pref", password.getText().toString());
				setResult(Activity.RESULT_OK, resultData);
				finish();
			}
		});
		Button exit  = (Button)findViewById(R.id.login_exit_button);
		exit.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent resultData = new Intent();
				setResult(Activity.RESULT_CANCELED, resultData);
				finish();
			}
		});
		
		//TODO: !remove debug
//		Button buttonNotificationTest = (Button) findViewById(R.id.buttonTestNotification2);
//		buttonNotificationTest.setOnClickListener(new OnClickListener() {
//			@Override
//			public void onClick(View v) {
//				UserstudyNotificationManager.fakeUserStudyNotification();
//			}
//		});
	}

	@Override
	public void onBackPressed() {
	   return;
	}

}
