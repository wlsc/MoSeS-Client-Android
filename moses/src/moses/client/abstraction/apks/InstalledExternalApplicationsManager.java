package moses.client.abstraction.apks;

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
import moses.client.util.FileLocationUtil;
import android.content.Context;
import android.util.Log;

/**
 * An InstalledExternalApplicationsManager holds references to installed
 * external applications. Should be initialized once at the start of the
 * application.
 * 
 * @author Simon L
 * 
 */
public class InstalledExternalApplicationsManager {
	private List<InstalledExternalApplication> apps;
	private static InstalledExternalApplicationsManager defaultInstance;
	private static int managerVersion = 2;

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
	public InstalledExternalApplicationsManager(List<InstalledExternalApplication> externalApps) {
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
		if (!containsApp(app)) {
			apps.add(app);
		} else {
			for (Iterator<InstalledExternalApplication> iterator = apps.iterator(); iterator.hasNext();) {
				InstalledExternalApplication currentApp = iterator.next();
				if (currentApp.getPackageName().equals(app.getPackageName())) {
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
	public boolean containsApp(String packageName) {
		for (InstalledExternalApplication app : apps) {
			if (app.getPackageName().equals(packageName)) return true;
		}
		return false;
	}

	/**
	 * Returns containsApp(app.getPackageName()); (see
	 * {@link #containsApp(String)})
	 * 
	 */
	public boolean containsApp(InstalledExternalApplication app) {
		return containsApp(app.getPackageName());
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
				if (bufWriter != null) bufWriter.close();
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
	public static InstalledExternalApplicationsManager loadAppDatabase(Context context) {
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
					Log.i("MoSeS.APK", "Initialized empty installed apps manager because the manager savefile was empty");
					return new InstalledExternalApplicationsManager();
				}
				if (fileVersion == -1 || fileVersion != managerVersion) {
					// versions are incompatible, so return empty manager.
					Log.i("MoSeS.APK", "Initialized empty installed apps manager because of version mismatch");
					return new InstalledExternalApplicationsManager();
				}

				while ((line = bufReader.readLine()) != null) {
					if (!line.trim().equals("")) {
						InstalledExternalApplication appRef = InstalledExternalApplication.fromOnelineString(line);
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
					if (bufReader != null) try {
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

	public void reset() {
		apps.clear();
	}
}
