package de.da_sense.moses.client.preferences;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;

import android.app.ActionBar;
import android.hardware.Sensor;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import de.da_sense.moses.client.R;
import de.da_sense.moses.client.abstraction.HardwareAbstraction;
import de.da_sense.moses.client.abstraction.SensorsEnum;
import de.da_sense.moses.client.service.MosesService;

/**
 * The Moses preference screen.
 * 
 * @author Jaco Hofmann, Wladimir Schmidt
 * 
 */
public class MosesPreferences extends PreferenceActivity {

	/**
	 * @see android.preference.PreferenceActivity#onCreate(android.os.Bundle)
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
//		System.err.println("here 1");
		// Actionbar
		ActionBar ab = getActionBar();
		ab.setTitle(getString(R.string.settings_title));
		ab.setDisplayShowTitleEnabled(true);
//		System.err.println("here 2");
		// set the resource for preference screen
		
//		addPreferencesFromResource(R.xml.moses_pref);
//		System.err.println("here 3");
		// read the available sensors of this device
		loadSensors();
//		System.err.println("here 4");
		// check if the user decide to filter the sensors' set
		if (getIntent().getBooleanExtra("startSensors", false)) {
//			System.err.println("here 5");
			getPreferenceScreen().onItemClick(null, null, 1, 0);
		}
//		System.err.println("here 6");
	}

	
	public static class PrefsFragment extends PreferenceFragment {
		
		@Override
		public void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);
			
			// load preferences from XML resource
			addPreferencesFromResource(R.xml.moses_pref);
		}
	}
	
	/**
	 * This method reads the available sensors on a device and shows them on the
	 * screen.
	 */
	private void loadSensors() {
		// a Hashset will represent the available sensors on a device.
		HashSet<SensorsEnum> l = new HashSet<SensorsEnum>();
		// an ArrayList represents the available sensors on a device.
		ArrayList<Sensor> sensors = (ArrayList<Sensor>) HardwareAbstraction
				.getSensors();
		// Moving the name of the available sensors in l
		for (int i = 0; i < sensors.size(); i++)
			l.add(SensorsEnum.values()[sensors.get(i).getType()]);
		// an Array will represents the available sensors on a device.
		SensorsEnum[] ls = new SensorsEnum[l.size()];
		// Setting the contents
		int z = 0;
		for (SensorsEnum i : l) {
			ls[z] = i;
			++z;
		}
		// Sorting the name of the sensors
		Arrays.sort(ls);
		// LinkedList will represent the sorted available sensors
		LinkedList<SensorsEnum> s = new LinkedList<SensorsEnum>();
		for (SensorsEnum i : ls)
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
		// TODO: here lp null :(
		if(lp != null){ 
			lp.setEntries(entries);
			lp.setImages(getApplicationContext(), entryPics);
			lp.setEntryValues(entryValues);
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
