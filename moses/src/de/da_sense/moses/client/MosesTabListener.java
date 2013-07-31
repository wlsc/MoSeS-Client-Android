package de.da_sense.moses.client;

import android.app.ActionBar.Tab;
import android.app.ActionBar.TabListener;
import android.app.Activity;
import android.app.FragmentTransaction;
import android.app.ListFragment;
import android.os.Bundle;
import android.util.Log;

/**
 * 
 * This tab listener controls switching between the tabs in the action bar of the {@link WelcomeActivity}.
 * It is responsible for reloading of tabs and animations when switching between tabs.
 * 
 * @author Jaco Hofmann
 * @author Sandra Amend
 * @author Wladimir Schmidt
 * @author Zijad Maksuti
 */
class MosesTabListener<T extends ListFragment> implements TabListener {

	/** the Fragment of the tab */
	private ListFragment mFragment;
	/** the Activity of the tab */
	private final Activity mActivity;
	/** the tag of the tab */
	private final String mTag;
	/** the class of the tab */
	private final Class<T> mClass;
	/** a bundle to check if we already have a fragment for this tab */
	private final Bundle mArgs;

	/**
	 * Constructor for the TabListener.
	 * 
	 * @param activity
	 *            the activity of the tab
	 * @param tag
	 *            the tag for the tab
	 * @param clz
	 *            the class of the tab
	 */
	MosesTabListener(Activity activity, String tag, Class<T> clz) {
		this(activity, tag, clz, null);
	}

	/**
	 * Constructor for the TabListener.
	 * 
	 * @param activity
	 * @param tag
	 * @param clz
	 * @param args
	 */
	private MosesTabListener(Activity activity, String tag, Class<T> clz,
			Bundle args) {
		mActivity = activity;
		mTag = tag;
		mClass = clz;
		mArgs = args;

		// Check to see if we already have a fragment for this tab, probably
		// from a previously saved state. If so, deactivate it, because our
		// initial state is that a tab isn't shown.
		mFragment = (ListFragment) mActivity.getFragmentManager()
				.findFragmentByTag(mTag);
		if (mFragment != null && !mFragment.isDetached()) {
			FragmentTransaction ft = mActivity.getFragmentManager()
					.beginTransaction();
			ft.detach(mFragment);
			ft.commit();
		}
	}

	/**
	 * Callback Methods for com.actionbarsherlock.app.ActionBar.TabListener
	 */
	@Override
	public void onTabSelected(Tab tab, FragmentTransaction ft) {
		// check if the Fragment is already initialized
		ListFragment preInitializedFragment = (ListFragment) mActivity
				.getFragmentManager().findFragmentByTag(mTag);

		// set the view of the tab
		mActivity.setContentView(R.layout.activity_main);

		if (mFragment == null && preInitializedFragment == null) {
			// instantiate the Fragment and add it
			Log.d("TabListener", "onTabSelected: mActivity = " + mActivity
					+ " mClass = " + mClass.getName());
			mFragment = (ListFragment) ListFragment.instantiate(mActivity,
					mClass.getName(), mArgs);
			mFragment.setRetainInstance(true); // XXX: inserted because of the
												// getActivty NullPointer

			Log.d("TabListener",
					"onTabSelected - R.id.listView = "
							+ mActivity.findViewById(R.id.listView));
			ft.add(R.id.listView, mFragment, mTag);
		} else if (mFragment != null) {
			// the Fragment already exists so just attach it
			Log.d("TabListener",
					"Fragment already exists, so just attached it: "
							+ mFragment.getTag());
			ft.attach(mFragment);
		} else if (preInitializedFragment != null) {
			Log.d("TabListener",
					"preInitializedFragment, so just attached it: "
							+ preInitializedFragment.getTag());
			ft.attach(preInitializedFragment);
			mFragment = preInitializedFragment;
		}

		// set the active tab in the MainActivity
		// solves the overlying of the tabs content on orientation change
		Log.d("TabListener",
				"mTag = " + mTag + " mActivity = "
						+ mActivity.getLocalClassName());
		if (mTag.equals("available")) {
			((WelcomeActivity) mActivity)
					.setActiveTab(WelcomeActivity.TAB_AVAILABLE);
			Log.d("TabListener", "activeTab set to 0");
		} else if (mTag.equals("running")) {
			((WelcomeActivity) mActivity)
					.setActiveTab(WelcomeActivity.TAB_RUNNING);
			Log.d("TabListener", "activeTab set to 1");
		} else if (mTag.equals("history")) {
			((WelcomeActivity) mActivity)
					.setActiveTab(WelcomeActivity.TAB_HISTORY);
			Log.d("TabListener", "activeTab set to 2");
		}
	}

	/**
	 * If unselected fragment not null, detach it.
	 */
	@Override
	public void onTabUnselected(Tab tab, FragmentTransaction ft) {
		Log.d("MosesTabListener", mFragment.getTag() + "Fragment detached");
		if (mFragment != null) {
			ft.detach(mFragment);
		}
	}

	@Override
	public void onTabReselected(Tab tab, FragmentTransaction ft) {
		// nothing to do here
	}
}