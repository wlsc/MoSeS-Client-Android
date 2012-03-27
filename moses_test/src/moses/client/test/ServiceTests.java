package moses.client.test;

import org.json.JSONObject;

import moses.client.MosesActivity;
import moses.client.com.NetworkJSON;
import moses.client.service.MosesService;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.test.ActivityInstrumentationTestCase2;
import android.test.UiThreadTest;

public class ServiceTests extends ActivityInstrumentationTestCase2<MosesActivity> {

	private TestResponseGenerator r = null;
	
	public ServiceTests() {
		super(MosesActivity.class);
	}

	protected void setUp() throws Exception {
		super.setUp();
		NetworkJSON.debug = true;
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

	public void testIsServiceRunning() {
		assertNotNull(MosesService.getInstance());
	}
	
	@UiThreadTest
	public void testLoginLogsIn() throws Throwable {
		assertFalse(MosesService.getInstance().isLoggedIn());
		MosesService.getInstance().login();
		while(!MosesService.getInstance().isLoggedIn() || MosesService.getInstance().isLoggingIn());
		assertTrue(MosesService.getInstance().isLoggedIn());
		assertEquals(Integer.toString(TestResponseGenerator.sessionid), MosesService.getInstance().getSessionID());
	}
}
