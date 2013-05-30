package de.da_sense.moses.client.util;

import java.io.File;

import android.content.Context;
import android.content.ContextWrapper;

/**
 * Class to manage the file and folder locations.
 */
public class FileLocationUtil {
	/** the filename for the user study notification database */
	public static final String USERSTUDY_NOTIFICATION_DB_FILENAME = "userstudyNotifications";
	/** the filename for the installed apps */
	public static final String APPDATABASE_FILENAME = "installedApps";
	/** the filename for the local history of the participated user studies */
	public static final String HISTORYDATABASE_FILENAME = "localHistory";

	/**
	 * Create a folder for the installed app database.
	 * @param context the context
	 * @return the folder
	 */
	public static File getApkDownloadFolder(Context privateContext) {
		File dir = new ContextWrapper(privateContext).getExternalFilesDir("apk");
		return dir;
	}

	/**
	 * Create a folder for the settings.
	 * @param context the context
	 * @return the folder
	 */
	public static File getSettingsFolder(Context privateContext) {
		File dir = new ContextWrapper(privateContext).getExternalFilesDir(null);
		return dir;
	}

	/**
	 * Create a file for the installed app database.
	 * @param context the context
	 * @return the database file
	 */
	public static File getAppDatabaseFile(Context context) {
		return new File(getSettingsFolder(context), FileLocationUtil.APPDATABASE_FILENAME);
	}
	
	/**
	 * Create a file for the history database.
	 * @param context the context
	 * @return the database file
	 */
	public static File getHistoryDatabaseFile(Context context) {
		return new File(getSettingsFolder(context), FileLocationUtil.HISTORYDATABASE_FILENAME);
	}

	/**
	 * Create a file for the user study notification database.
	 * @param context the context
	 * @return the database file
	 */
	public static File getNotificationDatabaseFile(Context context) {
		return new File(getSettingsFolder(context), USERSTUDY_NOTIFICATION_DB_FILENAME);
	}

}
