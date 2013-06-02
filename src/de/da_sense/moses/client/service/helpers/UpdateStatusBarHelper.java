package de.da_sense.moses.client.service.helpers;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import de.da_sense.moses.client.R;
import de.da_sense.moses.client.WelcomeActivity;
import de.da_sense.moses.client.abstraction.apks.InstalledExternalApplication;
import de.da_sense.moses.client.abstraction.apks.InstalledExternalApplicationsManager;
import de.da_sense.moses.client.service.MosesService;
import de.da_sense.moses.client.util.Log;

/**
 * Class to show notifications for an available update for an app.
 * 
 * @author Wladimir Schmidt
 */
public class UpdateStatusBarHelper {

	/**
	 * displays an android status bar notification for an incoming update. There
	 * must be an user study notification object already in the
	 * {@link InstalledExternalApplicationsManager}, from which it will be
	 * retrieved using the given id.
	 * 
	 * @param apkId
	 *            the id of the updated app
	 * @param context
	 *            a context object for displaying the notification
	 */
	public static void displayStatusBarNotification(String apkId, Context context) {
		Log.i("MoSeS.Service", "saving user study notification to the manager");
		if (InstalledExternalApplicationsManager.getInstance() != null) {
			InstalledExternalApplication app = InstalledExternalApplicationsManager.getInstance().getAppForId(apkId);

			Intent intent = generateIntentForNotification(app.getID(), MosesService.getInstance());
			Log.i("MoSeS.Service", "starting intent to display update notification");
			showNotificationStatic(intent, apkId, app, context);

		} else {
			Log.e("MoSeS.Service",
					"cannot display update notification because user notification manager could not be initialized.");
		}
	}

	/**
	 * Creates a new Intent for the Notification.
	 * @param id the apkid for which an update is available
	 * @param context the context
	 * @return the created intent
	 */
	private static Intent generateIntentForNotification(String id, Context context) {
		Intent intent = new Intent(context, WelcomeActivity.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		intent.putExtra(WelcomeActivity.EXTRA_UPDATE_APK_ID, id);
		return intent;
	}

	/**
	 * Shows an update notification in the Android statusbar.
	 * @param intent
	 * @param apkId
	 * @param app
	 * @param context
	 */
	private static void showNotificationStatic(Intent intent, String apkId, InstalledExternalApplication app,
			Context context) {
		Log.i("MoSeS.USERSTUDY", "try to display update notification in taskbar");
		showNotificationStatic(intent, 
							   context.getString(R.string.appUpdateAvailable, app.getName()), 
							   context.getString(R.string.app_name), 
							   false,
							   notificationManagerIdForApkId(apkId), 
							   context);
	}

	/**
	 * Creates a hash code for the apkid for a user study notification.
	 * @param apkId the apps apkid
	 * @return the hash code
	 */
	private static int notificationManagerIdForApkId(String apkId) {
		return Math.abs(("Update" + apkId).hashCode());
	}

	/**
	 * Shows a notification in the status bar.
	 * @param intent
	 * @param text
	 * @param title
	 * @param ongoing
	 * @param id
	 * @param context
	 */
	private static void showNotificationStatic(Intent intent, String text, String title, boolean ongoing, int id,
			Context context) {

		Notification notification = new Notification(R.drawable.ic_launcher, text, System.currentTimeMillis());
		if (ongoing) {
			notification.flags = Notification.FLAG_ONGOING_EVENT;
		} else {
			notification.flags = Notification.FLAG_AUTO_CANCEL;
		}
		PendingIntent contentIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

		notification.setLatestEventInfo(context, title, text, contentIntent);
		NotificationManager mNotificationManager = (NotificationManager) context
				.getSystemService(Context.NOTIFICATION_SERVICE);
		mNotificationManager.notify(id, notification);
	}

}
