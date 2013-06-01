package de.da_sense.moses.client;

import android.app.ActionBar;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.MenuItem;

/**
 * questionnaires for a user study
 * 
 * @author Sandra Amend, Wladimir Schmidt
 * 
 */
public class QuestionnaireActivity extends FragmentActivity {
	/**
	 * Defining a log tag to this class
	 */
	private static final String TAG = "QuestionnaireActivity";

	/**
	 * @see android.app.Activity.onCreate(Bundle savedInstanceState)
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		QuestionnaireFragment questionnaire;
		if (savedInstanceState == null) {
			Log.d(TAG, "savedInstanceState == null");
			// during initial setup plug in the detail fragment
			questionnaire = new QuestionnaireFragment();
			questionnaire.setRetainInstance(true);
			questionnaire.setArguments(getIntent().getExtras());
			getFragmentManager().beginTransaction()
					.add(android.R.id.content, questionnaire).commit();
		} else {
			Log.d(TAG, "savedInstanceState != null");
			questionnaire = new QuestionnaireFragment();
			questionnaire.setRetainInstance(true);
			questionnaire.setArguments(savedInstanceState);
			getFragmentManager().beginTransaction()
					.add(android.R.id.content, questionnaire).commit();
		}

		// get ActionBar and set AppIcon to direct to the "home screen"
		ActionBar ab = getActionBar();
		ab.setDisplayHomeAsUpEnabled(true);
	}

	/**
	 * @see com.actionbarsherlock.app.SherlockFragmentActivity#onOptionsItemSelected(android.view.MenuItem)
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
        case android.R.id.home:
            // application icon in action bar clicked
        	onBackPressed();
            return true;
        default:
            return super.onOptionsItemSelected(item);
        }
	}

	/**
	 * @see android.support.v4.app.FragmentActivity#onSaveInstanceState(android.os.Bundle)
	 */
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		Log.d(TAG, "onSaveInstanceState");
		outState.putAll(this.getIntent().getExtras());
		super.onSaveInstanceState(outState);
	}
	
	/**
	 * @see android.app.Activity#onRestoreInstanceState(android.os.Bundle)
	 */
	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		Log.d(TAG, "onRestoreInstanceState");
		super.onRestoreInstanceState(savedInstanceState);
	}
}