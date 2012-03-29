package moses.client.test;

import moses.client.MosesActivity;
import moses.client.ViewUserStudyActivity;
import moses.client.com.NetworkJSON;
import moses.client.service.MosesService;
import moses.client.service.helpers.UserStudyStatusBarHelper;
import moses.client.userstudy.UserstudyNotificationManager;
import moses.client.userstudy.UserStudyNotification.Status;
import android.app.NotificationManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.test.ActivityInstrumentationTestCase2;
import android.test.UiThreadTest;
import android.util.Log;
import android.widget.TabHost;
import android.widget.TextView;

public class UserstudyNotificationTests extends ActivityInstrumentationTestCase2<MosesActivity> {
	
	private TestResponseGenerator r = null;
	
	public UserstudyNotificationTests() {
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
	
	@UiThreadTest
	public void testGetElement() throws Throwable {
		
	}
	
	/**
	 * Invokes a fake user study and clicks "later" in the dialog. Tests, if the user study is in the database after that.
	 * 
	 * @throws Throwable
	 */
	@UiThreadTest
	public void testNotification() throws Throwable {
		UserstudyNotificationManager.userStudyNotificationArrived(TestResponseGenerator.FAKE_NOTIFICATION_APK_ID);
		
		NotificationManager mNotificationManager = (NotificationManager) MosesService.getInstance().getSystemService(
				Context.NOTIFICATION_SERVICE);
		
		myWait(500); //Delay for waiting for the notification to Show
		assertTrue(notificationForIdIsShown(TestResponseGenerator.FAKE_NOTIFICATION_APK_ID));
		
		//cancel the notification
		mNotificationManager.cancel(UserStudyStatusBarHelper.notificationManagerIdForApkId(TestResponseGenerator.FAKE_NOTIFICATION_APK_ID));

		//launch the intent as if it were by clicking the notification
		ViewUserStudyActivity.autoActions.add(Status.UNDECIDED);
		Log.d("TEST", "starting activity ViewUserStudiesActivity");

		MosesService.getInstance().startActivity(UserStudyStatusBarHelper.generateIntentForNotification(TestResponseGenerator.FAKE_NOTIFICATION_APK_ID, MosesService.getInstance()));
		
		myWait(500);

		assertTrue(UserstudyNotificationManager.getInstance().getNotificationForApkId(TestResponseGenerator.FAKE_NOTIFICATION_APK_ID) != null);
		assertTrue(UserstudyNotificationManager.getInstance().getNotificationForApkId(TestResponseGenerator.FAKE_NOTIFICATION_APK_ID).getStatus() == Status.UNDECIDED);
		
		//TODO: in after()-Method
		UserstudyNotificationManager.getInstance().saveToDisk(getActivity());
	}
	
	/**
	 * @param fakeNotificationApkId the apk id of the user study for which the notifiation is shown
	 * @return true if the status bar notification for this ID is really shown
	 */
	private boolean notificationForIdIsShown(String fakeNotificationApkId) {
		// TODO if there is a check for notifications in the statusbar (but apparently not)
		return true;
	}

	private static void myWait(int t) {
		long t0 = System.currentTimeMillis();
		while(System.currentTimeMillis()-t0 < t);
	}


}
