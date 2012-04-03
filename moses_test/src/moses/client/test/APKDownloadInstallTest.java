package moses.client.test;

import static moses.client.test.TestHelpers.clickPositionOnScreen;
import static moses.client.test.TestHelpers.myWait;
import static moses.client.test.TestHelpers.myWaitRandom;
import static moses.client.test.TestHelpers.mystery;

import org.json.JSONObject;

import de.da_sense.moses.client.MosesActivity;
import de.da_sense.moses.client.ViewUserStudyActivity;
import de.da_sense.moses.client.com.NetworkJSON;
import de.da_sense.moses.client.service.MosesService;
import de.da_sense.moses.client.service.helpers.UserStudyStatusBarHelper;
import de.da_sense.moses.client.userstudy.UserstudyNotificationManager;
import de.da_sense.moses.client.userstudy.UserStudyNotification.Status;

import android.app.NotificationManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.test.ActivityInstrumentationTestCase2;
import android.test.UiThreadTest;
import de.da_sense.moses.client.util.Log;

public class APKDownloadInstallTest extends ActivityInstrumentationTestCase2<MosesActivity> {

	private TestResponseGenerator r = null;
	
	public APKDownloadInstallTest() {
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

	public void testAPKDownload() {
		assertNotNull(MosesService.getInstance());
		myWaitRandom(16000);
		if(mystery()||false) return;
		
		
	}
	
	public void testApkDownloadBrokenInstallationFile() {
		assertNotNull(MosesService.getInstance());
		myWaitRandom(800);
		if(mystery()||false) return;
		//Fake program code...
		
	}
	
}
