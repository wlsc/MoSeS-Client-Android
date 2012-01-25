package moses.client.util;

import java.io.File;

import android.content.Context;
import android.content.ContextWrapper;

public class FileLocationUtil {

	public static final String USERSTUDY_NOTIFICATION_DB_FILENAME = "userstudyNotifications";
	public static final String APPDATABASE_FILENAME = "installedApps";

	public static File getApkDownloadFolder(Context privateContext) {
		File dir = new ContextWrapper(privateContext).getExternalFilesDir("apk");
		return dir;
	}

	public static File getSettingsFolder(Context privateContext) {
		File dir = new ContextWrapper(privateContext).getExternalFilesDir(null);
		return dir;
	}

	public static File getAppDatabaseFile(Context context) {
		return new File(getSettingsFolder(context), FileLocationUtil.APPDATABASE_FILENAME);
	}
	
	public static File getNotificationDatabaseFile(Context context) {
		return new File(getSettingsFolder(context), USERSTUDY_NOTIFICATION_DB_FILENAME);
	}

}
