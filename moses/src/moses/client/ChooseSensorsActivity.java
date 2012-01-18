package moses.client;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

import moses.client.abstraction.ESensor;
import moses.client.abstraction.HardwareAbstraction;
import moses.client.service.MosesService;
import moses.client.service.MosesService.LocalBinder;

import org.json.JSONArray;
import org.json.JSONException;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

// TODO: Auto-generated Javadoc
/**
 * If a user wishes to change his filter settings this activity is called.
 * 
 * @author Jaco
 */
public class ChooseSensorsActivity extends Activity {

	/** Represents the MoSeS service */
	public MosesService mService;

	/** This variable is true if this activity is bound to MoSeS Service */
	public static boolean mBound = false;

	/** Handles connection with MoSeS Service */
	private ServiceConnection mConnection = new ServiceConnection() {

		@Override
		public void onServiceConnected(ComponentName className, IBinder service) {
			// We've bound to LocalService, cast the IBinder and get
			// LocalService instance
			LocalBinder binder = (LocalBinder) service;
			mService = binder.getService();
			JSONArray filter = mService.getFilter();
			HashSet<Integer> h = new HashSet<Integer>();
			for (int i = 0; i < filter.length(); ++i) {
				try {
					h.add(filter.getInt(i));
				} catch (JSONException e) {
					Log.d("MoSeS.CHOOSE_SENSORS", "Illegal JSON String: "
							+ filter.toString());
				}
			}
			for (int i = 0; i < lstSensors.getCount(); ++i) {
				if (h.contains(((ESensor) lstSensors.getItemAtPosition(i))
						.ordinal())) {
					lstSensors.setItemChecked(i, true);
				}
			}
			mBound = true;
		}

		@Override
		public void onServiceDisconnected(ComponentName arg0) {
			mBound = false;
		}
	};

	/** List of all sensors to display on the screen. */
	ListView lstSensors;

	/** List of all sensors on this device */
	private List<Sensor> sensors = null;

	/**
	 * Pressing this button sends the currently selected filters to the server
	 * and closes this activity
	 **/
	private Button okBtn;

	/**
	 * This method is called in order to obtain the current filter from the
	 * server.
	 */
	protected void getFilter() {
		HardwareAbstraction ha = new HardwareAbstraction(this);
		ha.getFilter();
	}

	/**
	 * Get all available sensors from the operating system.
	 * 
	 * @return All available sensors on this device
	 */
	private List<Sensor> getSensors() {
		if (sensors == null) {
			sensors = new ArrayList<Sensor>();
			SensorManager s = (SensorManager) getSystemService(SENSOR_SERVICE);
			for (Sensor sen : s.getSensorList(Sensor.TYPE_ALL))
				sensors.add(sen);
		}

		return sensors;
	}

	/**
	 * Initialise the controls.
	 */
	private void initControls() {
		lstSensors = (ListView) findViewById(R.id.sensorlist);
		HashSet<ESensor> l = new HashSet<ESensor>();
		sensors = getSensors();
		for (int i = 0; i < sensors.size(); i++)
			l.add(ESensor.values()[sensors.get(i).getType()]);

		ESensor[] ls = new ESensor[l.size()];
		int z = 0;
		for (ESensor i : l) {
			ls[z] = i;
			++z;
		}

		Arrays.sort(ls);

		LinkedList<ESensor> s = new LinkedList<ESensor>();
		for (ESensor i : ls)
			s.add(i);

		lstSensors.setAdapter(new ArrayAdapter<ESensor>(this,
				android.R.layout.simple_list_item_multiple_choice, s));

		// implement the functionality of the "Ok" button
		okBtn = (Button) findViewById(R.id.choosesensorsokbutton);
		okBtn.setOnClickListener(new Button.OnClickListener() {
			@Override
			public void onClick(View v) {
				List<Integer> temp = new ArrayList<Integer>();
				SparseBooleanArray b = lstSensors.getCheckedItemPositions();
				for (int i = 0; i < lstSensors.getCount(); ++i) {
					if (b.get(i))
						temp.add(((ESensor) lstSensors.getItemAtPosition(i))
								.ordinal());
				}
				setFilter(temp);
				finish();
			}
		});

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.choosesensors);
		Intent intent = new Intent(this, MosesService.class);
		bindService(intent, mConnection, 0);
		initControls();
	}

	public void onDestroy() {
		super.onDestroy();
		if (mBound) {
			unbindService(mConnection);
		}
	}

	/**
	 * This method is called for setting the filter on the server.
	 * 
	 * @param filter
	 *            the new filter
	 */
	protected void setFilter(List<Integer> filter) {
		HardwareAbstraction hw = new HardwareAbstraction(this);
		hw.setFilter(filter);
		JSONArray j = new JSONArray(filter);
		mService.setFilter(j);
	}

}
