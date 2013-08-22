package de.da_sense.moses.client;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import de.da_sense.moses.client.abstraction.ApkMethods;
import de.da_sense.moses.client.abstraction.apks.ExternalApplication;
import de.da_sense.moses.client.abstraction.apks.InstalledExternalApplication;
import de.da_sense.moses.client.abstraction.apks.InstalledExternalApplicationsManager;
import de.da_sense.moses.client.service.MosesService;
import de.da_sense.moses.client.util.Log;
import de.da_sense.moses.client.util.Toaster;

/**
 * Represents the Fragment of an Available UserStudy.
 * Viewing running user studies in a list
 * 
 * @author Simon L, Sandra Amend, Wladimir Schmidt
 * @author Zijad Maksuti
 */
public class RunningFragment extends ListFragment {
	
	/**
	 * Request code that {@link DetailActivity} should use for passing back information about survey status:<br>
	 * {@link Activity#RESULT_OK} if it is successfully sent to server.
	 */
	static final int REQUEST_CODE_NOTIFY_ABOUT_SEND = 2;
	
	/** saves the current position in the list */
	int mCurRunPosition = 0;
	/** The current instance is saved in here. */
	private static RunningFragment thisInstance = null;
	/** a log tag for this class */
    private final static String LOG_TAG = RunningFragment.class.getName();
	/** The installed external applications. */
	private List<InstalledExternalApplication> installedApps;
	
	/**
	 * variable for limiting retries for requesting a check of validity of the
	 * installed apks database
	 */
	private int retriesCheckValidState = 0;
	
	/**
	 * The activity containing this fragment
	 */
	private Activity mActivity;
	
	/**
	 * Sometimes, we want to display info of a running user study and we only know the apkid assigned to it,
	 * but we do not know, which position the user study has in the list which is shown to the user.
	 * This {@link Map} tracks this information.
	 */
	private Map<String, Integer> mApkPositions;
    
	/** Returns the current instance (singleton) */
	public static RunningFragment getInstance() {
		return thisInstance;
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		
		initControls();
		
		if (savedInstanceState != null) {
			// restore last state
			mCurRunPosition = savedInstanceState.getInt("curChoice", 0);
		}
	}
	
	/**
	 * Helper method for showing the details of a userstudy.
	 * @param index the index of the userstudy to show the details for
	 * @param baseActivity the base activity
	 * @param startAppClickAction runnable for startApp button
	 * @param cancelClickAction
	 */
	protected void showDetails(int index, Activity baseActivity, 
			final Runnable startAppClickAction,
			final Runnable cancelClickAction) {
		if (MosesService.isOnlineOrIsConnecting(mActivity.getApplicationContext())) {
			if (getListView() != null) {
				// if we don't have any installed apps on the device an indexOutOfBoundsException gets thrown
				// this only happens on tablets (dual view)
				if (getInstalledApps() != null 
						&& getInstalledApps().size() > 0) {
					final InstalledExternalApplication app = getInstalledApps()
							.get(index);
						// otherwise launch new activity to display the fragment
						// with selected text
						Intent intent = new Intent();
						intent.setClass(mActivity, 
								DetailActivity.class);
						intent.putExtra("de.da_sense.moses.client.index", 
								index);
						intent.putExtra("de.da_sense.moses.client.belongsTo", 
								DetailFragment.RUNNING);
						intent.putExtra("de.da_sense.moses.client.appname", 
								app.getName());
						intent.putExtra("de.da_sense.moses.client.description", 
								app.getDescription());
						intent.putExtra(ExternalApplication.KEY_APK_ID, 
                                app.getID());
						intent.putExtra("de.da_sense.moses.client.apkVersion", 
                                app.getApkVersion());
                        intent.putExtra("de.da_sense.moses.client.startDate", 
                                app.getStartDateAsString());
						intent.putExtra("de.da_sense.moses.client.endDate", 
                                app.getEndDateAsString());
						mActivity.startActivityForResult(intent, REQUEST_CODE_NOTIFY_ABOUT_SEND);
				}			
			} else {
			}
		}
	}
	
