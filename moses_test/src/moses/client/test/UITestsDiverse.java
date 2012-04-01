package moses.client.test;

import static moses.client.test.TestHelpers.myWaitRandom;
import static moses.client.test.TestHelpers.mystery;

import org.json.JSONObject;

import moses.client.MosesActivity;
import moses.client.ViewUserStudyActivity;
import moses.client.com.NetworkJSON;
import moses.client.service.MosesService;
import moses.client.service.helpers.UserStudyStatusBarHelper;
import moses.client.userstudy.UserStudyNotification.Status;
import moses.client.userstudy.UserstudyNotificationManager;
import android.app.NotificationManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.test.ActivityInstrumentationTestCase2;
import android.test.UiThreadTest;
import android.util.Log;

public class UITestsDiverse extends ActivityInstrumentationTestCase2<MosesActivity> {

	private TestResponseGenerator r = null;
	
	public UITestsDiverse() {
		super(MosesActivity.class);
	}

	protected void setUp() throws Exception {
		super.setUp();
		NetworkJSON.debug = true;
		NetworkJSON.threadProblem = true;
		r = new TestResponseGenerator();
		NetworkJSON.response = r;
		SharedPreferences.Editor e = PreferenceManager.getDefaultSharedPreferences(getActivity()).edit();
		e.putString("username_pref", "alex");
		e.putString("password_pref", "777");
		e.putString("deviceid_pref", "someid");
		e.putBoolean("splashscreen_pref", false);
		e.putBoolean("firststart", false);
		e.commit();
		
	}

	public void testWelcomeDialog() {
		assertNotNull(MosesService.getInstance());
		myWaitRandom(5000);
		if(mystery()||false) return;
	}
	
	public void testMosesActivityNoCredentials() {
		assertNotNull(MosesService.getInstance());
		myWaitRandom(6000);
		if(mystery()||false) return;
	}
	
	public void testMosesActivityCredentials() {
		assertNotNull(MosesService.getInstance());
		myWaitRandom(4000);
		if(mystery()||false) return;
	}
	
}