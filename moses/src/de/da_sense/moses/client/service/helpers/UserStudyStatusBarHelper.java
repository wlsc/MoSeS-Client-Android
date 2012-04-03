package de.da_sense.moses.client.service.helpers;

import de.da_sense.moses.client.MosesActivity;
import de.da_sense.moses.client.ViewUserStudyActivity;
import de.da_sense.moses.client.service.MosesService;
import de.da_sense.moses.client.userstudy.UserStudyNotification;
import de.da_sense.moses.client.userstudy.UserstudyNotificationManager;

import de.da_sense.moses.client.R;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class UserStudyStatusBarHelper {

	/**
	 * displays an android status bar notification for an incoming user study notification.
	 * There must be an user study notification object already in the {@link UserstudyNotificationManager},
	 * from which it will be retrieved using the given id.
	 * 
	 * @param apkId the id of the user study notification
	 * @param context a context object for displaying the notification
	 */
	public static void displayStatusBarNotification(String apkId, Context context) {
		Log.i("MoSeS.Service", "saving user study notification to the manager");
		if(UserstudyNotificationManager.getInstance() != null) {
			UserStudyNotification notification = UserstudyNotificationManager.getInstance().getNotificationForApkId(apkId);

			Intent intent = generateIntentForNotification(notification.getApplication().getID(), MosesService.getInstance());
			Log.i("MoSeS.Service",
					"starting intent to display user study notification");
			showNotificationStatic(intent, apkId, context);
			
		} else {
			Log.e("MoSeS.Service",
					"cannot display user study notification because user notification manager could not be initialized.");
		}
	}
	
	public static Intent generateIntentForNotification(String id,
			Context context) {
		Intent intent = new Intent(context, MosesActivity.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		intent.putExtra(ViewUserStudyActivity.EXTRA_USER_STUDY_APK_ID, id);
		return intent;
	}

	protected static void showNotificationStatic(Intent intent, String apkId, Context context) {
		Log.i("MoSeS.USERSTUDY", "displayed user study notification in taskbar");
		showNotificationStatic(intent, "A new user study is available for you\nClick here to view it", "MoSeS",
			false, notificationManagerIdForApkId(apkId), context);
	}

	public static int notificationManagerIdForApkId(String apkId) {
		return Math.abs(("Userstudy"+apkId).hashCode());
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

}
