//package de.da_sense.moses.client;
//
//import android.support.v4.app.FragmentTransaction;
//
//import com.actionbarsherlock.app.ActionBar.Tab;
//import com.actionbarsherlock.app.ActionBar.TabListener;
//import com.actionbarsherlock.app.SherlockFragmentActivity;
//import com.actionbarsherlock.app.SherlockListFragment;
//
//import de.da_sense.moses.client.util.Log;
//
///**
// * 
// * @author Sandra Amend
// *
// * @param <T> class which extends SherlockListFragment
// */
//public class MosesTabListener<T extends SherlockListFragment> implements TabListener {
//	
//	/** the Fragment of the tab */
//	private SherlockListFragment mFragment;
//	/** the Activity of the tab */
//	private final SherlockFragmentActivity mActivity;
//	/** the tag of the tab */
//	private final String mTag;
//	/** the class of the tab */
//	private final Class<T> mClass;
//
//	/**
//	 * Constructor for the TabListener.
//	 * @param activity the activity of the tab
//	 * @param tag the tag for the tab
//	 * @param clz the class of the tab
//	 */
//	public MosesTabListener(SherlockFragmentActivity activity, String tag, Class<T> clz) {
//		mActivity = activity;
//		mTag = tag;
//		mClass = clz;
//	}
//	
//	/*
//	 * (non-Javadoc)
//	 * Callback Methods for com.actionbarsherlock.app.ActionBar.TabListener
//	 * @see com.actionbarsherlock.app.ActionBar.TabListener#onTabSelected(com.actionbarsherlock.app.ActionBar.Tab, android.support.v4.app.FragmentTransaction)
//	 */
//	@Override
//	public void onTabSelected(Tab tab, FragmentTransaction ft) {
//		// check if the Fragment is already initialized
//		SherlockListFragment preInitializedFragment = 
//				(SherlockListFragment) mActivity.getSupportFragmentManager()
//				.findFragmentByTag(mTag);
//		
//		// set the view of the tab
//		mActivity.setContentView(R.layout.activity_main);
//		
//		if (mFragment == null && preInitializedFragment == null) {
//			// instantiate the Fragment and add it
//			Log.d("TabListener", "onTabSelected: mActivity = " + mActivity + " mClass = " + mClass.getName());
//			mFragment = (SherlockListFragment) SherlockListFragment
//					.instantiate(mActivity, mClass.getName());
//			Log.d("TabListener", "onTabSelected - R.id.listView = " + mActivity.findViewById(R.id.listView));
//			ft.add(R.id.listView, mFragment, mTag);
//		} else if (mFragment != null) {
//			// the Fragment already exists so just attach it
//			Log.d("TabListener", "Fragment already exists, so just attached it: " + mFragment.getTag());
//			ft.attach(mFragment);
//		} else if (preInitializedFragment != null) {
//			Log.d("TabListener", "preInitializedFragment, so just attached it: " + preInitializedFragment.getTag());
//	        ft.attach(preInitializedFragment);
//	        mFragment = preInitializedFragment;
//	    }
//		
//		// set the active tab in the MainActivity
//		// solves the overlying of the tabs content on orientation change 
//		Log.d("TabListener", "mTag = " + mTag + " mActivity = " + mActivity
//				.getLocalClassName());
//		if (mTag.equals("available")) {
//			((MainActivity) mActivity).setActiveTab(MainActivity.TAB_AVAILABLE);
//			Log.d("TabListener", "activeTab set to 0");
//		} else if (mTag.equals("running")) {
//			((MainActivity) mActivity).setActiveTab(MainActivity.TAB_RUNNING);
//			Log.d("TabListener", "activeTab set to 1");
//		} else if (mTag.equals("history")) {
//			((MainActivity) mActivity).setActiveTab(MainActivity.TAB_HISTORY);
//			Log.d("TabListener", "activeTab set to 2");
//		}
//	}
//
//	/*
//	 * (non-Javadoc)
//	 * @see com.actionbarsherlock.app.ActionBar.TabListener#onTabUnselected(com.actionbarsherlock.app.ActionBar.Tab, android.support.v4.app.FragmentTransaction)
//	 */
//	@Override
//	public void onTabUnselected(Tab tab, FragmentTransaction ft) {
//		Log.d("MosesTabListener", mFragment.getTag() + "Fragment detached");
//		if (mFragment != null) {
//			ft.detach(mFragment);
//		}
//	}
//	
//	/*
//	 * (non-Javadoc)
//	 * @see com.actionbarsherlock.app.ActionBar.TabListener#onTabReselected(com.actionbarsherlock.app.ActionBar.Tab, android.support.v4.app.FragmentTransaction)
//	 */
//	@Override
//	public void onTabReselected(Tab tab, FragmentTransaction ft) {
//		// nothing to do here
//	}
//
//}
