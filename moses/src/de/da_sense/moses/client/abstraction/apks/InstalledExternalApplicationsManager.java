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
import de.da_sense.moses.client.service.MosesService;
import de.da_sense.moses.client.service.helpers.UpdateStatusBarHelper;
import de.da_sense.moses.client.util.FileLocationUtil;
import de.da_sense.moses.client.util.Log;

/**
 * An InstalledExternalApplicationsManager holds references to installed
 * external applications. Should be initialized once at the start of the
 * application.
 * 
 * @author Simon L
 * 
 */
public class InstalledExternalApplicationsManager {
	/**
	 * List of InstalledExternalApplication
	 */
	private List<InstalledExternalApplication> apps;
	
	/**
	 * The one and only instance
	 */
	private static InstalledExternalApplicationsManager defaultInstance;
	
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
	public static InstalledExternalApplicationsManager getInstance() {
		return defaultInstance;
	}

	/**
	 * Creates an empty application manager
	 */
	public InstalledExternalApplicationsManager() {
		this(new LinkedList<InstalledExternalApplication>());
	}

	/**
	 * Creates an external application manager which already holds all the
	 * references that are given as parameter here
	 * 
	 * @param packageNames
	 *            the package names
	 */
	private InstalledExternalApplicationsManager(List<InstalledExternalApplication> externalApps) {
		this.apps = new LinkedList<InstalledExternalApplication>();
		for (InstalledExternalApplication app : externalApps) {
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
	public void addExternalApplication(InstalledExternalApplication app) {
		if (!checkAndAddHistory(app)) {
			if (!containsApp(app)) {
				apps.add(app);
			} else {
				for (Iterator<InstalledExternalApplication> iterator = apps.iterator(); iterator.hasNext();) {
					InstalledExternalApplication currentApp = iterator.next();
					if (currentApp.getPackageName().equals(app.getPackageName()) || currentApp.getID().equals(app.getID())) {
						iterator.remove();
					}
				}
				apps.add(app);
			}
		} else {
			// app should have already been added to the History App Manager, so do nothing here
		}
	}
	
	/**
	 * Checks if an app matches the constraints for a history app. If it matches 
	 * it adds the app to the {@link HistoryExternalApplicationsManager} and returns true.
	 * @param app the app to check
	 * @return true if it's a history app
	 */
	public boolean checkAndAddHistory(InstalledExternalApplication app) {
		Log.d("IEA", "checking if app is actually a history app");
		// get History Manager
		if (HistoryExternalApplicationsManager.getInstance() == null) {
			HistoryExternalApplicationsManager.init(MosesService.getInstance());
		}
		HistoryExternalApplicationsManager historyManager = HistoryExternalApplicationsManager.getInstance();
		
		// check history constraints:
		// - end date exceeded and 
		// - questionnaire sent (if it has one)
		// if the end date of a user study is null => endDateReached = true
        boolean endDateReached = app.getEndDateReached();//(app.isEndDateSet()) ? (app.getEndDate().compareTo(new Date()) < 0) : false;
        Log.d("IEA", "endDateReached = " + endDateReached);
		boolean questionnaireFinished = 
				app.hasQuestionnaire() ? app.getSurvey().hasBeenSent() : false;
		Log.d("IEA", "endDateReached? " + endDateReached + " questionnaireFinished? " + questionnaireFinished);
		if (endDateReached && questionnaireFinished) {
			Log.d("IEA", "Constraints for History US match: adding to HistoryAppManager!");
			HistoryExternalApplication histApp = 
					new HistoryExternalApplication(app, questionnaireFinished, endDateReached);
			historyManager.addExternalApplication(histApp);
			return true;
		}
		return false;
	}

	/**
	 * Returns whether there is a app with the given package name in this
	 * manager or not
	 * 
	 * @param packageName
	 *            the name of a package
	 */
	private boolean containsApp(String packageName) {
		for (InstalledExternalApplication app : apps) {
			if (app.getPackageName().equals(packageName))
				return true;
		}
		return false;
	}

	/**
	 * Gets the App corresponding to the id. If none is found,
	 * null is returned.
	 * @param id The ID of the app to return
	 * @return The corresponding app
	 */
	public InstalledExternalApplication getAppForId(String id) {
		for (InstalledExternalApplication app : apps) {
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
	private void updateApp(InstalledExternalApplication app) {
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
		InstalledExternalApplication app = getAppForId(id);
		forgetExternalApplication(app);
	}

	/**
	 * Returns containsApp(app.getPackageName()); (see
	 * {@link #containsApp(String)})
	 * 
	 */
	private boolean containsApp(InstalledExternalApplication app) {
		return containsApp(app.getPackageName()) || containsAppForId(app.getID());
	}

	/**
	 * @return a list with all managed app references
	 */
	public LinkedList<InstalledExternalApplication> getApps() {
		return new LinkedList<InstalledExternalApplication>(apps);
	}

	/**
	 * forgets the existence of a specified external application (does not
	 * uninstall or sth. else)
	 * 
	 * @param app
	 *            the reference to the application
	 */
	public void forgetExternalApplication(InstalledExternalApplication app) {
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
			writer = new FileWriter(FileLocationUtil.getAppDatabaseFile(appContext));
			bufWriter = new BufferedWriter(writer);
			bufWriter.append("version = " + managerVersion + "\n");
			for (InstalledExternalApplication app : apps) {
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
	 * loads an InstalledExternalApplicationsManager from the standard file (see
	 * Util.getAppdatabaseFile). If this file does not exist, this method
	 * returns a manager with zero initial references.
	 * 
	 * @param context
	 * @return the loaded manager
	 * @throws IOException
	 */
	private static InstalledExternalApplicationsManager loadAppDatabase(Context context) {
		File settingsFile = FileLocationUtil.getAppDatabaseFile(context);
		InstalledExternalApplicationsManager manager = new InstalledExternalApplicationsManager();
	
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
							"Initialized empty installed apps manager because the manager savefile was empty");
					return new InstalledExternalApplicationsManager();
				}
				if (fileVersion == -1 || fileVersion != managerVersion) {
					// versions are incompatible, so return empty manager.
					Log.i("MoSeS.APK", "Initialized empty installed apps manager because of version mismatch");
					return new InstalledExternalApplicationsManager();
				}

				while ((line = bufReader.readLine()) != null) {
					if (!line.trim().equals("")) {
						Log.d("InstalledExternalApplicationManager", line);
						// first create ExternalApplication
						String[] split = line.split(InstalledExternalApplication.SEPARATOR);
						ExternalApplication exApp = new ExternalApplication(split[0]);
						InstalledExternalApplication appRef = new InstalledExternalApplication(split[1], exApp, Boolean.parseBoolean(split[2]), split[3], Boolean.parseBoolean(split[4]));
						manager.addExternalApplication(appRef);
					}
				}
				Log.i("MoSeS.APK", "Loaded " + manager.getApps().size() + " installed apps from file");
				return manager;
			} catch (FileNotFoundException e) {
				return new InstalledExternalApplicationsManager();
			} catch (IOException e) {
				return new InstalledExternalApplicationsManager();
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
			return new InstalledExternalApplicationsManager();
		}
	}

	@Override
	public String toString() {
		return apps.toString();
	}

	/**
	 * Clears he list of all known apps.
	 */
	@Deprecated
	public void reset() {
		apps.clear();
	}

	public static void updateArrived(String apkidString) {
		if (getInstance() == null) {
			if (MosesService.getInstance() != null) {
				init(MosesService.getInstance());
			} else {
				Log.e("MoSeS.Update",
						"Could not initialize Installed External Application Manager because of dead MoSeS Service, and such not save the incoming update for "
								+ apkidString);
			}
		}
		if (getInstance() != null) {
			InstalledExternalApplicationsManager m = getInstance();
			if (m.containsAppForId(apkidString)) {
				InstalledExternalApplication app = m.getAppForId(apkidString);
				app.setUpdateAvailable(true);
				m.updateApp(app);
				try {
					m.saveToDisk(MosesService.getInstance());
				} catch (IOException e) {
					Log.e("MoSeS.UPDATE", "Could not save manager with new update data to file", e);
				}

				if (MosesService.getInstance() != null) {
					UpdateStatusBarHelper.displayStatusBarNotification(app.getID(), MosesService.getInstance());
				} else {
					Log.e("MoSeS.UPDATE", "MoSesService is dead, so no notification about the update could be shown");
				}
			} else {
				Log.w("MoSeS.UPDATE",
						"The app for which an update arrived is not installed; update will be forgotten. apkid: "
								+ apkidString);
			}
		} else {
			Log.e("MoSeS.UPDATE", "Could not save incoming update to manager");
		}
	}
}
