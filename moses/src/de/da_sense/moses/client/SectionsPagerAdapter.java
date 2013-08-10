package de.da_sense.moses.client;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.util.Log;

/**
 * 
 * Returns a fragment of the specific position in {@link WelcomeActivity}
 * 
 * @author Zijad Maksuti
 *
 */
public class SectionsPagerAdapter extends FragmentPagerAdapter {
	
	/** Available user studies tab. Constant for the tab selection logic. */
	protected final static int TAB_AVAILABLE = 0;
	/** Running user studies tab. Constant for the tab selection logic. */
	protected final static int TAB_RUNNING = 1;
	/** History of user studies tab. Constant for the tab selection logic. */
	protected final static int TAB_HISTORY = 2;
	
	private Context mContext;
	
	private static final String LOG_TAG = SectionsPagerAdapter.class.getName(); 

	/**
	 * 
	 * @param fm
	 * @param context the context of the activity that is using this adapter
	 */
    public SectionsPagerAdapter(FragmentManager fm, Context context) {
        super(fm);
        mContext = context;
    }

    @Override
    public Fragment getItem(int i) {
    	Fragment result = null;
        switch (i) {
            case TAB_AVAILABLE:{
				Log.d(LOG_TAG, "returning TAB_AVAILABLE");
				result = new AvailableFragment();
				break;
			}
            case TAB_RUNNING:{
				Log.d(LOG_TAG, "returning TAB_RUNNING");
				result = new RunningFragment();
				break;
			}

            case TAB_HISTORY:{
				Log.d(LOG_TAG, "returning TAB_HISTORY");
				result = new HistoryFragment();
				break;
			}
            default:{
            	Log.w(LOG_TAG, "there is no fragment on the position " + i + " null will be returned");
            	break;
            }
        }
        return result;
    }

    @Override
    public int getCount() {
        return 3;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        switch(position){
        case TAB_AVAILABLE:
        	return mContext.getString(R.string.tab_available);
        case TAB_RUNNING:
        	return mContext.getString(R.string.tab_running);
        case TAB_HISTORY:
        	return mContext.getString(R.string.tab_history);
    	default:{
    		Log.e(LOG_TAG, "getPageTitle() invalid position " + position + " returning null");
    		return null;
    	}
        }
    }
}