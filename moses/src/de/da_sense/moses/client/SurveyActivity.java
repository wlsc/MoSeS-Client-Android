package de.da_sense.moses.client;

import android.app.ActionBar;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.MenuItem;
import de.da_sense.moses.client.abstraction.apks.ExternalApplication;
import de.da_sense.moses.client.abstraction.apks.HistoryExternalApplicationsManager;
import de.da_sense.moses.client.abstraction.apks.InstalledExternalApplication;
import de.da_sense.moses.client.abstraction.apks.InstalledExternalApplicationsManager;
import de.da_sense.moses.client.userstudy.Survey;

/**
 * Instance of this class is used to visualize a {@link Survey} instance.
 * 
 * @author Sandra Amend, Wladimir Schmidt
 * @author Zijad Maksuti
 * 
 */
public class SurveyActivity extends FragmentActivity {
	
	/**
	 * Adapter for showing {@link FormFragment}s.
	 */
    private SurveyActivityPagerAdapter mAdapter;
    
    private ViewPager mViewPager;
	
	/**
	 * The id of the {@link InstalledExternalApplication} whose {@link Survey} instance
	 * will be visualized by this {@link SurveyActivity}.
	 */
	private String mAPKID = null;

	/**
	 * Defining a log tag to this class
	 */
	private static final String LOG_TAG = SurveyActivity.class.getName();

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_surveyactivity);
		
		if(mAPKID == null)
			if(savedInstanceState == null)
				mAPKID = getIntent().getStringExtra(ExternalApplication.KEY_APK_ID);
			else
				mAPKID = savedInstanceState.getString(ExternalApplication.KEY_APK_ID, null);
		
		if(mAPKID == null)
			Log.e(LOG_TAG, "onCreate() apkid was not in the bundle nor in the intent");
		
		int belongsTo = getIntent().getIntExtra(WelcomeActivity.KEY_BELONGS_TO, WelcomeActivityPagerAdapter.TAB_RUNNING);
		ExternalApplication apk;
		if(belongsTo == WelcomeActivityPagerAdapter.TAB_HISTORY)
			apk = HistoryExternalApplicationsManager.getInstance().getAppForId(mAPKID);
		else
			apk = InstalledExternalApplicationsManager.getInstance().getAppForId(mAPKID);
		
		// get ActionBar and set AppIcon to direct to the "home screen"
		ActionBar ab = getActionBar();
		ab.setTitle(String.format(getString(R.string.actionbar_title_survey), apk.getName()));
		ab.setDisplayHomeAsUpEnabled(true);
		
		// set the adapter
		mAdapter = new SurveyActivityPagerAdapter(getSupportFragmentManager(), apk);
		mViewPager = (ViewPager) findViewById(R.id.surveyActivityPager);
        mViewPager.setAdapter(mAdapter);
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