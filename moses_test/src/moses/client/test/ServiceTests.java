package moses.client.test;

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
		
		//initialization for notification test
		if(UserstudyNotificationManager.getInstance()!=null) {
			UserstudyNotificationManager.init(getActivity());
		}
		UserstudyNotificationManager.getInstance().removeNotificationWithApkId(TestResponseGenerator.FAKE_NOTIFICATION_APK_ID);
		UserstudyNotificationManager.getInstance().saveToDisk(getActivity());
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
	
	
	static class UIThreadBinder {
		public boolean finished = false;
	}
	public void testNotification() throws Throwable {
		final UIThreadBinder binder = new UIThreadBinder();
		
		UserstudyNotificationManager.userStudyNotificationArrived(TestResponseGenerator.FAKE_NOTIFICATION_APK_ID);
		
		NotificationManager mNotificationManager = (NotificationManager) MosesService.getInstance().getSystemService(
				Context.NOTIFICATION_SERVICE);
		
		long t0 = System.currentTimeMillis();
		while(System.currentTimeMillis()-t0 < 2000);
		
		
//		launchActivityWithIntent("moses.client", ViewUserStudyActivity.class, UserStudyStatusBarHelper.generateIntentForNotification(FAKE_NOTIFICATION_ID, MosesService.getInstance()));
		
		//cancel the notification
		mNotificationManager.cancel(UserStudyStatusBarHelper.notificationManagerIdForApkId(TestResponseGenerator.FAKE_NOTIFICATION_APK_ID));
		//launch the intent as if it were by clicking the notification
		ViewUserStudyActivity.autoActions.add(Status.UNDECIDED);
		Log.d("TEST", "starting activity ViewUserStudiesActivity");
		
		runTestOnUiThread(new Runnable() {
			@Override
			public void run() {
				MosesService.getInstance().startActivity(UserStudyStatusBarHelper.generateIntentForNotification(TestResponseGenerator.FAKE_NOTIFICATION_APK_ID, MosesService.getInstance()));
				binder.finished = true;
			}
		});
		while(!binder.finished);
		binder.finished = false;
		
		myWait(5000);
		assertTrue(UserstudyNotificationManager.getInstance().getNotificationForApkId(TestResponseGenerator.FAKE_NOTIFICATION_APK_ID) != null);
		assertTrue(UserstudyNotificationManager.getInstance().getNotificationForApkId(TestResponseGenerator.FAKE_NOTIFICATION_APK_ID).getStatus() == Status.UNDECIDED);
		
//		MosesService.getInstance().startActivity(UserStudyStatusBarHelper.generateIntentForNotification(TestResponseGenerator.FAKE_NOTIFICATION_APK_ID, MosesService.getInstance()));
		
//		while(UserstudyNotificationManager.getInstance().getNotificationForApkId(FAKE_NOTIFICATION_ID) == null) {
//			Thread.sleep(100);
//		}
//		Log.d("TEST", "woke up");
		
		//queue "later" click
//		boolean canProceed = false;
//		while(!canProceed) {
//			synchronized(ViewUserStudyActivity.autoActions) {
//				canProceed = ViewUserStudyActivity.autoActions.size() == 0;
//			}
//		}
		
	}
	
	private static void myWait(int t) {
		long t0 = System.currentTimeMillis();
		while(System.currentTimeMillis()-t0 < t);
	}
	
	@UiThreadTest
	public void testAPKDownload() throws Throwable {
		
	}
	
	
}
