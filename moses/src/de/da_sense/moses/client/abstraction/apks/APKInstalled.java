package de.da_sense.moses.client.abstraction.apks;

import java.net.UnknownHostException;


import org.json.JSONException;

import de.da_sense.moses.client.com.ConnectionParam;
import de.da_sense.moses.client.com.ReqTaskExecutor;
import de.da_sense.moses.client.com.NetworkJSON.BackgroundException;
import de.da_sense.moses.client.com.requests.RequestInstalledAPK;
import de.da_sense.moses.client.service.MosesService;
import de.da_sense.moses.client.service.helpers.EHookTypes;
import de.da_sense.moses.client.service.helpers.EMessageTypes;
import de.da_sense.moses.client.service.helpers.Executor;

import android.util.Log;

public class APKInstalled {

	private class APKInstalledTaskExecutor implements ReqTaskExecutor {

		@Override
		public void handleException(Exception e) {
			if (e instanceof UnknownHostException || e instanceof JSONException) {
				Log.d("MoSeS.LOGIN",
						"No internet connection present (or DNS problems.)");
			} else
				Log.d("MoSeS.LOGIN", "FAILURE: " + e.getClass().toString()
						+ " " + e.getMessage());
		}

		@Override
		public void postExecution(String s) {
			Log.d("MoSeS.APK_INSTALLED", "Notified server about installed apk.");
			Log.d("MoSeS.APK_INSTALLED", s);
		}

		@Override
		public void updateExecution(BackgroundException c) {
			if (c.c != ConnectionParam.EXCEPTION) {
				// Don't care once more...
			} else {
				handleException(c.e);
			}
		}
	};

	public APKInstalled(final String appID) {
		if (MosesService.getInstance() != null)
			MosesService.getInstance().executeLoggedIn(EHookTypes.POSTLOGINSUCCESS, EMessageTypes.REQUESTINSTALLEDAPK, new Executor() {

				@Override
				public void execute() {
					new RequestInstalledAPK(new APKInstalledTaskExecutor(), MosesService
							.getInstance().getSessionID(), appID).send();
				}
			});
	}
}
