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
package de.da_sense.moses.client.userstudy;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.preference.PreferenceManager;
import de.da_sense.moses.client.R;
import de.da_sense.moses.client.ViewUserStudyActivity;
import de.da_sense.moses.client.WelcomeActivity;
import de.da_sense.moses.client.abstraction.apks.ExternalApplication;
import de.da_sense.moses.client.abstraction.apks.InstalledExternalApplicationsManager;
import de.da_sense.moses.client.preferences.MosesPreferences;
import de.da_sense.moses.client.service.MosesService;
import de.da_sense.moses.client.service.helpers.UserStudyStatusBarHelper;
import de.da_sense.moses.client.util.FileLocationUtil;
import de.da_sense.moses.client.util.Log;

/**
 * Manages pending user studies and their persistence
 * 
 * @author Simon L
 * @author Wladimir Schmidt
 * @author Zijad Maksuti
 * 
 */
public class UserstudyNotificationManager {
	
	private static UserstudyNotificationManager instance;
	private List<UserStudyNotification> notifications;
	private static HashMap<String, Long> userstudyArrivalTimes = new HashMap<String, Long>();

	private static final String LOG_TAG = UserstudyNotificationManager.class
			.getName();

	/**
	 * initializes the manager (if there is a file that contains an old manager,
	 * this will be loaded; otherwise, an empty manager will be created.
	 * 
	 * @param appContext
	 *            the application context
	 * @throws IOException
	 *             if something goes wrong with reading/parsing the manager
	 *             file.
	 */
	public static void init(Context appContext) {
		instance = loadInstance(appContext);
	}

	public static UserstudyNotificationManager getInstance() {
		return instance;
	}

	/**
	 * loads an InstalledExternalApplicationsManager from the standard file (see
	 * Util.getAppdatabaseFile). If this file does not exist, this method
	 * returns a manager with zero initial references.
	 * 
	 * @param context
	 * @return the loaded manager
	 */
	private static UserstudyNotificationManager loadInstance(Context context) {
		File settingsFile = FileLocationUtil
				.getNotificationDatabaseFile(context);
		UserstudyNotificationManager manager = new UserstudyNotificationManager();
		if (settingsFile.exists()) {
			FileReader reader = null;
			BufferedReader bufReader = null;
			try {
				reader = new FileReader(settingsFile);
				bufReader = new BufferedReader(reader);
				String line;
				while ((line = bufReader.readLine()) != null) {
					if (!line.trim().equals("")) {
						UserStudyNotification notification = UserStudyNotification
								.fromOnelineString(line);
						manager.addNotification(notification);
					}
				}
				return manager;
			} catch (FileNotFoundException e) {
				return new UserstudyNotificationManager();
			} catch (IOException e) {
				return new UserstudyNotificationManager();
			} finally {
				if (reader != null) {
					if (bufReader != null)
						try {
							bufReader.close();
						} catch (IOException e) {
							Log.i("MoSeS.IO", "couldn't close reader");
						}
				}
			}
		} else {
			return new UserstudyNotificationManager();
		}
	}

	public UserstudyNotificationManager() {
		this.notifications = new LinkedList<UserStudyNotification>();
	}

