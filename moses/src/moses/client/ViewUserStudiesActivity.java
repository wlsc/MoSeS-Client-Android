package moses.client;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import moses.client.abstraction.ApkDownloadLinkRequestObserver;
import moses.client.abstraction.ApkListRequestObserver;
import moses.client.abstraction.ApkMethods;
import moses.client.abstraction.apks.ApkDownloadObserver;
import moses.client.abstraction.apks.ApkDownloadTask;
import moses.client.abstraction.apks.ExternalApplication;
import moses.client.abstraction.apks.InstalledExternalApplication;
import moses.client.abstraction.apks.InstalledExternalApplicationsManager;
import moses.client.com.ReqTaskExecutor;
import moses.client.com.NetworkJSON.BackgroundException;
import moses.client.com.requests.RequestGetApkInfo;
import moses.client.service.MosesService;
import moses.client.service.helpers.Executor;
import moses.client.userstudy.UserStudyNotification;
import moses.client.userstudy.UserstudyNotificationManager;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

/**
 * Viewing and installing apks from the server
 * 
 * @author Simon L
 */
public class ViewUserStudiesActivity extends Activity implements
		ApkDownloadObserver, ApkListRequestObserver,
		ApkDownloadLinkRequestObserver {

	public static final String EXTRA_USER_STUDY_APK_ID = "UserStudyApkId";
	private ListView listView;
	private List<ExternalApplication> externalApps;
	private UserStudyNotification handleSingleNotificationData;
	private String sid;

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		String studyApkId = getIntent().getExtras().getString(
				EXTRA_USER_STUDY_APK_ID);
		sid = MosesService.getInstance().getSessionID();
		if (studyApkId != null) {
			UserStudyNotification notification = UserstudyNotificationManager
					.getInstance().getNotificationForApkId(studyApkId);
			showActivityForNotification(notification);
		} else {
			showActivityForNotification(null);
		}
	}

	private void showActivityForNotification(UserStudyNotification notification) {
		// setContentView(R.layout.availableapklist);
		// initControls();

		if (notification != null) {
			this.handleSingleNotificationData = notification;
			requestApkInfo(notification.getApplication().getID());
		}

		// AlertDialog ad = new AlertDialog.Builder(this).create();
		// ad.setCancelable(false); // This blocks the 'BACK' button
		// ad.setMessage("Hello World " +
		// notification!=null?notification.asOnelineString():"null...");
		// ad.setButton("OK", new DialogInterface.OnClickListener() {
		// @Override
		// public void onClick(DialogInterface dialog, int which) {
		// dialog.dismiss();
		// ViewUserStudiesActivity.this.finish();
		// }
		// });
		// ad.show();
	}

	private void requestApkInfo(String id) {
		final RequestGetApkInfo r = new RequestGetApkInfo(
				new ReqTaskExecutor() {
					@Override
					public void updateExecution(BackgroundException c) {
						// TODO Auto-generated method stub

					}

					@Override
					public void postExecution(String s) {
						try {
							JSONObject j = new JSONObject(s);
							if (RequestGetApkInfo.isInfoRetrived(j)) {
								String name = j.getString("NAME");
								String descr = j.getString("DESCR");
								handleSingleNotificationData.getApplication()
										.setName(name);
								handleSingleNotificationData.getApplication()
										.setDescription(descr);

								showDescisionDialog(handleSingleNotificationData);
							} else {
								Log.e("MoSeS.UserStudy",
										"user study info request: Server returned negative"
												+ j.toString());
								Toast.makeText(
										getApplicationContext(),
										"user study info request: Server returned negative"
												+ j.toString(),
										Toast.LENGTH_LONG).show();
							}
						} catch (JSONException e) {
							Log.e("MoSeS.UserStudy",
									"requesting study information: json exception"
											+ e.getMessage());
							Toast.makeText(
									getApplicationContext(),
									"requesting study information: json exception"
											+ e.getMessage(), Toast.LENGTH_LONG)
									.show();
						}
					}

					@Override
					public void handleException(Exception e) {
						Log.e("MoSeS.UserStudy",
								"couldn't load user study information"
										+ e.getMessage());
						Toast.makeText(
								getApplicationContext(),
								"couldn't load user study information"
										+ e.getMessage(), Toast.LENGTH_LONG)
								.show();
					}
				}, id, sid);
		if (MosesService.getInstance() != null)
			MosesService.getInstance().executeLoggedIn(new Executor() {

				@Override
				public void execute() {
					r.send();
				}
			});
	}

	protected void showDescisionDialog(final UserStudyNotification notification) {
		String message = "A new user study is avalable for you!\n\nName: "
				+ notification.getApplication().getName()
				+ "\n\n"
				+ notification.getApplication().getDescription()
				+ "\n\nDo you want to participate by installing the external application for this study?";
		DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				switch (which) {
				case DialogInterface.BUTTON_POSITIVE:
					requestUrlForApplication(notification.getApplication());
					dialog.dismiss();
					break;

				case DialogInterface.BUTTON_NEGATIVE:
					ViewUserStudiesActivity.this.finish();
					break;
				}
			}
		};

		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage(message)
				.setPositiveButton("Yes", dialogClickListener)
				.setNegativeButton("No", dialogClickListener).show();

		// Dialog d = new
		// AlertDialog ad = new AlertDialog.Builder(this).create();
		// ad.setCancelable(false); // This blocks the 'BACK' button
		// ad.setMessage("New user study avalable!\n"+
		// notification.getApplication().getName()+"\n\n"+notification.getApplication().getDescription());
		// ad.setButton("OK", new DialogInterface.OnClickListener() {
		// @Override
		// public void onClick(DialogInterface dialog, int which) {
		// requestUrlForApplication(notification.getApplication());
		// dialog.dismiss();
		// // ViewUserStudiesActivity.this.finish();
		// }
		// });
		// ad.show();
	}

	public void apkInstallClickHandler(View v) {
		int pos = listView.getPositionForView(v);
		ExternalApplication app = externalApps.get(pos);

		handleInstallApp(app);

		// ListView lvItems = listView;
		// for (int i=0; i < lvItems.getChildCount(); i++)
		// {
		// lvItems.getChildAt(i).setBackgroundColor(Color.BLUE);
		// }
		// get the row the clicked button is in
		// LinearLayout vwParentRow = (LinearLayout)v.getParent();
		// TextView child = (TextView)vwParentRow.getChildAt(0);
		// Button btnChild = (Button)vwParentRow.getChildAt(1);
		// btnChild.setText(child.getText());
		// int pos = lvItems.getPositionForView(v);
		// btnChild.setText("I've been clicked! " + pos);
		// int c = Color.CYAN;
		// vwParentRow.setBackgroundColor(c);
		// vwParentRow.refreshDrawableState();
	}

	/**
	 * Inits the controls.
	 */
	private void initControls() {
		requestExternalApplications();
	}

	private void handleInstallApp(ExternalApplication app) {
		// showMessageBox("Installing: \"" + app.getName()+"\"");
		// request Url for app
		requestUrlForApplication(app);
	}

	private void requestUrlForApplication(ExternalApplication app) {
		ApkMethods.getDownloadLinkFor(app, this);
		// appUrlReceived(app, "http://simlei.de/external.apk");
	}

	@Override
	public void apkDownloadLinkRequestFinished(String url,
			ExternalApplication app) {
		// fire download of apk
		try {
			ApkDownloadTask downloadTask = new ApkDownloadTask(this, new URL(
					url), this.getApplicationContext(),
					generateApkFileNameFor(app));
			downloadTask.setExternalApplicationReference(app);
			downloadTask.execute();
		} catch (MalformedURLException e) {
			Toast.makeText(
					getApplicationContext(),
					"Server sent malformed url; could not download application: "
							+ url, Toast.LENGTH_LONG).show();
		}
	}

	@Override
	public void apkDownloadLinkRequestFailed(Exception e) {
		Toast.makeText(getApplicationContext(),
				"Downloadlink request failed:\n" + concatStacktrace(e),
				Toast.LENGTH_LONG).show();
	}

	@Override
	public void apkDownloadFinished(ApkDownloadTask downloader, File result,
			ExternalApplication externalAppRef) {
		installDownloadedApk(result, externalAppRef);
	}

	@Override
	public void apkDownloadFailed(ApkDownloadTask downloader) {
		Toast.makeText(
				getApplicationContext(),
				"Download failed.\n"
						+ concatStacktrace(downloader.getDownloadException()),
				Toast.LENGTH_LONG).show();
	}

	private void installDownloadedApk(File result,
			ExternalApplication externalAppRef) {
		ApkMethods.installApk(result, this);
		try {
			if (InstalledExternalApplicationsManager.getDefault() == null) {
				InstalledExternalApplicationsManager
						.init(getApplicationContext());
			}
			String packageName = ApkMethods.getPackageNameFromApk(result,
					getApplicationContext());

			InstalledExternalApplication installedExternalApp = new InstalledExternalApplication(
					packageName, externalAppRef);
			InstalledExternalApplicationsManager.getDefault()
					.addExternalApplication(installedExternalApp);

			InstalledExternalApplicationsManager.getDefault().saveToDisk(
					getApplicationContext());
		} catch (IOException e) {
			// TODO: the package name could not be read from the apk file,
			// or there was a problem with saving the installed-app-manager. to
			// be programmed yet!
			// TODO: program check that installation was really successful
			e.printStackTrace();
		}

		finish();
	}

	private static String generateApkFileNameFor(ExternalApplication app) {
		return app.getID() + ".apk";
	}

	private void requestExternalApplications() {
		ApkMethods.getExternalApplications(this);
	}

	@Override
	public void apkListRequestFinished(List<ExternalApplication> applications) {
		externalApps = applications;
		populateList(applications);
	}

	@Override
	public void apkListRequestFailed(Exception e) {
		Toast.makeText(
				getApplicationContext(),
				"Error when loading the list of applications: "
						+ e.getMessage(), Toast.LENGTH_LONG).show();
	}

	private void populateList(List<ExternalApplication> applications) {
		listView = (ListView) findViewById(R.id.availableApkListView);
		String[] items = new String[applications.size()];
		int counter = 0;
		for (ExternalApplication app : applications) {
			items[counter] = app.getName();
			counter++;
		}
		ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(this,
				R.layout.availableabkslistitem, R.id.apklistitemtext, items) {
		};
		listView.setAdapter(arrayAdapter);
	}

	public static String concatStacktrace(Exception e) {
		String stackTrace = "";
		for (int i = 0; i < e.getStackTrace().length; i++) {
			stackTrace += e.getStackTrace()[i];
		}
		return stackTrace;
	}

}