	/**
	 * Helper method for showing the details of a userstudy.
	 * @param app the app (user study) for which to show the details for
	 * @param baseActivity the base activity
	 * @param startAppClickAction runnable for startApp button
	 * @param cancelClickAction
	 */
	public void showDetails(InstalledExternalApplication app, Activity baseActivity, 
			final Runnable startAppClickAction,
			final Runnable cancelClickAction) {
		if (MosesService.isOnlineOrIsConnecting(mActivity.getApplicationContext())) {
			if (getListView() != null) {
						// otherwise launch new activity to display the fragment
						// with selected text
						Intent intent = new Intent();
						intent.setClass(mActivity, DetailActivity.class);
						intent.putExtra("de.da_sense.moses.client.index", 0);
						intent.putExtra("de.da_sense.moses.client.belongsTo", 
								DetailFragment.RUNNING);
						intent.putExtra("de.da_sense.moses.client.appname", 
								app.getName());
						intent.putExtra("de.da_sense.moses.client.description", 
								app.getDescription());
                        intent.putExtra(ExternalApplication.KEY_APK_ID, 
                                app.getID());
                        intent.putExtra("de.da_sense.moses.client.apkVersion", 
                                app.getApkVersion());
                        intent.putExtra("de.da_sense.moses.client.startDate", 
                                app.getStartDateAsString());
                        intent.putExtra("de.da_sense.moses.client.endDate", 
                                app.getEndDateAsString());
						startActivity(intent);
				}		
			} else {
//				showNoConnectionInfoBox();
			}
	}

