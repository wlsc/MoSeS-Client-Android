package de.da_sense.moses.client.preferences;

import java.util.List;

import android.app.ActionBar;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import de.da_sense.moses.client.R;
import de.da_sense.moses.client.service.MosesService;

/**
 * The Moses preference screen.
 * 
 * @author Jaco Hofmann, Wladimir Schmidt
 * @author Zijad Maksuti
 * 
 */
public class MosesPreferences extends PreferenceActivity {

	/**
	 * @see android.preference.PreferenceActivity#onCreate(android.os.Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// Actionbar
		ActionBar ab = getActionBar();
		ab.setTitle("Settings");
		ab.setDisplayShowTitleEnabled(true);
	}
	
	public void onBuildHeaders(List<Header> target){
		loadHeadersFromResource(R.xml.preference_headers, target);
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
	
	
	/*
	 * Fragment
	 */
	public static class PrefsFragment extends PreferenceFragment {

		@Override
		public void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);

			// load preferences from XML resource
			addPreferencesFromResource(R.xml.moses_pref);
		}
		
	}
	
}
