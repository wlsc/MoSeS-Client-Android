package moses.client;

import java.util.LinkedList;

import android.app.Activity;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class ChooseSensorsActivity extends Activity {
	
	ListView lstSensors;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.choosesensors);
		initControls();
	}
	
	private void initControls() {
		lstSensors = (ListView)findViewById(R.id.sensorlist);
		lstSensors.setAdapter(new ArrayAdapter<String>(this, R.layout.choosesensors_row, R.id.choose_sensors_txt, getSensors()));
	}
	
	private String[] getSensors() {
		LinkedList<String> l = new LinkedList<String>();
		SensorManager s = (SensorManager) getSystemService(SENSOR_SERVICE);
		for(Sensor sen : s.getSensorList(Sensor.TYPE_ALL)) {
			l.add(sen.getName());
		}
		return l.toArray(new String[l.size()]);
	}
}
