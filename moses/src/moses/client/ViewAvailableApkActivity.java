package moses.client;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import moses.client.abstraction.ApkDownloadLinkRequestObserver;
import moses.client.abstraction.ApkListRequestObserver;
import moses.client.abstraction.ApkMethods;
import moses.client.abstraction.apks.APKInstalled;
import moses.client.abstraction.apks.ApkDownloadObserver;
import moses.client.abstraction.apks.ApkDownloadTask;
import moses.client.abstraction.apks.ExternalApplication;
import moses.client.abstraction.apks.InstalledExternalApplication;
import moses.client.abstraction.apks.InstalledExternalApplicationsManager;
import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

/**
 * Viewing and installing apks from the server
 * 
 * @author Simon L
 */
public class ViewAvailableApkActivity extends Activity implements
		ApkDownloadObserver, ApkListRequestObserver,
		ApkDownloadLinkRequestObserver {

	private ListView listView;
	private List<ExternalApplication> externalApps;

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.availableapklist);

		initControls();
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
			new APKInstalled(externalAppRef.getID());
		} catch (IOException e) {
			// TODO: the package name could not be read from the apk file,
			// or there was a problem with saving the installed-app-manager. to
			// be programmed yet!
			// TODO: program check that installation was really successful
			e.printStackTrace();
		}
	}

	private static String generateApkFileNameFor(ExternalApplication app) {
		return app.getID() + ".apk";
	}

	private void requestExternalApplications() {
		List<ExternalApplication> apps;
		// ExternalApplication app1 = new ExternalApplication("1");
		// ExternalApplication app2 = new ExternalApplication("2");
		// apps = Arrays.asList(new ExternalApplication[]{app1, app2});

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
