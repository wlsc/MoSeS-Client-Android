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

import moses.client.abstraction.ApkMethods;
import moses.client.util.FileLocationUtil;
import android.app.Activity;
import android.content.Context;

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
		defaultInstance = loadAppDatabase(appContext);
	}

	/**
	 * returns the default manager. if the manager was not initialized once,
	 * this returns null.
	 * 
	 * @return the default manager or null, if not initialized.
	 */
	public static InstalledExternalApplicationsManager getDefault() {
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
	 * Installs an application and adds its reference to this manager
	 * 
	 * @param apk
	 *            the apk file
	 * @param ID
	 *            the moses ID
	 * @param baseActivity
	 * @throws IllegalArgumentException
	 *             if the apk file does not exist
	 * @throws IOException
	 *             if parsing the apk file for the package name did not succeed
	 * @throws NameNotFoundException
	 *             if the apk file was not installed correctly
	 */
	public void installAndManageExternalApplication(File apk, String ID, Activity baseActivity)
		throws IllegalArgumentException, IOException {
		ApkMethods.installApk(apk, baseActivity);
		String packageName = ApkMethods.getPackageNameFromApk(apk, baseActivity);
		addExternalApplication(new InstalledExternalApplication(packageName, ID));
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
	public static InstalledExternalApplicationsManager loadAppDatabase(Context context) throws IOException {
		File settingsFile = FileLocationUtil.getAppDatabaseFile(context);
		InstalledExternalApplicationsManager manager = new InstalledExternalApplicationsManager();
		if (settingsFile.exists()) {
			FileReader reader = null;
			BufferedReader bufReader = null;
			try {
				reader = new FileReader(settingsFile);
				bufReader = new BufferedReader(reader);
				String line;
				while ((line = bufReader.readLine()) != null) {
					if (!line.trim().equals("")) {
						InstalledExternalApplication appRef = InstalledExternalApplication.fromOnelineString(line);
						manager.addExternalApplication(appRef);
					}
				}
				return manager;
			} catch (FileNotFoundException e) {
				e.printStackTrace();
				return new InstalledExternalApplicationsManager();
			} catch (IOException e) {
				throw e;
			} finally {
				if (reader != null) {
					if (bufReader != null) bufReader.close();
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
