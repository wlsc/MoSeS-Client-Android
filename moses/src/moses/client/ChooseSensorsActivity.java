package moses.client;

import java.util.ArrayList;
import java.util.List;

import moses.client.abstraction.HardwareAbstraction;
import android.app.Activity;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.SparseBooleanArray;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

// TODO: Auto-generated Javadoc
/**
 * If a user wishes to change his filter settings this activity is called.
 * 
 * @author Jaco
 */
public class ChooseSensorsActivity extends Activity {

	/** The lst sensors. */
	ListView lstSensors;

	/** The sensors. */
	private List<Sensor> sensors = null; // list of all sensors for this device

	/** The ok btn. */
	private Button okBtn; // Ok button for exiting the view

	/** The set filter btn. */
	private Button setFilterBtn; // for sending the filter to the server

	/** The get filter btn. */
	private Button getFilterBtn; // for getting the filter from the server

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
		String[] sensorNames = new String[getSensors().size()];
		for (int i = 0; i < sensors.size(); i++)
			sensorNames[i] = Integer.toString(sensors.get(i).getType());

		lstSensors.setAdapter(new ArrayAdapter<String>(this,
				android.R.layout.simple_list_item_multiple_choice,
				sensorNames));

		// implement the functionality of the "Ok" button
		okBtn = (Button) findViewById(R.id.choosesensorsokbutton);
		okBtn.setOnClickListener(new Button.OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
			}
		});

		// functionality of the setFilterBtn
		setFilterBtn = (Button) findViewById(R.id.shooseSensorsSetFilterButton);
		setFilterBtn.setOnClickListener(new Button.OnClickListener() {
			@Override
			public void onClick(View v) {

				List<Integer> temp = new ArrayList<Integer>();
				SparseBooleanArray b = lstSensors.getCheckedItemPositions();
				for(int i = 0; i < lstSensors.getCount(); ++i) {
					if(b.valueAt(i)) 
						temp.add(Integer.parseInt((String)lstSensors.getItemAtPosition(i)));
				}
				setFilter(temp);
			}
		});

		// functionality of the getFilterBtn
		getFilterBtn = (Button) findViewById(R.id.chooseSensorsGetFilterButton);
		getFilterBtn.setOnClickListener(new Button.OnClickListener() {
			@Override
			public void onClick(View v) {
				getFilter();
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
		initControls();
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

	}

}
