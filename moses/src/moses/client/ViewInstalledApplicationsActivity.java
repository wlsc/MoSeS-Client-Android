package moses.client;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import moses.client.abstraction.apks.ExternalApplication;
import moses.client.abstraction.apks.InstalledExternalApplication;
import moses.client.abstraction.apks.InstalledExternalApplicationsManager;
import android.app.Activity;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Viewing and installing apks from the server
 * 
 * @author Simon L
 */
public class ViewInstalledApplicationsActivity extends Activity {

	private ListView listView;
	private List<InstalledExternalApplication> installedApps;

	/**
	 * Inits the controls.
	 */
	private void initControls() {
		refreshInstalledApplications();
	}

	private void refreshInstalledApplications() {
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
			Toast.makeText(
					this.getApplicationContext(),
					"app was not found - maybe because it was uninstalled since last database refresh",
					Toast.LENGTH_LONG).show();
		}
	}

	private void populateList(List<InstalledExternalApplication> applications) {

		// TODO::
		listView = (ListView) findViewById(R.id.installedAppListView);
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
			listContent.add(rowMap);

		}
		SimpleAdapter contentAdapter = new SimpleAdapter( 
			this, 
			listContent,
			R.layout.installedapplistitem,
			new String[] { "name","description" },
			new int[] { R.id.installedAppListItemText, R.id.installedAppListItemDescription } );
		
//		ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(this,
//				R.layout.installedapplistitem, R.id.installedAppListItemText,
//				items) {
//		};
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
