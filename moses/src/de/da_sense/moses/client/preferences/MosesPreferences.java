package de.da_sense.moses.client.preferences;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;

import de.da_sense.moses.client.abstraction.ESensor;
import de.da_sense.moses.client.abstraction.HardwareAbstraction;
import de.da_sense.moses.client.service.MosesService;

import de.da_sense.moses.client.R;
import android.hardware.Sensor;
import android.os.Bundle;
import android.preference.PreferenceActivity;

public class MosesPreferences extends PreferenceActivity {

	private void loadSensors() {
		HashSet<ESensor> l = new HashSet<ESensor>();
		ArrayList<Sensor> sensors = (ArrayList<Sensor>) HardwareAbstraction.getSensors();
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

		CharSequence[] entries = new CharSequence[s.size()];
		CharSequence[] entryValues = new CharSequence[s.size()];
		String[] entryPics = new String[s.size()];
		for (int i = 0; i < s.size(); ++i) {
			entries[i] = s.get(i).toString();
			entryPics[i] = s.get(i).image();
			entryValues[i] = Integer.toString(s.get(i).ordinal());
		}
		ListPreferenceMultiSelect lp = (ListPreferenceMultiSelect) findPreference("sensor_data");
		lp.setEntries(entries);
		lp.setImages(getApplicationContext(), entryPics);
		lp.setEntryValues(entryValues);
	}

	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		addPreferencesFromResource(R.xml.moses_pref);
		loadSensors();
		if (getIntent().getBooleanExtra("startSensors", false)) {
			getPreferenceScreen().onItemClick(null, null, 1, 0);
		}
	}
	
	@Override
	public void onWindowFocusChanged(boolean f) {
		super.onWindowFocusChanged(f);
		if (f && MosesService.getInstance() != null) {
			MosesService.getInstance().setActivityContext(this);
		}
	}

}
