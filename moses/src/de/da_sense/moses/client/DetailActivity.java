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
package de.da_sense.moses.client;

import android.app.ActionBar;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.MenuItem;
import de.da_sense.moses.client.util.Log;

/**
 * Represents the details of a userstudy.
 * 
 * @author Sandra Amend
 * @author Wladimir Schmidt
 * @author Zijad Maksuti
 */
public class DetailActivity extends FragmentActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// check orientation and screen size
		// only for devices with API Level 11 or higher
		if (isAtLeastHoneycomb()
				&& getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE
				&& getResources().getConfiguration().isLayoutSizeAtLeast(
						Configuration.SCREENLAYOUT_SIZE_LARGE)) {
			Log.d("DetailActivity", "orientation = landscape");
			Log.d("DetailActivity", "layout at least size large");
			// If the screen is now in landscape mode, we can show the
			// dialog in-line with the list so we don't need this activity.
			finish();
			return;
		}

		// get the Detail Fragment to check later if it is null or not
		DetailFragment details = null;

		Log.d("DetailActivity", "savedInstanceState == null || details == null");
		// during initial setup plug in the detail fragment
		details = new DetailFragment();
		details.setRetainInstance(true);
		details.setArguments(getIntent().getExtras());
		getSupportFragmentManager().beginTransaction()
				.add(android.R.id.content, details).commit();

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
			// go back to list view (MainActivity)
			onBackPressed();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		Log.d("DetailActivity", "onSaveInstanceState");
		super.onSaveInstanceState(outState);
	}

	/**
	 * @see android.app.Activity#onRestoreInstanceState(android.os.Bundle)
	 */
	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		Log.d("DetailActivity", "onRestoreInstanceState");
		super.onRestoreInstanceState(savedInstanceState);
	}

	/**
	 * Helper method to check if we have at least API 11 or higher.
	 * 
	 * @return
	 */
	@Deprecated
	public boolean isAtLeastHoneycomb() {
		String version = android.os.Build.VERSION.RELEASE;
		if (version.startsWith("1.") || version.startsWith("2.")) {
			return false;
		} else {
			return true;
		}
	}
}