	/**
	 * save the current position in the list 
	 */
	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putInt("curChoice", mCurRunPosition);
	}

	@Override
	public void onPause() {
		super.onPause();
	}
	
	/**
	 * Get the installed apps list from the RunningFragment.
	 * @return the list of installedExternalApplications
	 */
	public List<InstalledExternalApplication> getInstalledApps() {
		if (installedApps == null) installedApps = InstalledExternalApplicationsManager.getInstance().getApps();
		return installedApps;
	}

	/**
	 * Comparator for the installed applications.
	 */
	private Comparator<? super InstalledExternalApplication> installedAppListComparator = new Comparator<InstalledExternalApplication>() {
		/**
		 * Comparator for InstalledExternalApplications.
		 * @param lhs
		 * @param rhs
		 * @return TODO
		 */
		@Override
		public int compare(InstalledExternalApplication lhs, InstalledExternalApplication rhs) {
			if (rhs == null && lhs == null) {
				return 0;
			}
			if (rhs != null && lhs == null) {
				return -1;
			}
			if (rhs == null && lhs != null) {
				return 1;
			}
			if ((rhs.isUpdateAvailable() && lhs.isUpdateAvailable())|| (!rhs.isUpdateAvailable() && !lhs.isUpdateAvailable())) {
				if (rhs.getName().equals(lhs.getName())) {
					return Integer.valueOf(rhs.hashCode()).compareTo(lhs.hashCode());
				}
				return rhs.getName().compareTo(lhs.getName());
			}
			if (lhs.isUpdateAvailable())
				return -1;
			return 1;
		}

	};

	/**
	 * Inits the controls.
	 */
	private void initControls() {
		Log.d(LOG_TAG, "initalizing ...");
		refreshInstalledApplications();
	}

	/**
	 * Refresh the list of installed Applications from the user studies.
	 */
	private void refreshInstalledApplications() {
		Log.d(LOG_TAG, "refreshing the installed Applications.");
		if (WelcomeActivity.checkInstalledStatesOfApks() == null) {
			if (retriesCheckValidState < 4) {
				Handler delayedRetryHandler = new Handler();
				delayedRetryHandler.postDelayed(new Runnable() {
					@Override
					public void run() {
						refreshInstalledApplications();
					}
				}, 1500);
				retriesCheckValidState++;
			} else {
				// TODO:show error when all retries didn't work?
				// TODO:BIG also do this for viewAvailableApplicationsList?
			}
		} else {
			retriesCheckValidState = 0;
		}
		if (InstalledExternalApplicationsManager.getInstance() == null)
			InstalledExternalApplicationsManager.init(mActivity);
		
		installedApps = sortForDisplay(
				new LinkedList<InstalledExternalApplication>
				(InstalledExternalApplicationsManager.getInstance().getApps()));
		
		populateList(installedApps);
	}

	/**
	 * Sort the list of applications to display them.
	 * @param linkedList the list to sort
	 * @return the sorted list
	 */
	private List<InstalledExternalApplication> sortForDisplay(
			Collection<InstalledExternalApplication> linkedList) {
		Log.d(LOG_TAG, "sorting the list of installed applications to display it.");
		if (linkedList == null)	{
			throw new RuntimeException("installed app list was null");
		}
		List<InstalledExternalApplication> sortedList = 
				new ArrayList<InstalledExternalApplication>(linkedList);
		Comparator<? super InstalledExternalApplication> comparator = 
				installedAppListComparator;
		Collections.sort(sortedList, comparator);
		return sortedList;
	}
	
	@Override
	public void onResume() {
		super.onResume();
		refreshInstalledApplications();
		Intent startingIntent = mActivity.getIntent();
		if(startingIntent.hasExtra(WelcomeActivity.KEY_VIEW_SURVEY)){
			// some other activity has started the parent activity in order to show an available survey, show it god damn it
			String apkID = startingIntent.getStringExtra(WelcomeActivity.KEY_VIEW_SURVEY);
			// obtain the view responsible for the apk
			final Integer positionOfTheView = mApkPositions.get(apkID);
			if(positionOfTheView == null || installedApps.size() <= positionOfTheView ){
				// the could already be in history tab, for example when the user fills the survey and then
				// tries to fill it again by following the status bar notification
				Log.i(LOG_TAG, "onResume() the apk is no longer in the list, the user may have already filled the survey");
				Toaster.showToast(mActivity, getString(R.string.notification_user_study_not_available));		
			}
			else{
				showDetails(positionOfTheView, mActivity, new Runnable() {
					@Override
					public void run() {
						handleStartApp(getInstalledApps().get(positionOfTheView));
					}
				}, new Runnable() {
					@Override
					public void run() {
					}
				});
			}
		}
	}

	/**
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		thisInstance = this;
		Log.d(LOG_TAG, "onCreate: parentActivity = " + 
				mActivity.getClass().getSimpleName());
	}
	
	/**
	 * Inflate the layout of the APK list.
	 */
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		Log.d(LOG_TAG, "onCreateView about to inflate View");
		 View runnningFragmentView = inflater.inflate(
				 R.layout.tab_running, container, false);
		 container.setBackgroundColor(getResources()
				 .getColor(android.R.color.background_light));
		 
		 return runnningFragmentView;
	}
	
	

	/* (non-Javadoc)
	 * @see android.support.v4.app.Fragment#onAttach(android.app.Activity)
	 */
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		mActivity = activity;
	}

	/**
	 * Expects the clicked view, which is then converted to the list position
	 * to handle the start of the application.
	 * @param v the clicked view item representing an app
	 */
	public void appStartClickHandler(View v) {
		int pos = getListView().getPositionForView(v);
		InstalledExternalApplication app = installedApps.get(pos);

		handleStartApp(app);
	}

	/**
	 * Handles the start of the application.
	 * @param app the already installed application to start
	 */
	protected void handleStartApp(InstalledExternalApplication app) {
		try {
			ApkMethods.startApplication(app.getPackageName(), WelcomeActivity.getInstance());
		} catch (NameNotFoundException e) {
			Log.e(LOG_TAG, "It was not possible to open the app");
		}
	}

	/**
	 * Shows the given applications in the list.
	 * @param applications
	 */
	private void populateList(List<InstalledExternalApplication> applications) {
		// get the app names
		String[] items = new String[applications.size()];
		int counter = 0;
		for (InstalledExternalApplication app : applications) {
			items[counter] = app.getName();
			counter++;
		}
		
		TextView instructionsView = (TextView) mActivity
				.findViewById(R.id.installedAppHeaderInstructions);
		if (instructionsView != null) {
			if (applications.size() == 0) {
				instructionsView.setText(R.string.installedApkList_emptyHint);
			} else {
				instructionsView.setText(R.string.installedApkList_defaultHint);
			}
		}

		List<Map<String, String>> listContent = new LinkedList<Map<String, String>>();
		mApkPositions = new HashMap<String, Integer>();
		int positionCounter = 0; // remember the positions of the shown APKs in the list
		for (InstalledExternalApplication app : applications) {
			HashMap<String, String> rowMap = new HashMap<String, String>();
			rowMap.put("name", app.getName());
			rowMap.put("updateIndicator", app.isUpdateAvailable() ? "update available" : "");
			listContent.add(rowMap);
			mApkPositions.put(app.getID(), positionCounter);
			positionCounter++;
		}
		
		MosesListAdapter contentAdapter = new MosesListAdapter(mActivity, 
				listContent, 
				R.layout.installedapplistitem,
				new String[] { "name", "updateIndicator" }, 
				new int[] { R.id.installedAppListItemText, R.id.updateIndicator });
		
		setListAdapter(contentAdapter);
	}

	/**
	 * Concatenate the stack trace of an exception to one String.
	 * @param e the exception to concatenate
	 * @return the concatenated String of the exception
	 */
	public static String concatStacktrace(Exception e) {
		String stackTrace = "";
		for (int i = 0; i < e.getStackTrace().length; i++) {
			stackTrace += e.getStackTrace()[i];
		}
		return stackTrace;
	}
}

