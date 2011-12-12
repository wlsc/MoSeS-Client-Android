package moses.client;

import java.util.LinkedList;

import android.app.Activity;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

/**
 * If a user wishes to change his filter settings this activity
 * is called
 * @author Jaco
 *
 */
public class ChooseSensorsActivity extends Activity {
	
	ListView lstSensors;
	private Button okBtn; // Ok button for confirming the chosen sensors
	
	private String[] getSensors() {
		LinkedList<String> l = new LinkedList<String>();
		SensorManager s = (SensorManager) getSystemService(SENSOR_SERVICE);
		for(Sensor sen : s.getSensorList(Sensor.TYPE_ALL)) {
			l.add(sen.getName());
		}
		return l.toArray(new String[l.size()]);
	}
	
	private void initControls() {
		lstSensors = (ListView)findViewById(R.id.sensorlist);
		lstSensors.setAdapter(new ArrayAdapter<String>(this, R.layout.choosesensors_row, R.id.choose_sensors_txt, getSensors()));
		
		// implement the functionality of the "Ok" button
		okBtn = (Button) findViewById(R.id.choosesensorsokbutton);
		okBtn.setOnClickListener(new Button.OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
			}
		});
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.choosesensors);
		initControls();
	}
	
}
