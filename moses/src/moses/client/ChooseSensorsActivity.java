package moses.client;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;

import moses.client.abstraction.HardwareAbstraction;
import moses.client.abstraction.ESensor;
import moses.client.service.MosesService;
import moses.client.service.MosesService.LocalBinder;
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

	/** The m service. */
	public MosesService mService;

	/** The m bound. */
	public static boolean mBound = false;

	/** The m connection. */
	private ServiceConnection mConnection = new ServiceConnection() {

		@Override
		public void onServiceConnected(ComponentName className, IBinder service) {
			// We've bound to LocalService, cast the IBinder and get
			// LocalService instance
			LocalBinder binder = (LocalBinder) service;
			mService = binder.getService();
			JSONArray filter = mService.getFilter();
			HashSet<Integer> h = new HashSet<Integer>();
			for(int i = 0; i < filter.length(); ++i) {
				try {
					h.add(filter.getInt(i));
					Log.d("DEBUG APP", "" + filter.getInt(i));
				} catch (JSONException e) {
					// TODO: Handle exception
				}
			}
			for(int i = 0; i < lstSensors.getCount(); ++i) {
				if(h.contains(((ESensor)lstSensors.getItemAtPosition(i)).ordinal())) {
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

	/** The lst sensors. */
	ListView lstSensors;

	/** The sensors. */
	private List<Sensor> sensors = null; // list of all sensors for this device

	/** The ok btn. */
	private Button okBtn; // Ok button for exiting the view

	/**
	 * This method is called in order to obtain the filter from the server.
	 * 
	 * @return the filter
	 */
	protected void getFilter() {
		HardwareAbstraction ha = new HardwareAbstraction(this);
		ha.getFilter();
	}

	/**
	 * Gets the sensors.
	 * 
	 * @return the sensors
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
	 * Inits the controls.
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
						temp.add(((ESensor) lstSensors
								.getItemAtPosition(i)).ordinal());
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
		if(mBound) {
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
