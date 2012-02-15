package moses.client;

import java.util.LinkedList;
import java.util.List;

import moses.client.abstraction.apks.ExternalApplication;
import moses.client.abstraction.apks.InstalledExternalApplication;
import moses.client.abstraction.apks.InstalledExternalApplicationsManager;
import android.app.Activity;
import android.content.pm.PackageManager.NameNotFoundException;
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
				InstalledExternalApplicationsManager.getDefault().getApps());
		populateList(installedApps);
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
		
		ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(this,
				R.layout.installedapplistitem, R.id.installedAppListItemText,
				items) {
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
