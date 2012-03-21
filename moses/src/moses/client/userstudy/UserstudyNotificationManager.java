package moses.client.userstudy;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import moses.client.ViewUserStudyActivity;
import moses.client.abstraction.apks.ExternalApplication;
import moses.client.service.MosesService;
import moses.client.service.helpers.UserStudyStatusBarHelper;
import moses.client.util.FileLocationUtil;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * Manages pending user studies and their persistence
 * 
 * @author Simon L
 *
 */
public class UserstudyNotificationManager {
	private static UserstudyNotificationManager instance;
	private List<UserStudyNotification> notifications;
	
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
	public static UserstudyNotificationManager loadInstance(Context context) {
		File settingsFile = FileLocationUtil.getNotificationDatabaseFile(context);
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
						UserStudyNotification notification = UserStudyNotification.fromOnelineString(line);
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
					if (bufReader != null) try {
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
			writer = new FileWriter(FileLocationUtil.getAppDatabaseFile(appContext));
			bufWriter = new BufferedWriter(writer);
			for (UserStudyNotification notification : notifications) {
				bufWriter.append(notification.asOnelineString() + "\n");
			}
		} catch (IOException e) {
			throw e;
		} finally {
			try {
				if (bufWriter != null) bufWriter.close();
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
	public void addNotification(UserStudyNotification notification) {
		if(getNotificationForApkId(notification.getApplication().getID())!= null) {
			removeNotificationWithApkId(notification.getApplication().getID());
			notifications.add(notification);
		} else {
			notifications.add(notification);
		}
	}

	/**
	 * updates a notification if it's contents have changed since its registration with this manager (e. g. status change)
	 * 
	 * @param notification the notification
	 */
	public void updateNotification(UserStudyNotification notification) {
		if(this.notifications.contains(notification)) {
			this.addNotification(notification);
		}
	}

	/**
	 * removes a notification by the id of it's external application object (see {@link UserStudyNotification#getApplication()}
	 * 
	 * @param id the id
	 */
	public void removeNotificationWithApkId(String id) {
		UserStudyNotification notificationToRemove = null;
		for(UserStudyNotification notification: notifications) {
			if(notification.getApplication().getID().equals(id)) {
				notificationToRemove = notification;
			}
		}
		
		if(notificationToRemove != null) {
			notifications.remove(notificationToRemove);
		}
	}

	/**
	 * @return all notifications this manager contains
	 */
	public List<UserStudyNotification> getNotifications() {
		return new LinkedList(notifications);
	}

	public UserStudyNotification getNotificationForApkId(String userstudyId) {
		for(UserStudyNotification notification: notifications) {
			if(notification.getApplication().getID().equals(userstudyId)) {
				return notification; 
			}
		}
		return null;
	}
	
	
	/**
	 * called when a user study has arrived (should only be called internally)
	 * 
	 * @param apkId the id for the user study
	 */
	public static void userStudyNotificationArrived(String apkId) {
		//create a new user study object and save it to the manager
		
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
		}
		
		displayStatusBarNotificationForUserStudy(apkId);
	}

	/**
	 * call this method to display a android status bar notification that a new user study has arrived
	 * 
	 * @param apkId the id of the study
	 */
	public static void displayStatusBarNotificationForUserStudy(String apkId) {
		if(MosesService.getInstance() != null) {
			UserStudyStatusBarHelper.displayStatusBarNotification(apkId, MosesService.getInstance());
		} else {
			Log.e("MoSeS.Userstudy", "Could not display notification that new userstudy has arrived, because moses service was null");
		}
	}

	/**
	 * Initiates a dialog which displazs informations about a specified user study (must already b
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
	
	
}
