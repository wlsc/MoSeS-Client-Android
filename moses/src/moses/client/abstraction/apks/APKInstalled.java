package moses.client.abstraction.apks;

import java.net.UnknownHostException;

import org.json.JSONException;

import android.util.Log;
import moses.client.com.ConnectionParam;
import moses.client.com.ReqTaskExecutor;
import moses.client.com.NetworkJSON.BackgroundException;
import moses.client.com.requests.RequestInstalledAPK;
import moses.client.service.MosesService;
import moses.client.service.helpers.Executor;

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
			MosesService.getInstance().executeLoggedIn(new Executor() {

				@Override
				public void execute() {
					new RequestInstalledAPK(new APKInstalledTaskExecutor(), MosesService
							.getInstance().getSessionID(), appID).send();
				}
			});
	}
}
