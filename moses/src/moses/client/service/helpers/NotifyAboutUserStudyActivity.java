package moses.client.service.helpers;

import java.io.IOException;

import moses.client.R;
import moses.client.ViewAvailableApkActivity;
import moses.client.ViewUserStudiesActivity;
import moses.client.abstraction.apks.ExternalApplication;
import moses.client.service.MosesService;
import moses.client.userstudy.UserStudyNotification;
import moses.client.userstudy.UserstudyNotificationManager;
import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

public class NotifyAboutUserStudyActivity extends Activity {

	public static NotifyAboutUserStudyActivity staticActivityReference;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		String userstudyId = getIntent().getStringExtra(ViewUserStudiesActivity.EXTRA_USER_STUDY_APK_ID);
		displayUserStudyNotification(userstudyId);
		finish();
	}

	public static void handleUserStudyNotificationFor(String apkId) {
		Intent intent = new Intent(MosesService.getInstance(), NotifyAboutUserStudyActivity.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		intent.putExtra(ViewUserStudiesActivity.EXTRA_USER_STUDY_APK_ID, apkId);
		MosesService.getInstance().startActivity(intent);
	}
	
	private void displayUserStudyNotification(String apkId) {
		Log.i("MoSeS.Service", "saving user study notification to the manager");
		if (UserstudyNotificationManager.getInstance() == null) {
			UserstudyNotificationManager.init(MosesService.getInstance().getApplicationContext());
		}

		if (UserstudyNotificationManager.getInstance() != null) {
			UserStudyNotification notification = new UserStudyNotification(
					new ExternalApplication(apkId));
			UserstudyNotificationManager.getInstance().addNotification(
					notification);
			try {
				UserstudyNotificationManager.getInstance().saveToDisk(
					MosesService.getInstance().getApplicationContext());
			} catch (IOException e) {
				Log.e("MoSeS", "Error when saving user study notifications");
			}

			Intent intent = new Intent(MosesService.getInstance(), ViewUserStudiesActivity.class);
			intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			intent.putExtra(ViewUserStudiesActivity.EXTRA_USER_STUDY_APK_ID,
					notification.getApplication().getID());
			Log.i("MoSeS.Service",
					"starting intent to display user study notification");
			showNotification(intent, apkId);
			
		} else {
			Log.e("MoSeS.Service",
					"cannot display user study notification because user notification manager could not be initialized.");
		}
	}
	
	protected void showNotification(Intent intent, String apkId) {
		Log.i("MoSeS.Userstudy", "diosplayed user study notification in taskbar");
		showNotification(intent, "A new user study is available for you\nClick here to view it", "MoSeS",
			false, Math.abs(("Userstudy"+apkId).hashCode()));
	}

	private void showNotification(Intent intent, String text, String title, boolean ongoing, int id) {

		Notification notification = new Notification(R.drawable.ic_launcher, text, System.currentTimeMillis());
		if (ongoing) {
			notification.flags = Notification.FLAG_ONGOING_EVENT;
		} else {
			notification.flags = Notification.FLAG_AUTO_CANCEL;
		}
		PendingIntent contentIntent = PendingIntent.getActivity(getContext(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

		notification.setLatestEventInfo(getContext(), title, text, contentIntent);
		NotificationManager mNotificationManager = (NotificationManager) getActivity().getSystemService(
			Context.NOTIFICATION_SERVICE);
		mNotificationManager.notify(id, notification);
	}

	private Activity getActivity() {
		return this;
	}

	private Context getContext() {
		return getActivity().getApplicationContext();
	}

}
