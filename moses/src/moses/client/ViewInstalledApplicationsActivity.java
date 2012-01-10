package moses.client;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import moses.client.abstraction.ApkMethods;
import moses.client.abstraction.HardwareAbstraction;
import moses.client.abstraction.apks.ApkDownloadObserver;
import moses.client.abstraction.apks.ApkDownloadTask;
import moses.client.abstraction.apks.ExternalApplication;
import moses.client.abstraction.apks.InstalledExternalApplication;
import moses.client.abstraction.apks.InstalledExternalApplicationsManager;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.DialogInterface;
import android.content.pm.PackageManager.NameNotFoundException;
import android.database.DataSetObserver;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
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
		installedApps = new LinkedList<InstalledExternalApplication>(InstalledExternalApplicationsManager.getDefault().getApps());
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
	
	public void appStartClickHandler(View v) 
    {
		int pos = listView.getPositionForView(v);
		InstalledExternalApplication app = installedApps.get(pos);
		
		handleStartApp(app);
		
//		ListView lvItems = listView;
//        for (int i=0; i < lvItems.getChildCount(); i++) 
//        {
//            lvItems.getChildAt(i).setBackgroundColor(Color.BLUE);        
//        }
        //get the row the clicked button is in
//        LinearLayout vwParentRow = (LinearLayout)v.getParent();
//        TextView child = (TextView)vwParentRow.getChildAt(0);
//        Button btnChild = (Button)vwParentRow.getChildAt(1);
//        btnChild.setText(child.getText());
//        int pos = lvItems.getPositionForView(v);
//        btnChild.setText("I've been clicked! " + pos);
//        int c = Color.CYAN;
//        vwParentRow.setBackgroundColor(c); 
//        vwParentRow.refreshDrawableState();       
    }

	private void handleStartApp(InstalledExternalApplication app) {
		try {
			app.startApplication(this);
		} catch (NameNotFoundException e) {
			Toast.makeText(this.getApplicationContext(), "app was not found - maybe because it was uninstalled since last database refresh", Toast.LENGTH_LONG);
		}
	}

	private void populateList(List<InstalledExternalApplication> applications) {
		
		//TODO::
		listView = (ListView) findViewById(R.id.installedAppListView);
		String[] items = new String[applications.size()];
		int counter = 0;
		for(ExternalApplication app: applications) {
			items[counter] = app.getName();
			counter++;
		}
		ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(this, 
				R.layout.installedapplistitem, 
				R.id.installedAppListItemText, items) {
		};
		listView.setAdapter(arrayAdapter);
	}
	
	protected void showMessageBox(String text) {
		AlertDialog ad = new AlertDialog.Builder(this).create();
		ad.setCancelable(false); // This blocks the 'BACK' button
		ad.setMessage(text);
	
		ad.setButton("OK", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
				// proceed.setProceed();
			}
		});
	
		ad.show();
		// while(! proceed.getProceed()) {
		// // block;
		// }
	}

	public static String concatStacktrace(Exception e) {
		String stackTrace = "";
		for (int i = 0; i < e.getStackTrace().length; i++) {
			stackTrace += e.getStackTrace()[i];
		}
		return stackTrace;
	}

}
