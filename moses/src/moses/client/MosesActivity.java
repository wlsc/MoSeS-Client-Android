package moses.client;

import moses.client.com.NetworkJSON;
import moses.client.service.MosesService;
import moses.client.service.MosesService.LocalBinder;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.IBinder;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;

/**
 * This activity is the main activity of our app providing login etc.
 * 
 * @author Jaco
 * 
 */
public class MosesActivity extends Activity {

	private EditText txtUname;

	private EditText txtPW;

	private Button btnconnect;

	private Button btnExit;
	private CheckBox chkLoginAuto;

	private CheckBox chkSaveUnamePW;

	private SharedPreferences settings;

	public MosesService mService;
	public static boolean mBound = false;

	private void connect() {
		SharedPreferences.Editor editor = settings.edit();
		if (chkSaveUnamePW.isChecked()) {
			editor.putString("uname", txtUname.getText().toString());
			editor.putString("password", txtPW.getText().toString());
		}
		editor.putBoolean("loginauto", chkLoginAuto.isChecked());
		editor.putBoolean("saveunamepw", chkSaveUnamePW.isChecked());
		editor.commit();
		// Wait till we're connected
		// TODO: Less ugly implementation
		mService.reloadSettings();
		mService.login();
		//while (!mService.isLoggedIn())
		//	;
		Intent mainDialog = new Intent(this, LoggedInViewActivity.class);
		startActivity(mainDialog);

	}

	private void initControls() {
		txtUname = (EditText) findViewById(R.id.uname);
		txtPW = (EditText) findViewById(R.id.pword);

		chkLoginAuto = (CheckBox) findViewById(R.id.loginauto);
		chkSaveUnamePW = (CheckBox) findViewById(R.id.saveunamepw);

		btnconnect = (Button) findViewById(R.id.connect_button);
		btnconnect.setClickable(false);
		btnconnect.setOnClickListener(new Button.OnClickListener() {
			@Override
			public void onClick(View v) {
				connect();
			}
		});

		btnExit = (Button) findViewById(R.id.exitbutton);
		btnExit.setOnClickListener(new Button.OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
			}
		});
	}

	private void loadConfig() {
		settings = getSharedPreferences("MoSeS.cfg", 0);
		txtUname.setText(settings.getString("uname", ""));
		txtPW.setText(settings.getString("password", ""));
		NetworkJSON.url = settings.getString("url",
				"http://212.72.183.71:80/moses/test.php");
		chkLoginAuto.setChecked(settings.getBoolean("loginauto", false));
		chkSaveUnamePW.setChecked(settings.getBoolean("saveunamepw", false));
	}

	private void startAndBindService() {
		Intent intent = new Intent(this, MosesService.class);
		bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
	}

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		initControls();
		loadConfig();
	}

	protected void onStart() {
		super.onStart();
		startAndBindService();
	}

	protected void onStop() {
		super.onStop();
		if (mBound) {
			unbindService(mConnection);
			mBound = false;
		}
	}

	private ServiceConnection mConnection = new ServiceConnection() {

		@Override
		public void onServiceConnected(ComponentName className, IBinder service) {
			// We've bound to LocalService, cast the IBinder and get
			// LocalService instance
			LocalBinder binder = (LocalBinder) service;
			mService = binder.getService();
			mBound = true;
			btnconnect.setClickable(true);
		}

		@Override
		public void onServiceDisconnected(ComponentName arg0) {
			mBound = false;
		}
	};

}