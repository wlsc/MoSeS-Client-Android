package de.da_sense.moses.client;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Patterns;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import de.da_sense.moses.client.abstraction.HardwareAbstraction;
import de.da_sense.moses.client.com.ConnectionParam;
import de.da_sense.moses.client.com.NetworkJSON.BackgroundException;
import de.da_sense.moses.client.com.ReqTaskExecutor;
import de.da_sense.moses.client.com.requests.RequestLogin;
import de.da_sense.moses.client.preferences.MosesPreferences;
import de.da_sense.moses.client.util.Log;

/**
 * Login screen for the application.
 * @author Sandra Amend, Jaco Hofmann
 * @author Zijad Maksuti
 *
 */
public class LoginActivity extends Activity {
	/** ProgressDialog showed after pressing the login button */
	private ProgressDialog d = null;
	/** handler for the runnables */
	private Handler h = new Handler();
	
	/**
	 * The name of the file containing stored users credentials.
	 * First line of this file contains the email, the second one the password.
	 * If the file is empty or non-existent, no credentials were saved.
	 */
	public static final String FILENAME_CREDENTIALS = "credentials_filename";
	private static final String LOG_TAG = LoginActivity.class.getName();
	
	/*
	 * Views of this activity
	 */
	private EditText editTextEmail;
	private EditText editTextPassword;
	private CheckBox checkBoxRemember;
	private Button buttonLogin;
	
	private LoginActivity lg;
	
	/*
	 * (non-Javadoc)
	 * @see android.support.v4.app.FragmentActivity#onCreate(android.os.Bundle)
	 */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        getActionBar().hide();
        
        setContentView(R.layout.login);
        
        lg = this;
        editTextEmail = (EditText) findViewById(R.id.login_email);
        editTextPassword = (EditText) findViewById(R.id.login_password);
        checkBoxRemember = (CheckBox) findViewById(R.id.checkbox_login_remember_me);
        buttonLogin = (Button) findViewById(R.id.login_button);
        
        // set the previously persisted credentials if any
        try {
			String[] credentials = readCredentials();
			if(credentials != null){
				editTextEmail.setText(credentials[0]);
				editTextPassword.setText(credentials[1]);
			}
		} catch (IOException e) {
			Log.e(LOG_TAG, "onCreate: file containing users credentials could not be read");
			e.printStackTrace();
		}
        
        // set the focus and cursor on an empty field (email or password)
        if(editTextEmail.getText().toString().isEmpty())
        	editTextEmail.requestFocus();
        else
        	if(editTextPassword.getText().toString().isEmpty())
        		editTextPassword.requestFocus();
        
