package de.da_sense.moses.client.abstraction.apks;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import de.da_sense.moses.client.abstraction.ApkMethods;

import android.content.Context;
import android.util.Log;

/**
 * Monitors installed apps whether their corresponding apps on the disk are
 * still installed and takes the appropriate actions
 * 
 * @author Simon L
 * 
 */
public class InstalledStateMonitor {

	private static InstalledStateMonitor singleton;

	public static InstalledStateMonitor getDefault() {
		if (singleton != null) {
			return singleton;
		} else {
			singleton = new InstalledStateMonitor();
			return singleton;
		}
	}

	/**
	 * checks if the installed app database and the actually installed apps are
	 * consistent
	 * 
	 * @param context
	 * @return true for a consistent state, and false, if the state was
	 *         inconsistent, and was made consistent in the process.
	 */
	public boolean checkForValidState(Context context) {
		if (InstalledExternalApplicationsManager.getInstance() == null) {
			InstalledExternalApplicationsManager.init(context);
		}

		List<InstalledExternalApplication> inconsistentApps = new LinkedList<InstalledExternalApplication>();
		for (InstalledExternalApplication app : InstalledExternalApplicationsManager.getInstance().getApps()) {
			if (!ApkMethods.isApplicationInstalled(app.getPackageName(), context)) {
				inconsistentApps.add(app);
			}
		}

		for (InstalledExternalApplication inconsistentApp : inconsistentApps) {
			handleInconsistentApp(inconsistentApp, context);
		}

		return inconsistentApps.size() == 0;

	}

	/**
	 * Handles the occurence of an app that this application thinks it is
	 * installed, but it is actually not. By default, the app is forgotten, and
	 * an Uninstallation notification is sent to the server.
	 * 
	 * @param inconsistentApp
	 * @param context
	 */
	private void handleInconsistentApp(InstalledExternalApplication inconsistentApp, Context context) {
		InstalledExternalApplicationsManager.getInstance().forgetExternalApplication(inconsistentApp);
		Log.d("MoSeS.APK", "removed externally uninstalled app + " + inconsistentApp.asOnelineString());
		new ApkUninstalled(inconsistentApp.getID());
		try {
			InstalledExternalApplicationsManager.getInstance().saveToDisk(context);
		} catch (IOException e) {
			Log.e("MoSeS.APK", "could not save manager to disk @ handleInconsistentApp", e);
		}
	}

}
