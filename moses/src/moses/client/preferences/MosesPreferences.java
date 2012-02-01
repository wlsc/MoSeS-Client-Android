package moses.client.preferences;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;

import moses.client.R;
import moses.client.abstraction.ESensor;
import moses.client.abstraction.HardwareAbstraction;
import android.hardware.Sensor;
import android.os.Bundle;
import android.preference.ListPreference;
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
		for(int i = 0; i < s.size(); ++i) {
			entries[i] = s.get(i).toString();
			entryValues[i] = Integer.toString(s.get(i).ordinal());
		}
		ListPreference lp = (ListPreference) findPreference("sensor_data");
		lp.setEntries(entries);
		lp.setEntryValues(entryValues);
	}
	
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		addPreferencesFromResource(R.xml.moses_pref);
		loadSensors();
	}

}
