package de.da_sense.moses.client;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import android.app.ListActivity;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import de.da_sense.moses.client.abstraction.apks.ExternalApplication;
import de.da_sense.moses.client.abstraction.apks.InstalledExternalApplication;
import de.da_sense.moses.client.abstraction.apks.InstalledExternalApplicationsManager;
import de.da_sense.moses.client.userstudy.UserstudyNotificationManager;

/**
 * Viewing installed apks from the server in a list
 * 
 * @author Simon L
 */
public class ViewInstalledApplicationsActivity extends ListActivity {

	private ListView listView;
	private List<InstalledExternalApplication> installedApps;
	private Comparator<? super InstalledExternalApplication> installedAppListComparator = new Comparator<InstalledExternalApplication>() {
		@Override
		public int compare(InstalledExternalApplication lhs, InstalledExternalApplication rhs) {
			if (rhs == null && lhs == null) {
				return 0;
			}
			if (rhs != null && lhs == null) {
				return -1;
			}
			if (rhs == null && lhs != null) {
				return 1;
			}
			if ((rhs.isUpdateAvailable() && lhs.isUpdateAvailable())
					|| (!rhs.isUpdateAvailable() && !lhs.isUpdateAvailable())) {
				if (rhs.getName().equals(lhs.getName())) {
					return Integer.valueOf(rhs.hashCode()).compareTo(lhs.hashCode());
				}
				return rhs.getName().compareTo(lhs.getName());
			}
			if (lhs.isUpdateAvailable())
				return -1;
			return 1;
		}

	};

	/**
	 * Inits the controls.
	 */
	private void initControls() {
		refreshInstalledApplications();
	}

	// variable for limiting retries for requesting a check of validity of the
	// installed apks database
	private int retriesCheckValidState = 0;

	private void refreshInstalledApplications() {
		if (MosesActivity.checkInstalledStatesOfApks() == null) {
			if (retriesCheckValidState < 4) {
				Handler delayedRetryHandler = new Handler();
				delayedRetryHandler.postDelayed(new Runnable() {
					@Override
					public void run() {
						refreshInstalledApplications();
					}
				}, 1500);
				retriesCheckValidState++;
			} else {
				// TODO:show error when all retries didn't work?
				// TODO:BIG also do this for viewAvailableApplicationsList?
			}
		} else {
			retriesCheckValidState = 0;
		}
		if (InstalledExternalApplicationsManager.getInstance() == null)
			InstalledExternalApplicationsManager.init(this);
		installedApps = sortForDisplay(new LinkedList<InstalledExternalApplication>(
				InstalledExternalApplicationsManager.getInstance().getApps()));
		populateList(installedApps);
	}

	private List<InstalledExternalApplication> sortForDisplay(Collection<InstalledExternalApplication> linkedList) {
		if (linkedList == null)
			throw new RuntimeException("installed app list was null");
		List<InstalledExternalApplication> sortedList = new ArrayList<InstalledExternalApplication>(linkedList);
		Comparator<? super InstalledExternalApplication> comparator = installedAppListComparator;
		Collections.sort(sortedList, comparator);
		return sortedList;
	}

//	long thr = System.currentTimeMillis();
	@Override
	protected void onResume() {
		super.onResume();
		refreshInstalledApplications();
//		if(System.currentTimeMillis()-thr>6000) {
//			UserstudyNotificationManager.getInstance().userStudyNotificationArrived("11");
//		}
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
		app.startApplication(this);
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
		if (instructionsView != null) {
			if (applications.size() == 0) {
				instructionsView.setText(R.string.installedApkList_emptyHint);
			} else {
				instructionsView.setText(R.string.installedApkList_defaultHint);
			}
		}

		List<Map<String, String>> listContent = new LinkedList<Map<String, String>>();
		for (InstalledExternalApplication app : applications) {
			HashMap<String, String> rowMap = new HashMap<String, String>();
			rowMap.put("name", app.getName());
			rowMap.put("description", app.getDescription());
			rowMap.put("userstudyIndicator", app.wasInstalledAsUserStudy() ? "user study" : "");
			rowMap.put("updateIndicator", app.isUpdateAvailable() ? "update available" : "");
			listContent.add(rowMap);

		}
		SimpleAdapter contentAdapter = new SimpleAdapter(this, listContent, R.layout.installedapplistitem,
				new String[] { "name", "description", "userstudyIndicator", "updateIndicator" }, new int[] {
						R.id.installedAppListItemText, R.id.installedAppListItemDescription, R.id.userstudyIndicator,
						R.id.updateIndicator });

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
