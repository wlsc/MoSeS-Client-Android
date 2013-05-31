package de.da_sense.moses.client.abstraction.apks;

import java.net.UnknownHostException;

import org.json.JSONException;

import de.da_sense.moses.client.com.ConnectionParam;
import de.da_sense.moses.client.com.NetworkJSON.BackgroundException;
import de.da_sense.moses.client.com.ReqTaskExecutor;
import de.da_sense.moses.client.com.requests.RequestUninstalledAPK;
import de.da_sense.moses.client.service.MosesService;
import de.da_sense.moses.client.service.helpers.EHookTypes;
import de.da_sense.moses.client.service.helpers.EMessageTypes;
import de.da_sense.moses.client.service.helpers.Executable;
import de.da_sense.moses.client.util.Log;

public class ApkUninstalled {

	private class APKUninstalledTaskExecutor implements ReqTaskExecutor {

		@Override
		public void handleException(Exception e) {
			if (e instanceof UnknownHostException || e instanceof JSONException) {
				Log.d("MoSeS.LOGIN", "No internet connection present (or DNS problems.)");
			} else
				Log.d("MoSeS.LOGIN", "FAILURE: " + e.getClass().toString() + " " + e.getMessage());
		}

		@Override
		public void postExecution(String s) {
			Log.d("MoSeS.APK_INSTALLED", "Confirmation received: notified server about uninstalled apk: " + s);
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

	/**
	 * Notifies the server of an uninstalled APK.
	 * @param appID The ID of the APK
	 */
	public ApkUninstalled(final String appID) {
		// TODO: handle service == null
		if (MosesService.getInstance() != null)
			MosesService.getInstance().executeLoggedIn(EHookTypes.POST_LOGIN_SUCCESS,
					EMessageTypes.REQUESTUNINSTALLEDAPK, new Executable() {

						@Override
						public void execute() {
							Log.d("MoSeS.APK", "Sending information to server that app was uninstalled: " + appID);
							new RequestUninstalledAPK(new APKUninstalledTaskExecutor(), MosesService.getInstance()
									.getSessionID(), appID).send();
						}
					});
	}
}
