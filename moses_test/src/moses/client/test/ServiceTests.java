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

	public ServiceTests() {
		super(MosesActivity.class);
	}

	protected void setUp() throws Exception {
		super.setUp();
		NetworkJSON.debug = true;
		TestResponseGenerator r = new TestResponseGenerator();
		NetworkJSON.response = r;
		SharedPreferences.Editor e = PreferenceManager.getDefaultSharedPreferences(getActivity()).edit();
		e.putString("username_pref", "alex");
		e.putString("password_pref", "777");
		e.putString("deviceid_pref", "someid");
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
		assertTrue(MosesService.getInstance().isLoggedIn());
		assertEquals("1234567", MosesService.getInstance().getSessionID());
	}
}