        /*
         * When user clicks the "Go" button on keyboard and the cursor is set on the password fields,
         * it should be equivalent to pressing the login button
         */
        editTextPassword.setOnEditorActionListener(new OnEditorActionListener() {
			
			public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
				 if ((event != null && (event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) || (actionId == EditorInfo.IME_ACTION_DONE)){
					 lg.handleClick(buttonLogin);
				}
				return false;
			}
		});  
    }
    
    /**
     * Handles the click on the login button.
     * Gets called on click on the login button.
     * @param v View
     */
    public void handleClick(View v) {
    	String email = editTextEmail.getText().toString().trim();
		String password = editTextPassword.getText().toString();
		String deviceID = HardwareAbstraction.extractDeviceIdFromSharedPreferences();
    	
    	// validate the format of email and password before sending anything to server
    	if (!validateEmail() || !validatePassword()) {
    		return;
    	}
    	// show ProgressDialog while checking and verifying entered credentials
    	d = new ProgressDialog(LoginActivity.this, ProgressDialog.STYLE_SPINNER);
		d.setTitle(getString(R.string.checking_credentials));
		d.setMessage(getString(R.string.verifying_credentials));
		d.show();
		// request a login/session from the server
		new RequestLogin(new ReqLogin(),  email, password, deviceID).send();
    }
    
    /**
     * Called if we got a SessionID from the server.
     * @param deviceName the name of the device returned from the server (may be null)
     */
    private void valid(String deviceName) {
    	Log.d("LoginActivity", "valid() called");
		d.dismiss();
		// get email and password
		String email = editTextEmail.getText().toString().trim();
		String password = editTextPassword.getText().toString();
		
		// set the result of the Activity and put the email and password
		Intent resultData = new Intent();
		resultData.putExtra(MosesPreferences.PREF_EMAIL, email);
		resultData.putExtra(MosesPreferences.PREF_PASSWORD, password);
		resultData.putExtra(MosesPreferences.PREF_DEVICENAME, deviceName);
		setResult(Activity.RESULT_OK, resultData);

    	Log.d(LOG_TAG, "valid(): email = " + email +
    			"\npassword = " + password);
    	
    	// persist the valid credentials if the box is set
    	if(checkBoxRemember.isChecked())
    		try {
    			saveCredentials(email, password);
    			} catch (IOException e) {
    				Log.e(LOG_TAG, "valid(): there was a problem storing users credentials");
    				e.printStackTrace();
    				
    			}
    	else
        	/* 
        	 * delete users credentials because the box was not checked. We also want to forget
        	 * previously stored credentials
        	 */
        	deleteFile(FILENAME_CREDENTIALS);
    	
    	finish();
    	
    }
    	
    	

    /**
     * Called if we didn't get a SessionID from the server.
     */
	private void invalid() {
		Log.d("LoginActivity", "invalid() called, d.dismiss() follows");
		d.dismiss();
		showWrongCredentials();
	}
    
	/**
	 * Persists users credentials to a private file.
	 * @param email the email (should not be null or empty String)
	 * @param password the password (should not be null or empty String)
	 * @throws IOException if the file for storing the users credentials could not be opened
	 * or the credentials could not be written to it. If one of the consumed arguments is null or an empty string,
	 * this method will not persist anything.
	 */
	private void saveCredentials(String email, String password) throws IOException{
		if(email != null && !email.isEmpty()&& password != null && !password.isEmpty()){
			StringBuffer sb = new StringBuffer();
	    	sb.append(email).append("\n").append(password);
	    	FileOutputStream fos = openFileOutput(FILENAME_CREDENTIALS, Context.MODE_PRIVATE);
	    	fos.write(sb.toString().getBytes());
	    	fos.close();
		}
		else{
			Log.w(LOG_TAG, "saveCredentials: persistation skipped; provided email or password are empty");
		}
	}

	/**
	 * Returns users credentials, that are persisted to the private file.
	 * @return An array consisting of two entries. 0th entry contains users email, 1st entry the password.
	 * If the file was empty or users email or password are not contained in it, this method returns null. 
	 * @throws IOException if the file containing the credentials could not be read.
	 */
	private String[] readCredentials() throws IOException {
		String[] result = null;
		FileInputStream fis = null;
		try {
			fis = openFileInput(FILENAME_CREDENTIALS);
		} catch (Exception e) {
			Log.i(LOG_TAG, "readCredentials: no credentials to read");
			return null;
		}
		BufferedReader br = new BufferedReader(new InputStreamReader(fis));
		StringBuffer sb = new StringBuffer();
		String line = null;
		while((line = br.readLine()) != null)
			sb.append(line).append("\n");
		fis.close();
		if(sb.length() != 0)
			result = sb.toString().split("\n");
		if(result.length != 2)
			return null;
		else{
			String email = result[0];
			String password = result[1];
			if(!email.isEmpty() && !password.isEmpty())
				return result;
			else
				return null;
		}
	}
	
	/**
	 * This method validates the email provided by the user. It shows the user proper messages,
	 * if the entered email is not well formated or missing.
	 * @return true if the entered email is well formated.
	 */
	private boolean validateEmail(){
		String email = editTextEmail.getText().toString().trim();
		if(email.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()){
			editTextEmail.setError(getString(R.string.login_hint_bad_email));
			return false;
		}
		return true;
	}
	
	/**
	 * This method validates the password provided by the user. It shows the user a proper messages,
	 * if the entered password is not longer than 6 .
	 * @return true if the entered email is well formated.
	 */
	private boolean validatePassword(){
		String password = editTextPassword.getText().toString();
		if(password.length() <6){
			editTextPassword.setError(getString(R.string.login_hint_bad_pass));
			return false;
		}
		return true;
	}
	
	/**
	 * This method shows to the user that his email or password
	 * were wrong and the authentication failed.
	 */
	private void showWrongCredentials(){
		editTextEmail.setError(getString(R.string.wrong_credentials));
		editTextPassword.setError(getString(R.string.wrong_credentials));
	}

	/**
	 * Handles the login request.
	 */
    private class ReqLogin implements ReqTaskExecutor {
    	/**
    	 * If there is no Internet connection, we display a message.
    	 */
		@Override
		public void handleException(Exception e) {
			Log.d("LoginActivity", getString(R.string.check_internet));
			d.setMessage(getString(R.string.check_internet));
			h.postDelayed(new Runnable() {
				@Override
				public void run() {
					invalid();
				}
			}, 6000);
		}

		/**
		 * After a login was requested from the server.
		 */
		@Override
		public void postExecution(String s) {
			try {
				JSONObject j = new JSONObject(s);
				if (j.getString("SESSIONID").equals("NULL")) {
					// we didn't get a session id 
					// -> wrong email or password
					Log.d("LoginActivity", "No valid session id received");
					Log.d("LoginActivity", getString(R.string.wrong_credentials));
					invalid();
				} else {
					// we did get a session id
					Log.d("LoginActivity", "Received valid session id: " 
							+ j.getString("SESSIONID"));
					String deviceName = null;
					if(j.has("DEVICENAME")){
						deviceName = (String) j.get("DEVICENAME");
						Log.d(LOG_TAG, "Server responded with a DEVICENAME \""+deviceName+"\"");
					}
					valid(deviceName);
				}
			} catch (JSONException e) {
				Log.d("LoginActivity", "Handling JSON EXCEPTION");
				handleException(e);
			}
		}
		
		/**
		 * 
		 */
		@Override
		public void updateExecution(BackgroundException c) {
			Log.d("LoginActivity", "updateExecution called");
			if (c.c == ConnectionParam.EXCEPTION) {
				handleException(c.e);
			}
		}
    }
    
}
