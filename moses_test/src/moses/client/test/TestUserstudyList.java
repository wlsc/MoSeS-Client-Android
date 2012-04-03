package moses.client.test;

import de.da_sense.moses.client.ViewUserStudyActivity;
import de.da_sense.moses.client.ViewUserStudyNotificationsList;
import de.da_sense.moses.client.com.NetworkJSON;
import de.da_sense.moses.client.service.MosesService;
import de.da_sense.moses.client.userstudy.UserStudyNotification;
import de.da_sense.moses.client.userstudy.UserstudyNotificationManager;
import de.da_sense.moses.client.userstudy.UserStudyNotification.Status;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.test.ActivityInstrumentationTestCase2;
import android.util.Log;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;
import static moses.client.test.TestHelpers.*;

public class TestUserstudyList extends
		ActivityInstrumentationTestCase2<ViewUserStudyNotificationsList> {

	private static final int OFFSET_CLICK = 3;
	private TestResponseGenerator r = null;
	private UserStudyNotification notification;

	private static void d(String s) {
		Log.d("TEST.USERSTUDYLIST", s);
	}

	private static void d(int i) {
		Log.d("TEST.USERSTUDYLIST", "" + i);
	}

	public TestUserstudyList() {
		// initialization for notification test
		super(ViewUserStudyNotificationsList.class);
	}

	protected void setUp() throws Exception {
		super.setUp();
		setActivityInitialTouchMode(false);

		NetworkJSON.debug = true;
		r = new TestResponseGenerator();
		NetworkJSON.response = r;
		SharedPreferences.Editor e = PreferenceManager
				.getDefaultSharedPreferences(getActivity()).edit();
		e.putString("username_pref", "alex");
		e.putString("password_pref", "777");
		e.putString("deviceid_pref", "someid");
		e.putBoolean("splashscreen_pref", false);
		e.putBoolean("firststart", false);
		e.commit();

		getActivity().startService(
				new Intent(getActivity(), MosesService.class));

		UserstudyNotificationManager.init(getActivity());
		notification = new UserStudyNotification(TestResponseGenerator.userstudyApp);
		UserstudyNotificationManager.getInstance().addNotification(
				notification);
		UserstudyNotificationManager.getInstance().saveToDisk(getActivity());
	}

	/**
	 * Selects the user study from the TestResponseGenerator from the user study
	 * list and declines it in the following dialog
	 * 
	 * @throws Throwable
	 */
	public void testDenyNotificationFromList() throws Throwable {
		assertTrue(0 < UserstudyNotificationManager.getInstance()
				.getNotifications().size());
		assertNotNull(UserstudyNotificationManager.getInstance()
				.getNotificationForApkId(
						TestResponseGenerator.FAKE_NOTIFICATION_APK_ID));

		// refresh ui for the new userstudy content
		runTestOnUiThread(new Runnable() {
			@Override
			public void run() {
				getInstrumentation().callActivityOnCreate(getActivity(),
						new Bundle());

			}
		});
		myWait(1000); // settle in..

		// check the UI state of the list
		final int[] locationArrayFirstChild = new int[2];
		runTestOnUiThread(new Runnable() {
			@Override
			public void run() {
				// check that the list in the UI has the right number of
				// children and is not null
				ListView listView = getActivity().getListView();
				assertNotNull(listView);
				assertTrue(1 == listView.getChildCount());

				// retrieve the position of the first child
				View firstChild = listView.getChildAt(0);
				assertNotNull(firstChild);
				firstChild.getLocationOnScreen(locationArrayFirstChild);
				
				//check if name and description match
				checkNameAndDescriptionOfListItem(firstChild, notification);
			}
		});
		myWait(1000); // settle in..

		// automatic action for this user study: deny
		ViewUserStudyActivity.autoActions.add(Status.DENIED);
		// now click the item
		Log.d("TEST.USERSTUDYLIST", "Send touch event to first item in list");
		clickPositionOnScreen(locationArrayFirstChild[0] + OFFSET_CLICK,
				locationArrayFirstChild[1] + OFFSET_CLICK, getInstrumentation());
		myWait(1000); // settle in..

		//userstudies that are denied have to be removed directly from the manager
		assertTrue(0 == UserstudyNotificationManager.getInstance().getNotifications().size());
		
		// check the UI state of the list
		runTestOnUiThread(new Runnable() {
			@Override
			public void run() {
				// check that the list in the UI has the right number of
				// children and is not null
				ListView listView = getActivity().getListView();
				assertNotNull(listView);
				assertTrue(0 == listView.getChildCount());
			}
		});

		myWait(2000);
		// getInstrumentation().
	}
	
	/**
	 * to be called from an ui thread!
	 * 
	 * @param listRow the view that is root to a row of the userstudies list
	 * @param notification the notification to check against
	 */
	protected void checkNameAndDescriptionOfListItem(View listRow,
			UserStudyNotification notification) {
		TextView nameTextView = (TextView) listRow.findViewById(de.da_sense.moses.client.R.id.userstudylistitemtext);
		TextView descriptionTextView = (TextView) listRow.findViewById(de.da_sense.moses.client.R.id.userstudylistitemdescription);
		
		assertNotNull(nameTextView);
		assertNotNull(descriptionTextView);
		
		assertEquals(notification.getApplication().getName(), nameTextView.getText());
		assertEquals(notification.getApplication().getDescription(), descriptionTextView.getText());
	}

	/**
	 * Selects the user study from the TestResponseGenerator from the user study
	 * list and decides to delay it further ("Later") in the following dialog
	 * 
	 * @throws Throwable
	 */
	public void testFurtherDelayNotificationFromList() throws Throwable {
		assertTrue(0 < UserstudyNotificationManager.getInstance()
				.getNotifications().size());
		assertNotNull(UserstudyNotificationManager.getInstance()
				.getNotificationForApkId(
						TestResponseGenerator.FAKE_NOTIFICATION_APK_ID));

		// refresh ui for the new userstudy content
		runTestOnUiThread(new Runnable() {
			@Override
			public void run() {
				getInstrumentation().callActivityOnCreate(getActivity(),
						new Bundle());

			}
		});
		myWait(1000); // settle in..

		// check the UI state of the list
		final int[] locationArrayFirstChild = new int[2];
		runTestOnUiThread(new Runnable() {
			@Override
			public void run() {
				// check that the list in the UI has the right number of
				// children and is not null
				ListView listView = getActivity().getListView();
				assertNotNull(listView);
				assertTrue(1 == listView.getChildCount());

				// retrieve the position of the first child
				View firstChild = listView.getChildAt(0);
				assertNotNull(firstChild);
				firstChild.getLocationOnScreen(locationArrayFirstChild);
				
				//check if name and description match
				checkNameAndDescriptionOfListItem(firstChild, notification);
			}
		});
		myWait(1000); // settle in..

		// automatic action for this user study: deny
		ViewUserStudyActivity.autoActions.add(Status.UNDECIDED);
		// now click the item
		Log.d("TEST.USERSTUDYLIST", "Send touch event to first item in list");
		clickPositionOnScreen(locationArrayFirstChild[0] + OFFSET_CLICK,
				locationArrayFirstChild[1] + OFFSET_CLICK, getInstrumentation());
		myWait(1000); // settle in..

		//userstudies that are denied have to be removed directly from the manager
		assertTrue(1 == UserstudyNotificationManager.getInstance().getNotifications().size());
		UserStudyNotification notificationForApkId = UserstudyNotificationManager.getInstance().getNotificationForApkId(TestResponseGenerator.FAKE_NOTIFICATION_APK_ID);
		assertNotNull(notificationForApkId);
		assertEquals(Status.UNDECIDED, notificationForApkId.getStatus());
		
		// check the UI state of the list
		runTestOnUiThread(new Runnable() {
			@Override
			public void run() {
				// check that the list in the UI has the right number of
				// children and is not null
				ListView listView = getActivity().getListView();
				assertNotNull(listView);
				assertTrue(1 == listView.getChildCount());
				
				// retrieve the position of the first child
				View firstChild = listView.getChildAt(0);
				assertNotNull(firstChild);
				firstChild.getLocationOnScreen(locationArrayFirstChild);
				
				//check if name and description match
				checkNameAndDescriptionOfListItem(firstChild, notification);
			}
		});
	}

}
