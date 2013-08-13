package de.da_sense.moses.client;

import de.da_sense.moses.client.abstraction.apks.ExternalApplication;
import de.da_sense.moses.client.abstraction.apks.InstalledExternalApplication;
import de.da_sense.moses.client.abstraction.apks.InstalledExternalApplicationsManager;
import de.da_sense.moses.client.userstudy.Form;
import de.da_sense.moses.client.userstudy.Survey;
import android.app.ActionBar;
import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;

/**
 * Instance of this class is used to visualize a {@link Survey} instance.
 * 
 * @author Sandra Amend, Wladimir Schmidt
 * @author Zijad Maksuti
 * 
 */
public class SurveyActivity extends Activity {
	/**
	 * Defining a log tag to this class
	 */
	private static final String LOG_TAG = SurveyActivity.class.getName();
	
	/**
	 * The id of the {@link InstalledExternalApplication} whose {@link Survey} instance
	 * will be visualized by this {@link SurveyActivity}.
	 */
	private String mAPKID = null;
	
//	/**
//	 * The {@link Survey} represented by this {@link SurveyActivity}.
//	 */
//	private Survey mSurvey;

	/**
	 * @see android.app.Activity.onCreate(Bundle savedInstanceState)
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		if(mAPKID == null)
			if(savedInstanceState == null)
				mAPKID = getIntent().getStringExtra(ExternalApplication.KEY_APK_ID);
			else
				mAPKID = savedInstanceState.getString(ExternalApplication.KEY_APK_ID, null);
		
		if(mAPKID == null)
			Log.e(LOG_TAG, "onCreate() apkid was not in the bundle nor in the intent");
		
		InstalledExternalApplication apk = InstalledExternalApplicationsManager.getInstance().getAppForId(mAPKID);
		Survey survey = apk.getSurvey();
		Form form = survey.getForms().get(0);

		FormFragment formFragment;
		if (savedInstanceState == null) {
			Log.d(LOG_TAG, "savedInstanceState == null");
			// during initial setup plug in the detail fragment
			formFragment = new FormFragment();
			formFragment.setmFormID(form.getId());
			formFragment.setRetainInstance(true);
			formFragment.setArguments(getIntent().getExtras());
			getFragmentManager().beginTransaction()
					.add(android.R.id.content, formFragment).commit();
		} else {
			Log.d(LOG_TAG, "savedInstanceState != null");
			formFragment = new FormFragment();
			formFragment.setmFormID(form.getId());
			formFragment.setRetainInstance(true);
			formFragment.setArguments(savedInstanceState);
			getFragmentManager().beginTransaction()
					.add(android.R.id.content, formFragment).commit();
		}

		// get ActionBar and set AppIcon to direct to the "home screen"
		ActionBar ab = getActionBar();
		ab.setTitle(getString(R.string.actionbar_title_survey));
		ab.setDisplayHomeAsUpEnabled(true);
	}

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

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		Log.d(LOG_TAG, "onSaveInstanceState");
		outState.putAll(this.getIntent().getExtras());
		super.onSaveInstanceState(outState);
	}
	
	/**
	 * @see android.app.Activity#onRestoreInstanceState(android.os.Bundle)
	 */
	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		Log.d(LOG_TAG, "onRestoreInstanceState");
		super.onRestoreInstanceState(savedInstanceState);
	}
}