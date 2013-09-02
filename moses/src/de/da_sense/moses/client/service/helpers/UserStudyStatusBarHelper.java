/*******************************************************************************
 * Copyright 2013
 * Telecooperation (TK) Lab
 * Technische Universität Darmstadt
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package de.da_sense.moses.client.service.helpers;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import de.da_sense.moses.client.R;
import de.da_sense.moses.client.ViewUserStudyActivity;
import de.da_sense.moses.client.WelcomeActivity;
import de.da_sense.moses.client.service.MosesService;
import de.da_sense.moses.client.userstudy.UserStudyNotification;
import de.da_sense.moses.client.userstudy.UserstudyNotificationManager;
import de.da_sense.moses.client.util.Log;

/**
 * Class for creating and showing notifications for user studies in the status bar.
 * @author Zijad Maksuti
 */
public class UserStudyStatusBarHelper {

	/**
	 * displays an android status bar notification for an incoming user study
	 * notification. There must be n user study notification object already in
	 * the {@link UserstudyNotificationManager}, from which it will be retrieved
	 * using the given id.
	 * 
	 * @param apkId
	 *            the id of the user study notification
	 * @param context
	 *            a context object for displaying the notification
	 */
	public static void displayStatusBarNotification(String apkId, Context context) {
		Log.i("MoSeS.Service", "saving user study notification to the manager");
		if (UserstudyNotificationManager.getInstance() != null) {
			UserStudyNotification notification = UserstudyNotificationManager.getInstance().getNotificationForApkId(apkId);

			Intent intent = generateIntentForNotification(notification.getApplication().getID(), MosesService.getInstance());
			Log.i("MoSeS.Service", "starting intent to display user study notification");
			showNotificationStatic(intent, apkId, context);

		} else {
			Log.e("MoSeS.Service",
					"cannot display user study notification because user notification manager could not be initialized.");
		}
	}

	/**
	 * Creates an Intent to start the {@link ViewUserStudyActivity}.
	 * @param id apkid of the app
	 * @param context the context for the activity
	 * @return the intent
	 */
	public static Intent generateIntentForNotification(String id, Context context) {
		Intent intent = new Intent(context, WelcomeActivity.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		intent.putExtra(ViewUserStudyActivity.EXTRA_USER_STUDY_APK_ID, id);
		return intent;
	}

	/**
	 * Shows a notification in the Android statusbar.
	 * @param intent the intent for the notification
	 * @param apkId apkid of the app for the user study
	 * @param context the context
	 */
	private static void showNotificationStatic(Intent intent, String apkId, Context context) {
		Log.i("MoSeS.USERSTUDY", "displayed user study notification in taskbar");
		showNotificationStatic(intent, 
							   context.getString(R.string.userStudy_newStudyAvailable), 
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
	public static int notificationManagerIdForApkId(String apkId) {
		return Math.abs(("Userstudy" + apkId).hashCode());
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
	public static void showNotificationStatic(Intent intent, String text, 
			String title, boolean ongoing, int id, Context context) {

	    Log.i("FIXME", "showNotificationStatic("+intent+",text,"+title+","+ongoing+","+id+","+context+")");
	    
		Notification notification = new Notification(R.drawable.ic_launcher, 
				text, System.currentTimeMillis());
		if (ongoing) {
			notification.flags = Notification.FLAG_ONGOING_EVENT;
		} else {
			notification.flags = Notification.FLAG_AUTO_CANCEL;
		}
		PendingIntent contentIntent = PendingIntent.getActivity(context, 0, 
				intent, PendingIntent.FLAG_UPDATE_CURRENT);

		notification.setLatestEventInfo(context, title, text, contentIntent);
		NotificationManager mNotificationManager = (NotificationManager) context
				.getSystemService(Context.NOTIFICATION_SERVICE);
		mNotificationManager.notify(id, notification);
	}

}