	/**
	 * Saves all managed references to external apps to the default file (see
	 * Util.getAppdatabaseFile)
	 * 
	 * @param appContext
	 * @throws IOException
	 *             if there went something wrong at opening or writing the file.
	 */
	public void saveToDisk(Context appContext) throws IOException {
		FileWriter writer;
		BufferedWriter bufWriter = null;
		try {
			writer = new FileWriter(
					FileLocationUtil.getNotificationDatabaseFile(appContext));
			bufWriter = new BufferedWriter(writer);
			for (UserStudyNotification notification : notifications) {
				bufWriter.append(notification.asOnelineString() + "\n");
			}
		} catch (IOException e) {
			throw e;
		} finally {
			try {
				if (bufWriter != null)
					bufWriter.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * adds a notification to this manager
	 * 
	 * @param notification
	 */
	private void addNotification(UserStudyNotification notification) {
		if (getNotificationForApkId(notification.getApplication().getID()) != null) {
			removeNotificationWithApkId(notification.getApplication().getID());
			notifications.add(notification);
		} else {
			notifications.add(notification);
		}
	}

	/**
	 * updates a notification if it's contents have changed since its
	 * registration with this manager (e. g. status change)
	 * 
	 * @param notification
	 *            the notification
	 */
	public void updateNotification(UserStudyNotification notification) {
		if (this.notifications.contains(notification)) {
			this.addNotification(notification);
		}
	}

	/**
	 * removes a notification by the id of it's external application object (see
	 * {@link UserStudyNotification#getApplication()}
	 * 
	 * @param id
	 *            the id
	 */
	public void removeNotificationWithApkId(String id) {
		UserStudyNotification notificationToRemove = null;
		for (UserStudyNotification notification : notifications) {
			if (notification.getApplication().getID().equals(id)) {
				notificationToRemove = notification;
			}
		}

		if (notificationToRemove != null) {
			notifications.remove(notificationToRemove);
		}
	}

	/**
	 * @return all notifications this manager contains
	 */
	public List<UserStudyNotification> getNotifications() {
		return new LinkedList<UserStudyNotification>(notifications);
	}

	public UserStudyNotification getNotificationForApkId(String userstudyId) {
		for (UserStudyNotification notification : notifications) {
			if (notification.getApplication().getID().equals(userstudyId)) {
				return notification;
			}
		}
		return null;
	}

	/**
	 * called when a user study has arrived (should only be called internally)
	 * 
	 * @param apkId
	 *            the id for the user study
	 */
	public static void userStudyNotificationArrived(Context context, String apkId) {
		// create a new user study object and save it to the manager
		boolean doIt = true;

		// Threshold C2DM shotgun messages
		if (userstudyArrivalTimes.containsKey(apkId)
				&& System.currentTimeMillis()
						- userstudyArrivalTimes.get(apkId) < 10000) {
			doIt = false;
		}
		userstudyArrivalTimes.put(apkId, System.currentTimeMillis());

		if (doIt) {
			if (UserstudyNotificationManager.getInstance() == null) {
				if (MosesService.getInstance() != null) {
					UserstudyNotificationManager.init(MosesService.getInstance().getApplicationContext());
				} else {
					Log.e("MoSeS.USERSTUDY",
							"Could not initialize Userstudy notification manager because of dead service");
				}
			}

			if (UserstudyNotificationManager.getInstance() != null) {
				UserStudyNotification notification = new UserStudyNotification(new ExternalApplication(Integer.valueOf(apkId)));
				UserstudyNotificationManager.getInstance().addNotification(notification);
				try {
					UserstudyNotificationManager.getInstance().saveToDisk(MosesService.getInstance().getApplicationContext());
				} catch (IOException e) {
					Log.e("MoSeS", "Error when saving user study notifications");
				}
			} else {
				Log.e("MoSeS.USERSTUDY",
						"Could not save userstudy notification to manager because the manager could not be initialized");
			}

			if(PreferenceManager.getDefaultSharedPreferences(context).getBoolean(MosesPreferences.PREF_SHOW_STATUSBAR_NOTIFICATIONS, true)){
				displayStatusBarNotificationForUserStudy(apkId);
				// blink with LED
				displayBlinkingLED(context);
			}
		}
		
	}

	/**
	 * Blinks with LED with given speed and color
	 * @param context
	 */
	public static void displayBlinkingLED(Context context) {
		NotificationManager notifManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
	    Notification notif = new Notification();
	    notif.ledARGB = Color.LTGRAY;
	    notif.flags = Notification.FLAG_SHOW_LIGHTS;
	    notif.defaults |= Notification.DEFAULT_SOUND;
	    notif.ledOnMS = 100; 
	    notif.ledOffMS = 2000; 
	    notifManager.notify(0, notif);
	}

	/**
	 * Handling a notification from a userstudy to answer its surveys
	 * 
	 * @param apkidString
	 *            the apk id as a user study id
	 */
	public static void questionnaireNotificationArrived(Context context, String apkId) {
		// create a new user study object and save it to the manager
		boolean doIt = true;

		// Threshold C2DM shotgun messages
		if (userstudyArrivalTimes.containsKey(apkId)
				&& System.currentTimeMillis()
						- userstudyArrivalTimes.get(apkId) < 10000) {
			doIt = false;
		}
		Log.i(LOG_TAG, "doIt = " + doIt);
		userstudyArrivalTimes.put(apkId, System.currentTimeMillis());

		if (doIt) {
			Log.i(LOG_TAG,
					"check UsNM = "
							+ UserstudyNotificationManager.getInstance());
			if (UserstudyNotificationManager.getInstance() == null) {
				Log.i(LOG_TAG, "check MS = " + MosesService.getInstance());
				if (MosesService.getInstance() != null) {
					Log.i(LOG_TAG, "UsNM.int ( "
							+ MosesService.getInstance()
									.getApplicationContext() + " )");
					UserstudyNotificationManager.init(MosesService
							.getInstance().getApplicationContext());
				} else {
					Log.e(LOG_TAG,
							"Could not initialize Userstudy notification manager because of dead service");
				}
			}

			if (UserstudyNotificationManager.getInstance() != null) {
				UserStudyNotification notification = new UserStudyNotification(new ExternalApplication(Integer.valueOf(apkId)));
				UserstudyNotificationManager.getInstance().addNotification(notification);
				Log.i(LOG_TAG, "try");
				try {
					UserstudyNotificationManager.getInstance().saveToDisk(MosesService.getInstance().getApplicationContext());
				} catch (IOException e) {
					Log.e("MoSeS",
							"Error when saving questionnaire notifications for ");
				}
			} else {
				Log.e("MoSeS.USERSTUDY",
						"Could not save userstudy notification to manager because the manager could not be initialized");
			}
			
			Intent intent = new Intent(MosesService.getInstance(), WelcomeActivity.class);
			intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			intent.putExtra(WelcomeActivity.KEY_VIEW_SURVEY, apkId);
			String notificationMessage = String.format(MosesService.getInstance().getString(R.string.notification_survey_available_for),
					InstalledExternalApplicationsManager.getInstance().getAppForId(apkId).getName());
			String notificationTitle = MosesService.getInstance().getString(R.string.moses_title);
			if(PreferenceManager.getDefaultSharedPreferences(context).getBoolean(MosesPreferences.PREF_SHOW_STATUSBAR_NOTIFICATIONS, true)){
				UserStudyStatusBarHelper.showNotificationStatic(intent,notificationMessage, notificationTitle, false, UserStudyStatusBarHelper.notificationManagerIdForApkId(apkId), MosesService.getInstance());
				// blink with LED
				displayBlinkingLED(context);
			}
		}

	}

	/**
	 * call this method to display a android status bar notification that a new
	 * user study has arrived
	 * 
	 * @param apkId
	 *            the id of the study
	 */
	private static void displayStatusBarNotificationForUserStudy(String apkId) {
		if (MosesService.getInstance() != null) {
			UserStudyStatusBarHelper.displayStatusBarNotification(apkId,MosesService.getInstance());
		} else {
			Log.e("MoSeS.USERSTUDY",
					"Could not display notification that new userstudy has arrived, because moses service was null");
		}
	}

	/**
	 * Initiates a dialog which displazs informations about a specified user
	 * study (must already b
	 * 
	 * @param userStudyId
	 * @param applicationContext
	 */
	public static void displayUserStudyContent(String userStudyId, Context applicationContext) {
		Intent viewUserStudy = new Intent(applicationContext, ViewUserStudyActivity.class);
		viewUserStudy.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		viewUserStudy.putExtra(ViewUserStudyActivity.EXTRA_USER_STUDY_APK_ID, userStudyId);
		applicationContext.startActivity(viewUserStudy);
	}

	public void removeNotificationById(String id) {
		for (Iterator<UserStudyNotification> iterator = notifications
				.iterator(); iterator.hasNext();) {
			UserStudyNotification us = iterator.next();
			if (us.getApplication().getID().equals(id)) {
				iterator.remove();
			}
		}
	}

}
