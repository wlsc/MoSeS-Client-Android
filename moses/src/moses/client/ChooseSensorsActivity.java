package moses.client;

import java.util.ArrayList;
import java.util.List;

import moses.client.abstraction.HardwareAbstraction;
import android.app.Activity;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Bundle;
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
			sensorNames[i] = sensors.get(i).getName();

		lstSensors.setAdapter(new ArrayAdapter<String>(this,
				R.layout.choosesensors_row, R.id.choose_sensors_txt,
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

				// TODO this dummy values should be replaced with the real ones
				// had some problems accessing the checkboxes
				List<Integer> temp = new ArrayList<Integer>();
				temp.add(1);
				temp.add(2);
				temp.add(3);
				temp.add(10);
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

	/* (non-Javadoc)
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
	 * @param filter the new filter
	 */
	protected void setFilter(List<Integer> filter) {
		HardwareAbstraction hw = new HardwareAbstraction(this);
		hw.setFilter(filter);

	}

}
