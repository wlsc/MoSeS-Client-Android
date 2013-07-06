package de.da_sense.moses.client;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.EditText;
import de.da_sense.moses.client.com.ConnectionParam;
import de.da_sense.moses.client.com.NetworkJSON.BackgroundException;
import de.da_sense.moses.client.com.ReqTaskExecutor;
import de.da_sense.moses.client.com.requests.RequestLogin;
import de.da_sense.moses.client.service.helpers.Login;
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
	
	/*
	 * (non-Javadoc)
	 * @see android.support.v4.app.FragmentActivity#onCreate(android.os.Bundle)
	 */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        setContentView(R.layout.login);
    }
    
    /**
     * Handles the click on the login button.
     * Gets called on click on the login button (as specified in login.xml)
     * @param v View
     */
    public void handleClick(View v) {
    	final EditText email = (EditText) findViewById(R.id.login_email);
    	final EditText password = (EditText) findViewById(R.id.login_password);
    	// check if fields are empty -> no login try
    	if (email.getText().toString().isEmpty() 
    			|| password.getText().toString().isEmpty()) {
    		return;
    	}
    	// show ProgressDialog while checking and verifying entered credentials
    	d = new ProgressDialog(LoginActivity.this, ProgressDialog.STYLE_SPINNER);
		d.setTitle(getString(R.string.checking_credentials));
		d.setMessage(getString(R.string.verifying_credentials));
		d.show();
		// request a login/session from the server
		new RequestLogin(new ReqLogin(), 
				email.getText().toString(), 
				password.getText().toString()).send();
    }
    
    /**
     * Called if we got a SessionID from the server.
     */
    private void valid() {
    	Log.d("LoginActivity", "valid() called");
		d.dismiss();
		// get username and password
		final EditText email = (EditText) findViewById(R.id.login_email);
		final EditText password = (EditText) findViewById(R.id.login_password);
		// set the result of the Activity and put the username and password
		Intent resultData = new Intent();
		resultData.putExtra(Login.PREF_EMAIL, email.getText().toString());
		resultData.putExtra(Login.PREF_PASSWORD, password.getText().toString());
		setResult(Activity.RESULT_OK, resultData);

    	Log.d("LoginActivity", "valid(): email = " + email.getText().toString() +
    			"\npassword = " + password.getText().toString());
		
		finish();
	}

    /**
     * Called if we didn't get a SessionID from the server.
     */
	private void invalid() {
		Log.d("LoginActivity", "invalid() called, d.dismiss() follows");
		d.dismiss();
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
					// -> wrong username or password
					Log.d("LoginActivity", "No valid session id received");
					Log.d("LoginActivity", getString(R.string.wrong_credentials));
					
					d.setMessage(getString(R.string.wrong_credentials));
					h.postDelayed(new Runnable() {
						@Override
						public void run() {
							invalid();
						}
					}, 2000);
				} else {
					// we did get a session id
					Log.d("LoginActivity", "Received valid session id: " 
							+ j.getString("SESSIONID"));
					valid();
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
