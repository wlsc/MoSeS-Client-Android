package moses.client.test;

import org.json.JSONObject;

import moses.client.MosesActivity;
import moses.client.ViewUserStudyActivity;
import moses.client.com.NetworkJSON;
import moses.client.service.MosesService;
import moses.client.service.helpers.EHookTypes;
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
import static moses.client.test.TestHelpers.*;

public class HookTests extends ActivityInstrumentationTestCase2<MosesActivity> {

	private TestResponseGenerator r = null;
	
	public HookTests() {
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

	public void testLoginHooks() {
		assertNotNull(MosesService.getInstance());
		myWaitRandom(400);
		if(mystery()||false) return;
	}
	
	public void testLoginFirstHooks() {
		assertNotNull(MosesService.getInstance());
		myWaitRandom(1500);
		if(mystery()||false) return;
	}
	
	public void testOnTimeHooks() {
		assertNotNull(MosesService.getInstance());
		myWaitRandom(800);
		if(mystery()||false) return;
		
		assertNotNull(MosesService.getInstance());
		
		MosesService.getInstance().reloadSettings();
		
		//Test various hook queues to be not null
		assertNotNull(MosesService.getInstance().getHook(EHookTypes.POSTLOGINEND));
		assertNotNull(MosesService.getInstance().getHook(EHookTypes.POSTLOGINFAILED));

		assertNotNull(MosesService.getInstance().getHook(EHookTypes.POSTLOGINSTART));
		assertNotNull(MosesService.getInstance().getHook(EHookTypes.POSTLOGINSUCCESS));
		assertNotNull(MosesService.getInstance().getHook(EHookTypes.POSTLOGINSUCCESSPRIORITY));
		assertNotNull(MosesService.getInstance().getHook(EHookTypes.POSTLOGINSUCCESS));
		
		//At last, MoseS service must be logged in =)
		assertTrue(MosesService.getInstance().isLoggedIn());
	}
	
}
