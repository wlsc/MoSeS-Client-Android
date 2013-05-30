package de.da_sense.moses.client.preferences;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;

import android.app.ActionBar;
import android.hardware.Sensor;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import de.da_sense.moses.client.R;
import de.da_sense.moses.client.abstraction.ESensor;
import de.da_sense.moses.client.abstraction.HardwareAbstraction;
import de.da_sense.moses.client.service.MosesService;

/**
 * The Moses preference screen.
 * 
 * @author Jaco Hofmann, Wladimir Schmidt
 * 
 */
public class MosesPreferences extends PreferenceActivity {

	/**
	 * This method reads the available sensors on a device and shows them on the
	 * screen.
	 */
	private void loadSensors() {
		// a Hashset will represent the available sensors on a device.
		HashSet<ESensor> l = new HashSet<ESensor>();
		// an ArrayList represents the available sensors on a device.
		ArrayList<Sensor> sensors = (ArrayList<Sensor>) HardwareAbstraction
				.getSensors();
		// Moving the name of the available sensors in l
		for (int i = 0; i < sensors.size(); i++)
			l.add(ESensor.values()[sensors.get(i).getType()]);
		// an Array will represents the available sensors on a device.
		ESensor[] ls = new ESensor[l.size()];
		// Setting the contents
		int z = 0;
		for (ESensor i : l) {
			ls[z] = i;
			++z;
		}
		// Sorting the name of the sensors
		Arrays.sort(ls);
		// LinkedList will represent the sorted available sensors
		LinkedList<ESensor> s = new LinkedList<ESensor>();
		for (ESensor i : ls)
			s.add(i);
		// an Array represents the sensors' names
		CharSequence[] entries = new CharSequence[s.size()];
		// an Array represents the sensors' values
		CharSequence[] entryValues = new CharSequence[s.size()];
		// an Array represents the sensors' pictures
		String[] entryPics = new String[s.size()];
		for (int i = 0; i < s.size(); ++i) {
			entries[i] = s.get(i).toString();
			entryPics[i] = s.get(i).image();
			entryValues[i] = Integer.toString(s.get(i).ordinal());
		}
		ListPreferenceMultiSelect lp = (ListPreferenceMultiSelect) findPreference("sensor_data");
		// set lp with the available sensors and their information
		lp.setEntries(entries);
		lp.setImages(getApplicationContext(), entryPics);
		lp.setEntryValues(entryValues);
	}

	/**
	 * @see android.preference.PreferenceActivity#onCreate(android.os.Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// Actionbar
		ActionBar ab = getActionBar();
		ab.setTitle("MoSeS - Settings");
		ab.setDisplayShowTitleEnabled(true);
		
		// set the resource for preference screen
		addPreferencesFromResource(R.xml.moses_pref);
		// read the available sensors of this device
		loadSensors();
		// check if the user decide to filter the sensors' set
		if (getIntent().getBooleanExtra("startSensors", false)) {
			getPreferenceScreen().onItemClick(null, null, 1, 0);
		}
	}

	/**
	 * @see android.app.Activity#onWindowFocusChanged(boolean)
	 */
	@Override
	public void onWindowFocusChanged(boolean f) {
		super.onWindowFocusChanged(f);
		if (f && MosesService.getInstance() != null) {
			MosesService.getInstance().setActivityContext(this);
		}
	}

}
