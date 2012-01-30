package moses.client.abstraction;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import moses.client.abstraction.apks.ExternalApplication;
import moses.client.com.ConnectionParam;
import moses.client.com.NetworkJSON.BackgroundException;
import moses.client.com.ReqTaskExecutor;
import moses.client.com.requests.RequestDownloadlink;
import moses.client.com.requests.RequestGetListAPK;
import moses.client.com.requests.RequestLogin;
import moses.client.service.MosesService;
import moses.client.service.helpers.Executor;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.util.Log;

public class ApkMethods {

	private static class RequestDownloadLinkExecutor implements ReqTaskExecutor {
		private ApkDownloadLinkRequestObserver observer;
		private ExternalApplication app;

		public RequestDownloadLinkExecutor(
				ApkDownloadLinkRequestObserver observer, ExternalApplication app) {
			this.observer = observer;
			this.app = app;
		}

		@Override
		public void handleException(Exception e) {
			observer.apkDownloadLinkRequestFailed(e);
		}

		@Override
		public void postExecution(String s) {
			JSONObject j = null;
			try {
				j = new JSONObject(s);
				if (RequestDownloadlink.downloadLinkRequestAccepted(j)) {

					String url = j.getString("URL");
					observer.apkDownloadLinkRequestFinished(url, app);

				} else {
					Log.d("MoSeS.APK_METHODS",
							"Request not successful! Server returned negative response");
					this.handleException(new RuntimeException("abc"));
				}
			} catch (JSONException e) {
				this.handleException(e);
			}
		}

		@Override
		public void updateExecution(BackgroundException c) {
			if (c.c == ConnectionParam.EXCEPTION) {
				handleException(c.e);
			}
		}
	}

	public static void getDownloadLinkFor(ExternalApplication app,
			ApkDownloadLinkRequestObserver observer) {
		String sessionID = RequestLogin.getSessionID(); // obtain the session id

		final RequestDownloadlink rGetListAPK = new RequestDownloadlink(
				new RequestDownloadLinkExecutor(observer, app), sessionID,
				app.getID());
		if (MosesService.getInstance() != null)
			MosesService.getInstance().executeLoggedIn(new Executor() {

				@Override
				public void execute() {
					rGetListAPK.send();
				}
			});
	}

	private static class RequestApkListExecutor implements ReqTaskExecutor {
		private ApkListRequestObserver observer;

		public RequestApkListExecutor(ApkListRequestObserver observer) {
			this.observer = observer;
		}

		@Override
		public void handleException(Exception e) {
			observer.apkListRequestFailed(e);
		}

		@Override
		public void postExecution(String s) {
			JSONObject j = null;
			try {
				j = new JSONObject(s);
				if (RequestGetListAPK.isListRetrived(j)) {

					List<ExternalApplication> apps = new LinkedList<ExternalApplication>();

					JSONArray apkInformations = j.getJSONArray("APK_LIST");
					for (int i = 0; i < apkInformations.length(); i++) {
						JSONObject apkInformation = apkInformations
								.getJSONObject(i);
						String id = apkInformation.getString("ID");
						String name = apkInformation.getString("NAME");
						String description = apkInformation.getString("DESCR");

						ExternalApplication externalApplication = new ExternalApplication(
								id);
						externalApplication.setName(name);
						externalApplication.setDescription(description);
						apps.add(externalApplication);
					}

					observer.apkListRequestFinished(apps);

				} else {
					Log.d("MoSeS.APK_METHODS",
							"Request not successful! Server returned negative response");
				}
			} catch (JSONException e) {
				this.handleException(e);
			}
		}

		@Override
		public void updateExecution(BackgroundException c) {
			if (c.c == ConnectionParam.EXCEPTION) {
				handleException(c.e);
			}
		}
	}

	public static void getExternalApplications(ApkListRequestObserver observer) {
		String sessionID = RequestLogin.getSessionID(); // obtain the session id

		RequestGetListAPK rGetListAPK = new RequestGetListAPK(
				new RequestApkListExecutor(observer), sessionID);

		rGetListAPK.send();
	}

	/**
	 * Installs a given apk file.
	 * 
	 * @param apk
	 * @param baseActivity
	 */
	public static void installApk(File apk, Activity baseActivity) {
		Intent promptInstall = new Intent(Intent.ACTION_VIEW);
		if (apk.exists()) {
			promptInstall.setDataAndType(Uri.fromFile(apk),
					"application/vnd.android.package-archive");
			try {
				baseActivity.startActivity(promptInstall);
			} catch (ActivityNotFoundException e) {
				// should not occur
				e.printStackTrace();
			}
		} else {
			throw new IllegalArgumentException("apk file does not exist");
		}
	}

	public static void startApplication(String packageName,
			Activity baseActivity) throws NameNotFoundException {
		Intent intent = baseActivity.getApplicationContext()
				.getPackageManager().getLaunchIntentForPackage(packageName);
		baseActivity.startActivity(intent);
	}

	/**
	 * Retrieves the package name from an apk file
	 * 
	 * @param apk
	 *            the file
	 * @param appContext
	 *            the application context
	 * @return the package name
	 * @throws IOException
	 *             if the apk file could not be parsed correctly
	 */
	public static String getPackageNameFromApk(File apk, Context appContext)
			throws IOException {
		PackageInfo appInfo = appContext.getPackageManager()
				.getPackageArchiveInfo(apk.getAbsolutePath(), 0);
		if (appInfo != null) {
			return appInfo.packageName;
		} else {
			throw new IOException(
					"the given package could not be parsed correctly");
		}
	}

	/**
	 * checks whether an application is installed or not
	 * 
	 * @param packageName
	 *            the package name for the application
	 * @param context
	 * @return
	 */
	public static boolean isApplicationInstalled(String packageName,
			Context context) {
		try {
			context.getPackageManager().getApplicationInfo(packageName, 0);
		} catch (NameNotFoundException e) {
			return false;
		}
		return true;
	}

}
