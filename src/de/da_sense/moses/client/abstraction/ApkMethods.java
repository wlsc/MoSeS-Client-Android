package de.da_sense.moses.client.abstraction;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.preference.PreferenceManager;
import de.da_sense.moses.client.abstraction.apks.ApkInstallObserver;
import de.da_sense.moses.client.abstraction.apks.ExternalApplication;
import de.da_sense.moses.client.abstraction.apks.InstallApkActivity;
import de.da_sense.moses.client.com.ConnectionParam;
import de.da_sense.moses.client.com.NetworkJSON.BackgroundException;
import de.da_sense.moses.client.com.ReqTaskExecutor;
import de.da_sense.moses.client.com.requests.RequestDownloadlink;
import de.da_sense.moses.client.com.requests.RequestGetListAPK;
import de.da_sense.moses.client.com.requests.RequestLogin;
import de.da_sense.moses.client.service.MosesService;
import de.da_sense.moses.client.service.helpers.HookTypesEnum;
import de.da_sense.moses.client.service.helpers.MessageTypesEnum;
import de.da_sense.moses.client.service.helpers.Executable;
import de.da_sense.moses.client.util.Log;

/**
 * Methods for handling the APKs.
 * @author 
 */
public class ApkMethods {
	
	private static final String LOG_TAG = ApkMethods.class.getName();
	/**
	 * Class for download link request executors.
	 */
	private static class RequestDownloadLinkExecutor implements ReqTaskExecutor {
		private ApkDownloadLinkRequestObserver observer;
		private ExternalApplication app;

		/**
		 * Constructor for a download link request executor.
		 * @param observer the link request observer
		 * @param app the app for which to request the link
		 */
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
			Log.d("MoSeS.APK", "DownloadLinkRequest response: " + s);
			try {
				j = new JSONObject(s);
				if (RequestDownloadlink.downloadLinkRequestAccepted(j)) {

					String url = j.getString("URL");
					observer.apkDownloadLinkRequestFinished(url, app);

				} else {
					Log.d("MoSeS.APK_METHODS",
							"Download link Request not successful! Server returned negative response: "
									+ s);
					this.handleException(new RuntimeException(
							"Download link Request not successful! Server returned negative response: "
									+ s));
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

	/**
	 * Get the download link for an app.
	 * @param app the application to get the download link for
	 * @param observer the Observer to notify
	 */
	public static void getDownloadLinkFor(final ExternalApplication app,
			final ApkDownloadLinkRequestObserver observer) {

		if (MosesService.getInstance() != null)
			MosesService.getInstance().executeLoggedIn(
					HookTypesEnum.POST_LOGIN_SUCCESS,
					MessageTypesEnum.REQUEST_DOWNLOAD_LINK, new Executable() {

						@Override
						public void execute() {
							new RequestDownloadlink(
									new RequestDownloadLinkExecutor(observer,
											app), RequestLogin.getSessionID(),
									PreferenceManager
											.getDefaultSharedPreferences(
													MosesService.getInstance())
											.getString("deviceid_pref", ""),
									app.getID()).send();
						}
					});
	}

	/**
	 * Class for APK list request executors.
	 */
	private static class RequestApkListExecutor implements ReqTaskExecutor {
		private ApkListRequestObserver observer;

		/**
		 * Constructor for an APK link request executor.
		 * @param observer the observer to notify
		 */
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
				Log.d("MOSES.APK", "APK-List response received:" + s);
				j = new JSONObject(s);
				if (RequestGetListAPK.isListRetrieved(j)) {

					List<ExternalApplication> apps = new LinkedList<ExternalApplication>();

					JSONArray apkInformations = j.getJSONArray("APK_LIST");
					for (int i = 0; i < apkInformations.length(); i++) {
						JSONObject apkInformation = apkInformations
								.getJSONObject(i);
						String id = apkInformation.getString("ID");
						String name = apkInformation.getString("NAME");
						String description = apkInformation.getString("DESCR");
						JSONArray sensorsArray = apkInformation
								.getJSONArray("SENSORS");
						List<Integer> resultSensors = new LinkedList<Integer>();
						for (int k = 0; k < sensorsArray.length(); k++) {
							resultSensors.add(sensorsArray.getInt(k));
						}
						String startDate = apkInformation
								.getString("STARTDATE");
						String endDate = apkInformation.getString("ENDDATE");
						String apkVersion = apkInformation
								.getString("APKVERSION");

						ExternalApplication externalApplication = new ExternalApplication(
								Integer.valueOf(id));
						externalApplication.setName(name);
						externalApplication.setDescription(description);
						externalApplication.setSensors(resultSensors);
						externalApplication.setStartDate(startDate);
						externalApplication.setEndDate(endDate);
						externalApplication.setApkVersion(apkVersion);

						apps.add(externalApplication);
					}

					observer.apkListRequestFinished(apps);

				} else {
					observer.apkListRequestFailed(new RuntimeException(
							"invalid response: " + j.toString()));
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

	/**
	 * Get the list of the available APK from the server.
	 * 
	 * @param observer
	 *            the list request observer
	 */
	public static void getExternalApplications(
			final ApkListRequestObserver observer) {
		if (MosesService.getInstance() != null) {
			MosesService.getInstance().executeLoggedIn(
					HookTypesEnum.POST_LOGIN_SUCCESS,
					MessageTypesEnum.REQUEST_GET_LIST_APK, new Executable() {
						@Override
						public void execute() {
							Log.d("MoSeS.APKMETHODS", "requesting apk list");
							new RequestGetListAPK(new RequestApkListExecutor(
									observer), RequestLogin.getSessionID())
									.send();
						}
					});
		}
	}

	/**
	 * Installs a given APK file.
	 * 
	 * @param apk
	 *            the apk file to install
	 * @param baseActivity
	 *            the base activity
	 * @throws IOException
	 *             if the file does not exist
	 */
	public static void installApk(File apk, ExternalApplication appRef,
			ApkInstallObserver o) throws IOException {
		MosesService service = MosesService.getInstance();
		if (service != null) {
			if (apk.exists()) {
				InstallApkActivity.setAppToInstall(apk, appRef, o);
				Intent installActivityIntent = new Intent(service,
						InstallApkActivity.class);
				installActivityIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				service.startActivity(installActivityIntent);
			} else {
				o.apkInstallError(
						apk,
						appRef,
						new RuntimeException(
								"Could not install apk file because it was nonexistent."));
			}
		} else {
			o.apkInstallError(
					apk,
					appRef,
					new RuntimeException(
							"Could not install apk file because the service was not running."));
		}
	}

	/**
	 * Start an application by the name of the package.
	 * 
	 * @param packageName package name of the app to start
	 * @param baseActivity the base activity
	 * @throws NameNotFoundException
	 */
	public static void startApplication(String packageName,
			Activity baseActivity) throws NameNotFoundException {
		if (baseActivity == null) {
			Log.e("ApkMethods", "the context was NULL for the package: "
					+ packageName);
		}
		Intent intent = baseActivity.getApplicationContext()
				.getPackageManager().getLaunchIntentForPackage(packageName);
		Log.d("ApkMethods",
				"created intent, about to launch other application with packageName: "
						+ packageName);
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
	 * Checks whether an application is installed or not.
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
