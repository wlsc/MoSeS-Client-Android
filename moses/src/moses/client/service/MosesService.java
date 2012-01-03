package moses.client.service;

import moses.client.service.helpers.Login;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Binder;
import android.os.IBinder;
import android.widget.Toast;

public class MosesService extends android.app.Service {

	private final IBinder mBinder = new LocalBinder();

	private SharedPreferences settingsFile;
	private MosesSettings mset = new MosesSettings();

	@Override
	public IBinder onBind(Intent arg0) {
		return mBinder;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		initConfig();
		Toast.makeText(this, "Service Created", Toast.LENGTH_LONG).show();
	}

	private void initConfig() {
		settingsFile = getSharedPreferences("MoSeS.cfg", 0);
		mset.saveunamepw = settingsFile.getBoolean("saveunamepw", false);
		mset.loginauto = settingsFile.getBoolean("loginauto", false);
		mset.username = settingsFile.getString("uname", "");
		mset.password = settingsFile.getString("password", "");
	}

	@Override
	public void onDestroy() {
		super.onDestroy();

		Toast.makeText(this, "Service Destroyed", Toast.LENGTH_LONG).show();

	}

	@Override
	public void onStart(Intent intent, int startId) {

		super.onStart(intent, startId);

		Toast.makeText(this, "Service Started", Toast.LENGTH_LONG).show();
	}

	public class LocalBinder extends Binder {
		public MosesService getService() {
			return MosesService.this;
		}
	}

	public void reloadSettings() {
		initConfig();
	}

	public void login() {
		new Login(mset.username, mset.password, this);
	}

	public boolean isLoggedIn() {
		return mset.loggedIn;
	}

	public void loggedIn(String sessionid) {
		mset.loggedIn = true;
		mset.sessionid = sessionid;
	}

	public class MosesSettings {
		public boolean loginauto;
		public boolean saveunamepw;

		public String username;
		public String password;

		public String sessionid = "";

		public boolean loggedIn = false;

	}

}
