package de.da_sense.moses.client.preferences;

import android.app.ActionBar;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.text.Editable;
import android.text.InputType;
import android.text.Spanned;
import android.text.TextWatcher;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.widget.EditText;
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
		getFragmentManager().beginTransaction().replace(android.R.id.content, new PrefsFragment()).commit();
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
		
		private static String LOG_TAG = PreferenceFragment.class.getSimpleName();
		@Override
		public void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);
			
			// load preferences from XML resource
			addPreferencesFromResource(R.xml.moses_pref);
			
			getActivity();
			
			/*
			 * Change the default behaviour of the EditTextPreference for changing the deviceId
			 */
			final EditTextPreference prefDevId = (EditTextPreference) findPreference(getString(R.string.deviceIDPreferenceKey));
			final EditText devIdEditText = prefDevId.getEditText();
			prefDevId.setOnPreferenceClickListener(new OnPreferenceClickListener() {
				
				@Override
				public boolean onPreferenceClick(Preference preference) {
					// disable suggestions and spell checking
					devIdEditText.setInputType(InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
					devIdEditText.setSelection(devIdEditText.getText().length());
					return true;
				}
			});
			
			// give feedback to the user if he enters a non valid character
			devIdEditText.addTextChangedListener(new TextWatcher() {
				
				@Override
				public void onTextChanged(CharSequence s, int start, int before, int count) {
					// TODO Auto-generated method stub
				}
				
				@Override
				public void beforeTextChanged(CharSequence s, int start, int count,
						int after) {
					// TODO Auto-generated method stub
				}
				
				@Override
				public void afterTextChanged(Editable s) {
					// show error message if user entered non valid character
					// only alphanumeric characters and and underscores are allowed
					boolean isErrorFound = false;
					for(int i=0; i<s.length(); i++){
						char ch = s.charAt(i);
	                	// only alphanumeric characters and underscores allowed
                        if (!Character.isLetterOrDigit(ch) && ch != '_' && ch != ' ') {
                        	ForegroundColorSpan redSpan = new ForegroundColorSpan(Color.RED);
                        	s.setSpan(redSpan, i, i+1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                        	isErrorFound = true;
                        	}
                        }
					if(isErrorFound)
						devIdEditText.setError(getString(R.string.dev_id_invalid_character_error));
					else
						devIdEditText.setError(null);
				}
			});
						
			prefDevId.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
				
				@Override
				public boolean onPreferenceChange(Preference preference, Object newValue) {
					Log.d(LOG_TAG, preference.toString());
					Log.d(LOG_TAG, newValue.toString());
					return false;
				}
			});
		}
		
	}
	
	
	
}
