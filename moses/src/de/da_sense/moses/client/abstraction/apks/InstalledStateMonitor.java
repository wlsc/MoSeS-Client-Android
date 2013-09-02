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

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import android.content.Context;
import de.da_sense.moses.client.abstraction.ApkMethods;
import de.da_sense.moses.client.util.Log;

/**
 * Monitors installed apps whether their corresponding apps on the disk are
 * still installed and takes the appropriate actions. Singleton
 * 
 * @author Simon L
 */
public class InstalledStateMonitor {

	/**
	 * The one and only instance
	 */
	private static InstalledStateMonitor singleton;

	/**
	 * Return the default instance of the InstalledStateMonitor.
	 * @return the only instance of the InstalledStateMonitor
	 */
	public static InstalledStateMonitor getDefault() {
		if (singleton != null) {
			Log.d("InstalledStateMonitor", "InstalledStateMonitor.getDefault() != null");
			return singleton;
		} else {
			singleton = new InstalledStateMonitor();
			return singleton;
		}
	}

	/**
	 * Checks if the installed app database and the actually installed apps are
	 * consistent. 
	 * 
	 * @param context the context
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
	 * Handles the occurrence of an app that this application thinks it is
	 * installed, but it is actually not. By default, the app is forgotten, and
	 * an uninstallation notification is sent to the server.
	 * 
	 * @param inconsistentApp the inconsistent app 
	 * @param context the context
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
