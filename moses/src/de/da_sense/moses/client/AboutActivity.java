package de.da_sense.moses.client;

import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;
import android.text.method.LinkMovementMethod;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * Shows about view mask
 * @author Wladimir Schmidt
 * @author Zijad Maksuti
 *
 */
public class AboutActivity extends Activity {
	
	private int mClickCounter = 0;
	private long mLastClick = 0L;
	private long mCounterThreshold = 300000000L;
	private int mNumberOfGoatClicks = 6;
	private Dialog mDialog;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_about);
		getActionBar().setDisplayHomeAsUpEnabled(true);
		
		TextView t2 = (TextView) findViewById(R.id.tk_lab_more_info);
	    t2.setMovementMethod(LinkMovementMethod.getInstance());
	    
	    LinearLayout aboutLinearLayout = (LinearLayout) findViewById(R.id.about_linear_layout);
	    aboutLinearLayout.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				long timestamp = System.nanoTime();
				if(timestamp - mLastClick <= mCounterThreshold)
					mClickCounter++;
				else
					mClickCounter=0;
				if(mClickCounter == mNumberOfGoatClicks)
					showGoat();
				mLastClick = timestamp;
			}
		});
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		
		switch (item.getItemId()) {
	        case android.R.id.home:
	        	onBackPressed();
	            return true;
	        default:
	            return super.onOptionsItemSelected(item);
		}
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		return false;
	}
	
	private void showGoat() {

		mDialog = new Dialog(this);
		mDialog.setContentView(R.layout.goat);
		mDialog.setTitle(getString(R.string.dialog_goat));
        mDialog.show();
    }

	/* (non-Javadoc)
	 * @see android.app.Activity#onPause()
	 */
	@Override
	protected void onPause() {
		super.onPause();
		if(mDialog != null)
			mDialog.cancel();
	}
	
	

}
