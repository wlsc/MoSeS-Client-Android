package de.da_sense.moses.client;

import android.app.Activity;
import android.os.Bundle;
import android.text.method.LinkMovementMethod;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

/**
 * Shows about view mask
 * @author Wladimir Schmidt
 *
 */
public class AboutActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_about);
		getActionBar().setDisplayHomeAsUpEnabled(true);
		
		TextView tkLabMoreInfo = (TextView) findViewById(R.id.tk_lab_more_info);
	    tkLabMoreInfo.setMovementMethod(LinkMovementMethod.getInstance());
	    
	    TextView aboutFounderDevEmail1 = (TextView) findViewById(R.id.about_founder_dev_email_1);
	    aboutFounderDevEmail1.setMovementMethod(LinkMovementMethod.getInstance());
	    
	    TextView aboutFounderDevEmail2 = (TextView) findViewById(R.id.about_founder_dev_email_2);
	    aboutFounderDevEmail2.setMovementMethod(LinkMovementMethod.getInstance());
	    
	    TextView aboutSupervisorText = (TextView) findViewById(R.id.about_supervisor);
	    aboutSupervisorText.setMovementMethod(LinkMovementMethod.getInstance());
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

}
