package moses.client.abstraction.apks;

import java.net.UnknownHostException;

import moses.client.com.ConnectionParam;
import moses.client.com.NetworkJSON.BackgroundException;
import moses.client.com.ReqTaskExecutor;
import moses.client.com.requests.RequestUninstalledAPK;
import moses.client.service.MosesService;
import moses.client.service.helpers.EHookTypes;
import moses.client.service.helpers.EMessageTypes;
import moses.client.service.helpers.Executor;

import org.json.JSONException;

import android.util.Log;

public class ApkUninstalled {

	private class APKUninstalledTaskExecutor implements ReqTaskExecutor {

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

	public ApkUninstalled(final String appID) {
		//TODO: handle service == null
        if (MosesService.getInstance() != null)
			MosesService.getInstance().executeLoggedIn(EHookTypes.POSTLOGINSUCCESS, EMessageTypes.REQUESTUNINSTALLEDAPK, new Executor() {

				@Override
				public void execute() {
					Log.d("MoSeS.APK", "Sending information to server that app was uninstalled: " + appID);
					new RequestUninstalledAPK(new APKUninstalledTaskExecutor(), MosesService
							.getInstance().getSessionID(), appID).send();
				}
			});
	}
}