package moses.client.service.helpers;

import java.io.IOException;

import moses.client.MosesActivity;
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

//TODO: eliminate activity (only use static methods here)
public class NotifyAboutUserStudyActivity extends Activity {

	public static NotifyAboutUserStudyActivity staticActivityReference;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		String userstudyId = getIntent().getStringExtra(ViewUserStudiesActivity.EXTRA_USER_STUDY_APK_ID);
		displayUserStudyNotification(userstudyId);
		finish();
	}

	private void displayUserStudyNotification(String apkId) {
		displayUserStudyNotificationStatic(apkId, this);
	}
	
	public static void displayUserStudyNotificationStatic(String apkId, Context context) {
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
			showNotificationStatic(intent, apkId, context);
			
		} else {
			Log.e("MoSeS.Service",
					"cannot display user study notification because user notification manager could not be initialized.");
		}
	}
	
	protected static void showNotificationStatic(Intent intent, String apkId, Context context) {
		Log.i("MoSeS.Userstudy", "displayed user study notification in taskbar");
		showNotificationStatic(intent, "A new user study is available for you\nClick here to view it", "MoSeS",
			false, Math.abs(("Userstudy"+apkId).hashCode()), context);
	}

	private static void showNotificationStatic(Intent intent, String text, String title, boolean ongoing, int id, Context context) {

		Notification notification = new Notification(R.drawable.ic_launcher, text, System.currentTimeMillis());
		if (ongoing) {
			notification.flags = Notification.FLAG_ONGOING_EVENT;
		} else {
			notification.flags = Notification.FLAG_AUTO_CANCEL;
		}
		PendingIntent contentIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

		notification.setLatestEventInfo(context, title, text, contentIntent);
		NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(
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
