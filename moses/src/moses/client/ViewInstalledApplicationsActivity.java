package moses.client;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import moses.client.abstraction.apks.ExternalApplication;
import moses.client.abstraction.apks.InstalledExternalApplication;
import moses.client.abstraction.apks.InstalledExternalApplicationsManager;
import android.app.Activity;
import android.app.ListActivity;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Viewing installed apks from the server in a list
 * 
 * @author Simon L
 */
public class ViewInstalledApplicationsActivity extends ListActivity {

	private ListView listView;
	private List<InstalledExternalApplication> installedApps;

	/**
	 * Inits the controls.
	 */
	private void initControls() {
		refreshInstalledApplications();
	}

	//variable for limiting retries for requesting a check of validity of the installed apks database
	private int retriesCheckValidState = 0;
	private void refreshInstalledApplications() {
		if(MosesActivity.checkInstalledStatesOfApks() == null) {
			if(retriesCheckValidState < 2) {
				Handler delayedRetryHandler = new Handler();
				delayedRetryHandler.postDelayed(new Runnable() {
					@Override
					public void run() {
						refreshInstalledApplications();
					}
				}, 3000);
				retriesCheckValidState++;
			} else {
				//TODO:show error when all retries didn't work?
				//TODO:BIG also do this for viewAvailableApplicationsList? 
			}
		} else {
			retriesCheckValidState = 0;
		}
		installedApps = new LinkedList<InstalledExternalApplication>(
				InstalledExternalApplicationsManager.getInstance().getApps());
		populateList(installedApps);
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		refreshInstalledApplications();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.installedapplicationslist);

		initControls();
	}

	public void appStartClickHandler(View v) {
		int pos = listView.getPositionForView(v);
		InstalledExternalApplication app = installedApps.get(pos);

		handleStartApp(app);

	}

	private void handleStartApp(InstalledExternalApplication app) {
		try {
			app.startApplication(this);
		} catch (NameNotFoundException e) {
			Log.e("MoSeS.APK", "Appstart: app was not found - maybe because it was uninstalled since last database refresh");
		}
	}

	/**
	 * Shows the given applications in the list
	 * 
	 * @param applications 
	 */
	private void populateList(List<InstalledExternalApplication> applications) {

		listView = getListView();
		String[] items = new String[applications.size()];
		int counter = 0;
		for (ExternalApplication app : applications) {
			items[counter] = app.getName();
			counter++;
		}
		
		TextView instructionsView = (TextView) findViewById(R.id.installedAppHeaderInstructions);
		if(instructionsView != null) {
			if(applications.size() == 0) {
				instructionsView.setText(R.string.installedApkList_emptyHint);
			} else {
				instructionsView.setText(R.string.installedApkList_defaultHint);
			}
		}
		
		List<Map<String, String>> listContent = new LinkedList<Map<String, String>>();
		for(InstalledExternalApplication app: applications) {
			HashMap<String, String> rowMap = new HashMap<String, String>();
			rowMap.put("name", app.getName());
			rowMap.put("description", app.getDescription());
			rowMap.put("userstudyIndicator", app.wasInstalledAsUserStudy()?"user study":"");
			listContent.add(rowMap);

		}
		SimpleAdapter contentAdapter = new SimpleAdapter( 
			this, 
			listContent,
			R.layout.installedapplistitem,
			new String[] { "name","description","userstudyIndicator" },
			new int[] { R.id.installedAppListItemText, R.id.installedAppListItemDescription, R.id.userstudyIndicator } );
		
		listView.setAdapter(contentAdapter);
	}

	public static String concatStacktrace(Exception e) {
		String stackTrace = "";
		for (int i = 0; i < e.getStackTrace().length; i++) {
			stackTrace += e.getStackTrace()[i];
		}
		return stackTrace;
	}

}
