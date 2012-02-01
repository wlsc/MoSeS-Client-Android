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

import moses.client.ViewUserStudiesActivity;
import moses.client.abstraction.apks.ExternalApplication;
import moses.client.service.MosesService;
import moses.client.util.FileLocationUtil;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

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
	public static void init(Context appContext) throws IOException {
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
	 * @throws IOException
	 */
	public static UserstudyNotificationManager loadInstance(Context context) throws IOException {
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
				e.printStackTrace();
				return new UserstudyNotificationManager();
			} catch (IOException e) {
				throw e;
			} finally {
				if (reader != null) {
					if (bufReader != null) bufReader.close();
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

	public void addNotification(UserStudyNotification notification) {
		if(getNotificationForApkId(notification.getApplication().getID())!= null) {
			removeNotificationWithApkId(notification.getApplication().getID());
			notifications.add(notification);
		} else {
			notifications.add(notification);
		}
		//TODO: notify view?
	}

	private void removeNotificationWithApkId(String id) {
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

	public UserStudyNotification getNotificationForApkId(String userstudyId) {
		for(UserStudyNotification notification: notifications) {
			if(notification.getApplication().getID().equals(userstudyId)) {
				return notification; 
			}
		}
		return null;
	}
	
	
	public static void userStudyNotificationArrived(String apkId) {
		handleUserStudyNotificationFor(apkId);
	}
	
	private static void handleUserStudyNotificationFor(String apkId) {
		Log.i("MoSeS.Service", "saving user study notification to the manager");
		if (UserstudyNotificationManager.getInstance() == null) {
			try {
				UserstudyNotificationManager.init(MosesService.getInstance().getApplicationContext());
			} catch (IOException e) {
				e.printStackTrace();
			}
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
			MosesService.getInstance().startActivity(intent);
		} else {
			Log.e("MoSeS.Service",
					"cannot display user study notification because user notification manager could not be initialized.");
		}
	}
}
