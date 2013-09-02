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
package de.da_sense.moses.client.util;

import java.io.File;

import android.content.Context;
import android.content.ContextWrapper;

/**
 * Class to manage the file and folder locations.
 */
public class FileLocationUtil {
	/** the filename for the user study notification database */
	private static final String USERSTUDY_NOTIFICATION_DB_FILENAME = "userstudyNotifications";
	/** the filename for the installed apps */
	private static final String APPDATABASE_FILENAME = "installedApps";
	/** the filename for the local history of the participated user studies */
	private static final String HISTORYDATABASE_FILENAME = "localHistory";

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
	private static File getSettingsFolder(Context privateContext) {
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
