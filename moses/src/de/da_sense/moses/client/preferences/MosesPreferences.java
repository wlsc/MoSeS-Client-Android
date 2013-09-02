/*******************************************************************************
 * Copyright 2013
 * Telecooperation (TK) Lab
 * Technische Universität Darmstadt
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package de.da_sense.moses.client.preferences;

import android.app.ActionBar;
import android.app.AlertDialog;
import android.app.Dialog;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.text.Editable;
import android.text.InputType;
import android.text.Spanned;
import android.text.TextWatcher;
import android.text.style.ForegroundColorSpan;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
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
	 * Key for the shared preference EMAIL
	 */
	public static String PREF_EMAIL = "email_pref";
	
	/**
	 * Key for the shared preference PASSWORD
	 */
	public static String PREF_PASSWORD = "password_pref";
	
	/**
	 * Key for the shared preference DEVICENAME
	 */
	public static String PREF_DEVICENAME = "devicename_pref";
	
	/**
	 * Key for the shared preference DEVICENAME
	 */
	public static String PREF_DEVICEID = "deviceid_pref";
	
	/**
	 * Key for the shared preference for showing the splash screen on the start
	 */
	public static String PREF_SHOW_SPLASHSCREEN = "splashscreen_pref";
	
	/**
	 * Key for the shared preference for showing the notifications in status bar
	 */
	public static String PREF_SHOW_STATUSBAR_NOTIFICATIONS = "statusbar_notifications_pref";
	
	/**
	 * Key for the clients ID when using Google Clous Messaging (GCM)
	 */
	public static String PREF_GCM_ID = "c2dm_pref";

	/**
	 * @see android.preference.PreferenceActivity#onCreate(android.os.Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// Actionbar
		ActionBar actionBar = getActionBar();
		actionBar.setTitle("Settings");
		actionBar.setDisplayShowTitleEnabled(true);
		actionBar.setDisplayHomeAsUpEnabled(true);
		
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
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		
		 if (item.getItemId() == android.R.id.home) {
	           onBackPressed();
	           return true;
	       } 
		
		return super.onOptionsItemSelected(item);
	}
	
	
	/*
	 * Fragment
	 */
	public static class PrefsFragment extends PreferenceFragment {
		
		private Button mPositiveButtonDeviceId;
		private String mErrorDeviceIdErrorMessage;
		
		@Override
		public void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);
			
			// load preferences from XML resource
			addPreferencesFromResource(R.xml.moses_pref);
			
			getActivity();
			
			/*
			 * Change the default behavior of the EditTextPreference for changing the deviceId
			 */
			final EditTextPreference prefDevId = (EditTextPreference) findPreference(getString(R.string.deviceNamePreferenceKey));
			final EditText devIdEditText = prefDevId.getEditText();
			prefDevId.setOnPreferenceClickListener(new OnPreferenceClickListener() {
				
				@Override
				public boolean onPreferenceClick(Preference preference) {
					// disable suggestions and spell checking
					devIdEditText.setInputType(InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
					devIdEditText.setSelection(devIdEditText.getText().length());
					/*
					 * prevent the preference dialog from closing if the user clicks on ok
					 * and entered data could not be validated
					 */
					final Dialog dialog = prefDevId.getDialog();
					
					final AlertDialog alertDialog = (AlertDialog) dialog; // a trick for getting controls over the button
					mPositiveButtonDeviceId = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
					
					mPositiveButtonDeviceId.setOnClickListener(new OnClickListener() {
						
						public void onClick(View v) {
							if(mErrorDeviceIdErrorMessage != null){
								devIdEditText.setError(mErrorDeviceIdErrorMessage);
							}
							else{
								// trim the device name entered by he user, before persisting and sending it to the server
								devIdEditText.setText(devIdEditText.getText().toString().trim());
								prefDevId.onClick(dialog, AlertDialog.BUTTON_POSITIVE);
								dialog.dismiss();
							}
						}
					});
					
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
					// mark invalid characters with red
					// only alphanumeric characters and underscores are allowed
					mErrorDeviceIdErrorMessage = null;
					devIdEditText.setError(null); // remove previous error as soon as the user starts typing
					String input ="";
					for(int i=0; i<s.length(); i++){
						char ch = s.charAt(i);
						input+=ch;
	                	// only alphanumeric characters and underscores allowed
                        if (!Character.isLetterOrDigit(ch) && ch != '_' && ch!=' ') {
                        	ForegroundColorSpan redSpan = new ForegroundColorSpan(Color.RED);
                        	s.setSpan(redSpan, i, i+1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                        	mErrorDeviceIdErrorMessage = getString(R.string.dev_id_invalid_character_error);
                        	}
                        }
					if(input.trim().length() == 0){
						mErrorDeviceIdErrorMessage = getString(R.string.dev_id_empty_error);
					}
				}
			});
			
			/*
	         * When user clicks the "Go" act as if he clicked the OK button
	         */
	        devIdEditText.setOnEditorActionListener(new OnEditorActionListener() {
				
				public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
					 if ((event != null && (event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) || (actionId == EditorInfo.IME_ACTION_DONE)){
						 mPositiveButtonDeviceId.performClick();
					}
					return false;
				}
			});
			
		}
		
		
	}
	
	
	
}
