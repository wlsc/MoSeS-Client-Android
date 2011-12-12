package moses.client;

import java.util.LinkedList;
import moses.client.com.ConnectionParam;
import moses.client.com.NetworkJSON.BackgroundException;
import moses.client.com.ReqTaskExecutor;
import moses.client.com.requests.RequestLogin;
import moses.client.com.requests.RequestSetHardwareParameters;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

public class ChooseSensorsActivity extends Activity {
	
	ListView lstSensors;
	private Button okBtn; // Ok button for confirming the chosen sensors
	private TextView choosesensorstext; // text on the login frame
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.choosesensors);
		initControls();
	}
	
	private void initControls() {
		lstSensors = (ListView)findViewById(R.id.sensorlist);
		lstSensors.setAdapter(new ArrayAdapter<String>(this, R.layout.choosesensors_row, R.id.choose_sensors_txt, getSensors()));
		choosesensorstext = (TextView) findViewById(R.id.choosesensorstext);
		
		// implement the functionality of the "Ok" button
		okBtn = (Button) findViewById(R.id.choosesensorsokbutton);
		okBtn.setOnClickListener(new Button.OnClickListener() {
			public void onClick(View v) {
				setHardwareParameters();
			}
		});
		
	}
	
	/**
	 * This method reads the sensors currently chosen by the user
	 * and send the appropriate update to the MoSeS-Website
	 */
	private void setHardwareParameters(){
		
		/* Alles Quatsch ab hier
		List<String> chosenSensors = new ArrayList<String>();
		ListAdapter ladapter = lstSensors.getAdapter();
		// iterate over all adapters, add only the chosen ones to the list
		for(int i=0; i<ladapter.getCount(); i++){
			CheckBox checkBox = (CheckBox) ladapter.getItem(i);
			if(checkBox.isChecked())
				// The checkBox is checked, get the ordinal of the sensor
				// FIXME this is incorrect!!!!! The real sensorID should be sent
				// and not the ordinal
				chosenSensors.add(Integer.toString(i));
		}
		
		StringBuffer sb = new StringBuffer(chosenSensors.size()*2); // sensors ids <sensorID><_><sensorID>...
		for(String sensorID : chosenSensors){
			sb.append(sensorID);
			sb.append("_");
		}
		
		// this string will be wrapped into JASON object
		String sensors;
		if(sb.length() >= 1)
			sensors = sb.substring(0, sb.length()-1); // get rid of tha last _ if any
		else sensors = ""; */
		
		//*** SENDING SET_HARDWARE_PARAMETERS REQUEST TO SERVER ***//
		String sessionID = RequestLogin.getSessionID(); // obrain the session id
		
		RequestSetHardwareParameters rSetHWParams = // FIXME: the names should not be hard coded!!!!
				new RequestSetHardwareParameters(new ReqClassChooseSensors(), sessionID, "nexusOne", "andoidVersion 2.3.6", "1_2_3");
		rSetHWParams.send();
		
	}
	
	private String[] getSensors() {
		LinkedList<String> l = new LinkedList<String>();
		SensorManager s = (SensorManager) getSystemService(SENSOR_SERVICE);
		for(Sensor sen : s.getSensorList(Sensor.TYPE_ALL)) {
			l.add(sen.getName());
		}
		return l.toArray(new String[l.size()]);
	}
	
	private class ReqClassChooseSensors implements ReqTaskExecutor {

		public void postExecution(String s) {
			JSONObject j = null;
			try {
				j = new JSONObject(s);
				if (RequestSetHardwareParameters.parameterSetOnServer(j)) {
					Intent loginScreen = new Intent();
					setResult(RESULT_OK, loginScreen);
					finish();
				} else {
					// TODO handling and a proper place for textSuccess!!
					choosesensorstext.setText("SET_HARDWARE_PARAMETERS WAS REJECTED FROM SERVER"
							+ j.toString());
				}
			} catch (JSONException e) {
				this.handleException(e);
			}
		}

		public void updateExecution(BackgroundException c) {
			if (c.c != ConnectionParam.EXCEPTION) {
				choosesensorstext.setText(c.toString());
			} else {
				handleException(c.e);
			}
		}

		public void handleException(Exception e) {
			choosesensorstext.setText("FAILURE: " + e.getMessage());
		}
	}
	
	
	
}
