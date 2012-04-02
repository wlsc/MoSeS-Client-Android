package moses.client.service.helpers;

import java.io.IOException;

import moses.client.MosesActivity;
import moses.client.R;
import moses.client.ViewUserStudyActivity;
import moses.client.abstraction.apks.ExternalApplication;
import moses.client.abstraction.apks.InstalledExternalApplication;
import moses.client.abstraction.apks.InstalledExternalApplicationsManager;
import moses.client.service.MosesService;
import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

public class UpdateStatusBarHelper {

	/**
	 * displays an android status bar notification for an incoming update.
	 * There must be an user study notification object already in the {@link InstalledExternalApplicationsManager},
	 * from which it will be retrieved using the given id.
	 * 
	 * @param apkId the id of the updated app
	 * @param context a context object for displaying the notification
	 */
	public static void displayStatusBarNotification(String apkId, Context context) {
		Log.i("MoSeS.Service", "saving user study notification to the manager");
		if(InstalledExternalApplicationsManager.getInstance() != null) {
			InstalledExternalApplication app = InstalledExternalApplicationsManager.getInstance().getAppForId(apkId);

			Intent intent = generateIntentForNotification(app.getID(), MosesService.getInstance());
			Log.i("MoSeS.Service",
					"starting intent to display update notification");
			showNotificationStatic(intent, apkId, app, context);
			
		} else {
			Log.e("MoSeS.Service",
					"cannot display update notification because user notification manager could not be initialized.");
		}
	}
	
	public static Intent generateIntentForNotification(String id,
			Context context) {
		Intent intent = new Intent(context, MosesActivity.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		intent.putExtra(MosesActivity.EXTRA_UPDATE_APK_ID, id);
		return intent;
	}

	protected static void showNotificationStatic(Intent intent, String apkId, InstalledExternalApplication app, Context context) {
		Log.i("MoSeS.USERSTUDY", "try to display update notification in taskbar");
		showNotificationStatic(intent, "An update is available for the app \"" + app.getName() + "\".", "MoSeS",
			false, notificationManagerIdForApkId(apkId), context);
	}

	public static int notificationManagerIdForApkId(String apkId) {
		return Math.abs(("Update"+apkId).hashCode());
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
