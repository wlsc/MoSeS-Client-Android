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
package de.da_sense.moses.client.abstraction.apks;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import android.content.Context;
import de.da_sense.moses.client.util.FileLocationUtil;
import de.da_sense.moses.client.util.Log;

/**
 * An HistoryExternalApplicationsManager holds references to past user studies as
 * external applications. Should be initialized once at the start of the
 * application.
 * 
 * @author Sandra Amend
 * 
 */
public class HistoryExternalApplicationsManager {
	/**
	 * List of InstalledExternalApplication
	 */
	private List<HistoryExternalApplication> apps;
	
	/**
	 * The one and only instance
	 */
	private static HistoryExternalApplicationsManager defaultInstance;
	
	/**
	 * Version
	 */
	private static int managerVersion = 9;

	/**
	 * initializes the manager (if there is a file that contains an old manager,
	 * this will be loaded; otherwise, an empty manager will be created.
	 * 
	 * @param appContext
	 *            the application context
	 */
	public static void init(Context appContext) {
		defaultInstance = loadAppDatabase(appContext);
	}

	/**
	 * returns the default manager. if the manager was not initialized once,
	 * this returns null.
	 * 
	 * @return the default manager or null, if not initialized.
	 */
	public static HistoryExternalApplicationsManager getInstance() {
		return defaultInstance;
	}

	/**
	 * Creates an empty application manager
	 */
	public HistoryExternalApplicationsManager() {
		this(new LinkedList<HistoryExternalApplication>());
	}

	/**
	 * Creates an external application manager which already holds all the
	 * references that are given as parameter here
	 * 
	 * @param packageNames
	 *            the package names
	 */
	private HistoryExternalApplicationsManager(List<HistoryExternalApplication> externalApps) {
		this.apps = new LinkedList<HistoryExternalApplication>();
		for (HistoryExternalApplication app : externalApps) {
			addExternalApplication(app);
		}
	}

	/**
	 * Add an installed application to this manager, if it was not already added
	 * (In this case, the existing object will be removed before adding this
	 * one).
	 * 
	 * @param app
	 *            the reference object to the application
	 */
	public void addExternalApplication(HistoryExternalApplication app) {
		if (!containsApp(app)) {
			apps.add(app);
		} else {
			for (Iterator<HistoryExternalApplication> iterator = apps.iterator(); iterator.hasNext();) {
				HistoryExternalApplication currentApp = iterator.next();
				if (currentApp.getID().equals(app.getID())) {
					iterator.remove();
				}
			}
			apps.add(app);
		}
	}

	/**
	 * Returns whether there is a app with the given package name in this
	 * manager or not
	 * 
	 * @param packageName
	 *            the name of a package
	 */
	public boolean containsApp(ExternalApplication exapp) {
		for (HistoryExternalApplication app : apps) {
			if (app != null) {
				//Log.d("HistoryExternalApplicationsManager", "External app is not Null and ID is: " + app.getID());
				if (app.getID().equals(exapp.getID()) && 
						app.getDescription().equals(exapp.getDescription())) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * Gets the App corresponding to the id. If none is found,
	 * null is returned.
	 * @param id The ID of the app to return
	 * @return The corresponding app
	 */
	public HistoryExternalApplication getAppForId(String id) {
		for (HistoryExternalApplication app : apps) {
			if (app.getID().equals(id))
				return app;
		}
		return null;
	}

	/**
	 * Checks if the Manager contains the app corresponding to
	 * the id. True if so, else false.
	 * @param id
	 * @return
	 */
	private boolean containsAppForId(String id) {
		return getAppForId(id) != null;
	}

	/**
	 * If the manager contains an App with the same id as
	 * app.getID() it forgets the previous App and adds the
	 * new one.
	 * @param app
	 */
	@Deprecated
	public void updateApp(HistoryExternalApplication app) {
		if (containsAppForId(app.getID())) {
			forgetExternalApplication(app.getID());
			addExternalApplication(app);
		}
	}

	/**
	 * see: {@link #forgetExternalApplication(InstalledExternalApplication)}
	 * 
	 * @param id
	 *            the id of the apop to forget
	 */
	private void forgetExternalApplication(String id) {
		HistoryExternalApplication app = getAppForId(id);
		forgetExternalApplication(app);
	}

	/**
	 * @return a list with all managed app references
	 */
	public LinkedList<HistoryExternalApplication> getApps() {
		return new LinkedList<HistoryExternalApplication>(apps);
	}

	/**
	 * forgets the existence of a specified external application (does not
	 * uninstall or sth. else)
	 * 
	 * @param app
	 *            the reference to the application
	 */
	private void forgetExternalApplication(HistoryExternalApplication app) {
		apps.remove(app);
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
			writer = new FileWriter(FileLocationUtil.getHistoryDatabaseFile(appContext));
			bufWriter = new BufferedWriter(writer);
			bufWriter.append("version = " + managerVersion + "\n");
			for (HistoryExternalApplication app : apps) {
				bufWriter.append(app.asOnelineString() + "\n");
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
	 * Loads an HistoryExternalApplicationsManager from the standard file (see
	 * Util.getAppdatabaseFile). If this file does not exist, this method
	 * returns a manager with zero initial references.
	 * 
	 * @param context
	 * @return the loaded manager
	 * @throws IOException
	 */
	private static HistoryExternalApplicationsManager loadAppDatabase(Context context) {
		File settingsFile = FileLocationUtil.getHistoryDatabaseFile(context);
		HistoryExternalApplicationsManager manager = new HistoryExternalApplicationsManager();
		if (settingsFile.exists()) {
			int fileVersion = -1;

			FileReader reader = null;
			BufferedReader bufReader = null;
			try {
				reader = new FileReader(settingsFile);
				bufReader = new BufferedReader(reader);
				String line;

				//
				if ((line = bufReader.readLine()) != null) {
					if (line.trim().matches("version = [0-9]*")) {
						line = line.trim().replace("version = ", "");
						fileVersion = Integer.parseInt(line);
					}
				} else {
					fileVersion = -1;
					// return straight here because no lines are contained
					Log.i("MoSeS.APK",
							"Initialized empty history manager because the manager savefile was empty");
					return new HistoryExternalApplicationsManager();
				}
				if (fileVersion == -1 || fileVersion != managerVersion) {
					// versions are incompatible, so return empty manager.
					Log.i("MoSeS.APK", "Initialized empty history apps manager because of version mismatch");
					return new HistoryExternalApplicationsManager();
				}

				while ((line = bufReader.readLine()) != null) {
					if (!line.trim().equals("")) {
						HistoryExternalApplication appRef = new HistoryExternalApplication(line);
						manager.addExternalApplication(appRef);
					}
				}
				Log.i("MoSeS.APK", "Loaded " + manager.getApps().size() + " history entries from file");
				return manager;
			} catch (FileNotFoundException e) {
				return new HistoryExternalApplicationsManager();
			} catch (IOException e) {
				return new HistoryExternalApplicationsManager();
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
			return new HistoryExternalApplicationsManager();
		}
	}

	@Override
	public String toString() {
		return apps.toString();
	}

	/**
	 * Clears the list of all known apps.
	 */
	@Deprecated
	public void reset() {
		apps.clear();
	}
}
