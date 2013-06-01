package de.da_sense.moses.client;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;

import org.json.JSONArray;
import org.json.JSONException;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.FragmentTransaction;
import android.app.ListFragment;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentActivity;
import android.util.SparseIntArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import de.da_sense.moses.client.abstraction.ApkListRequestObserver;
import de.da_sense.moses.client.abstraction.ApkMethods;
import de.da_sense.moses.client.abstraction.apks.APKInstalled;
import de.da_sense.moses.client.abstraction.apks.ApkDownloadManager;
import de.da_sense.moses.client.abstraction.apks.ApkInstallManager;
import de.da_sense.moses.client.abstraction.apks.ExternalApplication;
import de.da_sense.moses.client.abstraction.apks.HistoryExternalApplication;
import de.da_sense.moses.client.abstraction.apks.HistoryExternalApplicationsManager;
import de.da_sense.moses.client.abstraction.apks.InstalledExternalApplication;
import de.da_sense.moses.client.abstraction.apks.InstalledExternalApplicationsManager;
import de.da_sense.moses.client.preferences.MosesPreferences;
import de.da_sense.moses.client.service.MosesService;
import de.da_sense.moses.client.service.helpers.ExecutableForObject;
import de.da_sense.moses.client.util.Log;

/**
 * Responsible for displaying the available APKs which get fetched from the
 * server.
 * @author Sandra Amend, Simon L
 */
public class AvailableFragment extends ListFragment implements ApkListRequestObserver {
	/**
	 * Enums for the state of the layout.
	 */
	public static enum LayoutState {
		NORMAL_LIST, SENSORS_HINT, EMPTYLIST_HINT, PENDING_REQUEST, NO_CONNECTIVITY;
	}
	
	/** boolean for the combined list and detail mode */
	public boolean mDualPane;
	/** saves the current position in the list */
	private int mCurAvaiPosition = 0;
	/** The current instance is saved in here. */
	private static AvailableFragment thisInstance = null;
	/** Identifier for setting the sensor hint boolean in the preference manager. */
	private static final String PREFKEY_SHOW_SET_SENSORS_HINT = "showInitialSetSensorsHint";
	/** Threshold for the refresh time. */
	private static final int REFRESH_THRESHHOLD = 800;
	/** Listing of the applications */
	private List<ExternalApplication> externalApps;
	/** Variable to save when the last refresh of the list was. */
	private Long lastListRefreshTime = null;
	/** Save the last layout which was set. */
	LayoutState lastSetLayout = null;
	/** TODO: I have no idea what exactly this is for ... */
	private int totalSize = -1;
	/** variable for requestExternalApplications  */
	protected int requestListRetries = 0;
	/** variable to check if the app is paused */
	private boolean isPaused;
	/** a log tag for this class */
    private final static String TAG = "AvailableFragment";
    /** mapping from filtered (displayed) to non-filtered (externalApps) list */
	private SparseIntArray listIndex = new SparseIntArray();
	
	/** Returns the current instance (singleton) */
	public static AvailableFragment getInstance() {
		return thisInstance;
	}

	/**
	 * @return the externalApps
	 */
	public List<ExternalApplication> getExternalApps() {
		return externalApps;
	}

	/**
	 * @param externalApps the externalApps to set
	 */
	public void setExternalApps(List<ExternalApplication> externalApps) {
		this.externalApps = externalApps;
	}

	/**
	 * 
	 * @see android.support.v4.app.Fragment#onActivityCreated(android.os.Bundle)
	 */
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		
		initControls();
		
		// check for frame in which to embed the details and set the boolean
		View detailsFrame = getActivity().findViewById(R.id.details);
		mDualPane = (detailsFrame != null) 
				&& (detailsFrame.getVisibility() == View.VISIBLE);
		
		if (savedInstanceState != null) {
			// restore last state
			mCurAvaiPosition = savedInstanceState.getInt("curChoice", 0);
		}
		
		if (mDualPane) {
			// in dual pane mode the list view highlights the selected item
			getListView().setChoiceMode(ListView.CHOICE_MODE_SINGLE);
			// show details frame
			showDetails(mCurAvaiPosition, getActivity(), new Runnable() {
					@Override
					public void run() {
					}
				}, new Runnable() {
					@Override
					public void run() {
					}
				});
		}
	}

	/**
	 * Helper method for showing the details of a userstudy.
	 * @param index the index of the userstudy to show the details for
	 */
	protected void showDetails(int index, Activity baseActivity, 
			final Runnable installAppClickAction,
			final Runnable cancelClickAction) {
		if (MosesService.isOnline(getActivity().getApplicationContext())) {
			if (getListView() != null) {
				if (getExternalApps() != null) {
					final ExternalApplication app = getExternalApps().get(index);
					if (mDualPane) {
						getListAdapter().getItem(index);
						
						// dual mode: we can display everything on the screen
						// update list to highlight the selected item and show data
						getListView().setItemChecked(index, true);

						// check what fragment is currently shown, replace if needed
						DetailFragment details = (DetailFragment) getActivity().getFragmentManager().findFragmentById(R.id.details);

						if (details == null 
								|| details.getShownIndex() != index) {
							details = DetailFragment.newInstance(index, 
									DetailFragment.AVAILABLE, 
									app.getName(),
									app.getDescription(), 
									(ArrayList<Integer>) app.getSensors(),
									app.getID(), 
									app.getApkVersion(), 
									app.getStartDateAsString(), 
									app.getEndDateAsString());
//							details.setRetainInstance(true); 

							FragmentTransaction ft = getActivity().getFragmentManager().beginTransaction();
							ft.replace(R.id.details, details);
							ft.setTransition(FragmentTransaction
									.TRANSIT_FRAGMENT_FADE);
							ft.commit();
						}
					} else {
						// otherwise launch new activity to display the fragment
						// with selected text
						Intent intent = new Intent();
						intent.setClass(getActivity(), 
								DetailActivity.class);
						intent.putExtra("de.da_sense.moses.client.index", 
								index);
						intent.putExtra("de.da_sense.moses.client.belongsTo", 
								DetailFragment.AVAILABLE);
						intent.putExtra("de.da_sense.moses.client.appname", 
								app.getName());
						intent.putExtra("de.da_sense.moses.client.description", 
								app.getDescription());
						intent.putExtra("de.da_sense.moses.client.sensors", 
								app.getSensors());
						intent.putExtra("de.da_sense.moses.client.apkid", 
								app.getID());
						intent.putExtra("de.da_sense.moses.client.apkVersion", 
                                app.getApkVersion());
						intent.putExtra("de.da_sense.moses.client.startDate", 
                                app.getStartDateAsString());
						intent.putExtra("de.da_sense.moses.client.endDate", 
                                app.getEndDateAsString());
						startActivity(intent);
					}
				} else { // no ExternalApplication: show Placeholder
					// check what fragment is currently shown, replace if needed
					DetailFragment details = (DetailFragment)
							getActivity().getFragmentManager()
							.findFragmentById(R.id.details);

					if (details == null || details.getShownIndex() != index) {
						details = DetailFragment.newInstance();
//						details.setRetainInstance(true);

						FragmentTransaction ft = getActivity()
								.getFragmentManager()
								.beginTransaction();
						ft.replace(R.id.details, details);
						ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
						ft.commit();
					}
				}			
			} else {
				showNoConnectionInfoBox();
			}
		}
	}
	
	/**
	 * Helper method for showing the details of a userstudy.
	 * @param index the index of the userstudy to show the details for
	 */
	public void showDetails(ExternalApplication app, Activity baseActivity, 
			final Runnable installAppClickAction,
			final Runnable cancelClickAction) {
		if (MosesService.isOnline(getActivity().getApplicationContext())) {
			if (getListView() != null) {
					if (mDualPane) {						
						// check what fragment is currently shown, replace if needed
						DetailFragment details = (DetailFragment)
								getActivity()
								.getFragmentManager()
								.findFragmentById(R.id.details);

						if (details == null) {
							details = DetailFragment.newInstance( 
									DetailFragment.AVAILABLE, 
									app.getName(), 
									app.getDescription(), 
									(ArrayList<Integer>) app.getSensors(),
									app.getID(), 
									app.getApkVersion(), 
									app.getStartDateAsString(), 
									app.getEndDateAsString());
//							details.setRetainInstance(true);

							FragmentTransaction ft = getActivity()
									.getFragmentManager()
									.beginTransaction();
							ft.replace(R.id.details, details);
							ft.setTransition(FragmentTransaction
									.TRANSIT_FRAGMENT_FADE);
							ft.commit();
						}
					} else {
						// otherwise launch new activity to display the fragment
						// with selected text
						Intent intent = new Intent();
						intent.setClass(getActivity(), 
								DetailActivity.class);
						intent.putExtra("de.da_sense.moses.client.index", 0);
						intent.putExtra("de.da_sense.moses.client.belongsTo",
								DetailFragment.AVAILABLE);
						intent.putExtra("de.da_sense.moses.client.appname",
								app.getName());
						intent.putExtra("de.da_sense.moses.client.description",
								app.getDescription());
						intent.putExtra("de.da_sense.moses.client.sensors",
								app.getSensors());
                        intent.putExtra("de.da_sense.moses.client.apkid", 
                                app.getID());
                        intent.putExtra("de.da_sense.moses.client.apkVersion", 
                                app.getApkVersion());
                        intent.putExtra("de.da_sense.moses.client.startDate", 
                                app.getStartDateAsString());
                        intent.putExtra("de.da_sense.moses.client.endDate", 
                                app.getEndDateAsString());
						startActivity(intent);
					}
				} else { // no ExternalApplication: show Placeholder
					// check what fragment is currently shown, replace if needed
					DetailFragment details = (DetailFragment)
							getActivity().getFragmentManager()
							.findFragmentById(R.id.details);

					if (details == null) {
						details = DetailFragment.newInstance();
//						details.setRetainInstance(true);

						FragmentTransaction ft = getActivity()
								.getFragmentManager()
								.beginTransaction();
						ft.replace(R.id.details, details);
						ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
						ft.commit();
					}
				}			
			} else {
				showNoConnectionInfoBox();
			}
	}
	
	/**
	 * Save the current position in the list
	 * @see android.support.v4.app.Fragment#onSaveInstanceState(android.os.Bundle)
	 */
	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putInt("curChoice", mCurAvaiPosition);
		Log.d("AvailableFragment", "onSaveInstanceState called");
	}

	/**
	 * 
	 * @see android.support.v4.app.Fragment#onPause()
	 */
	@Override
	public void onPause() {
		super.onPause();
		this.isPaused = true;
	}

	/**
	 * 
	 * @see android.support.v4.app.Fragment#onCreate(android.os.Bundle)
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		thisInstance = this;
		Log.d("AvailableFragment", "onCreate: parentActivity = " + 
				getActivity().getClass().getSimpleName());
	}
	
	/**
	 * Inflate the layout of the APK list.
	 * @see android.support.v4.app.ListFragment#onCreateView(android.view.LayoutInflater, android.view.ViewGroup, android.os.Bundle)
	 */
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		Log.d("AvailableFragment", "onCreateView about to inflate View");
		 View availbleFragmentView = inflater.inflate(R.layout.availableapklist,
				 container, false);
		 container.setBackgroundColor(getResources()
				 .getColor(android.R.color.background_light));
		 
		 return availbleFragmentView;
	}

	/**
	 * Method to save the last layout which was set.
	 * @param lastSetLayout the last layout
	 */
	public void setLastSetLayout(LayoutState lastSetLayout) {
		this.lastSetLayout = lastSetLayout;
		Log.d("MoSeS.UI", "Layouted showAvailableApkList to state "
				+ lastSetLayout);
	}


	/**
	 * Show a dialog to inform that there is no internet connection.
	 */
	private void showNoConnectionInfoBox() {
		new AlertDialog.Builder(getActivity())
				.setMessage(getString(R.string.availableTab_noInternetConnection))
				.setTitle(getString(R.string.noInternetConnection_title)).setCancelable(true)
				// TODO: this was already commented out ...
				// .setNeutralButton("OK",
				// new DialogInterface.OnClickListener() {
				// public void onClick(DialogInterface dialog, int
				// whichButton){}
				// })
				.show();
	}

	/**
	 * Initialize the controls in the gui.
	 */
	private void initControls() {
		initControlsOnRequestApks();
		requestExternalApplications();
	}

	/**
	 * Checks if the app list is still in the cache available.
	 * @return true if the apps are still available
	 */
	private boolean appsLocallyInCacheStillAvailable() {
		return getExternalApps() != null && getExternalApps().size() > 0;
	}

	/**
	 * Initialize the controls to request the APKs.
	 * This Method calls a different initControls... depending on the state of
	 * the application.
	 */
	private void initControlsOnRequestApks() {
		if (showInitialSensorHint()) {
			// if the Sensor Hint boolean is set call this first
			initControlsShowSensorsHint();
		} else {
			if (appsLocallyInCacheStillAvailable()) {
				// if the App list is still cached just display it
				initControlsNormalList(getExternalApps());
			} else {
				if (MosesService.isOnline(WelcomeActivity.getInstance())) {
					initControlsPendingListRequest();
				} else {
					initControlsNoConnectivity();
				}
			}
		}
	}

	/**
	 * Controls what to show and what to do when there is no connection.
	 */
	private void initControlsNoConnectivity() {
		if (lastSetLayout != LayoutState.NO_CONNECTIVITY) {
			// no connection so show an empty list
			LinearLayout emptylistCtrls = (LinearLayout) getActivity()
					.findViewById(R.id.apklist_emptylistLayout);
			emptylistCtrls.setVisibility(View.VISIBLE);
			
			LinearLayout apkListCtrls = (LinearLayout) getActivity()
					.findViewById(R.id.apklist_mainListLayout);
			apkListCtrls.setVisibility(View.GONE);

			// set hint that there is no connection
			TextView mainHint = (TextView) getActivity()
					.findViewById(R.id.apklist_emptylistHintMain);
			mainHint.setText(R.string.apklist_hint_noconnectivity);

			// display button to refresh and set action to perform
			final Button actionBtn1 = (Button) getActivity()
					.findViewById(R.id.apklist_emptylistActionBtn1);
			final Button actionBtn2 = (Button) getActivity()
					.findViewById(R.id.apklist_emptylistActionBtn2);
			actionBtn1.setText("Refresh");
			actionBtn2.setVisibility(View.GONE);

			refreshResfreshBtnTimeout(actionBtn1, getString(R.string.retry),
					LayoutState.NO_CONNECTIVITY);

			actionBtn1.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					refreshResfreshBtnTimeout(actionBtn1, getString(R.string.retry),
							LayoutState.NO_CONNECTIVITY);
					requestExternalApplications();
				}
			});
			
			// set the last layout
			setLastSetLayout(LayoutState.NO_CONNECTIVITY);
		}
	}

	/**
	 * Controls what to show and what to do during a pending request.
	 */
	private void initControlsPendingListRequest() {
		if (lastSetLayout != LayoutState.PENDING_REQUEST) {
			// during a pending request show an empty list
			LinearLayout emptylistCtrls = (LinearLayout) getActivity()
					.findViewById(R.id.apklist_emptylistLayout);
			emptylistCtrls.setVisibility(View.VISIBLE);
			LinearLayout apkListCtrls = (LinearLayout) getActivity()
					.findViewById(R.id.apklist_mainListLayout);
			apkListCtrls.setVisibility(View.GONE);

			// display hint that there is a pending request
			TextView mainHint = (TextView) getActivity()
					.findViewById(R.id.apklist_emptylistHintMain);
			mainHint.setText(R.string.apklist_hint_pendingrequest);

			// show a refresh button an add an action
			final Button actionBtn1 = (Button) getActivity()
					.findViewById(R.id.apklist_emptylistActionBtn1);
			final Button actionBtn2 = (Button) getActivity()
					.findViewById(R.id.apklist_emptylistActionBtn2);
			actionBtn1.setText("Refresh");
			actionBtn2.setVisibility(View.GONE);

			refreshResfreshBtnTimeout(actionBtn1, getString(R.string.refresh),
					LayoutState.PENDING_REQUEST);

			actionBtn1.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					refreshResfreshBtnTimeout(actionBtn1, getString(R.string.refresh),
							LayoutState.PENDING_REQUEST);
					requestExternalApplications();
				}
			});
			
			// set the last layout
			setLastSetLayout(LayoutState.PENDING_REQUEST);
		}
	}

	/**
	 * Controls what the button does during a refresh.
	 */
	private void refreshResfreshBtnTimeout(final Button refreshButton,
			final String minimalString, final LayoutState parentLayout) {
		// disable the button during a refresh
		refreshButton.setEnabled(false);
		refreshButton.setText(minimalString);
		// changes the text on the button over time
		Handler enableRefreshHandler = new Handler();
		enableRefreshHandler.postDelayed(new Runnable() {
			@Override
			public void run() {
				if (!isPaused && lastSetLayout == parentLayout) {
					refreshButton.setText(minimalString + ".");
				}
			}
		}, 800);
		enableRefreshHandler.postDelayed(new Runnable() {
			@Override
			public void run() {
				if (!isPaused && lastSetLayout == parentLayout) {
					refreshButton.setText(minimalString + "..");
				}
			}
		}, 1600);
		enableRefreshHandler.postDelayed(new Runnable() {
			@Override
			public void run() {
				if (!isPaused && lastSetLayout == parentLayout) {
					refreshButton.setText(minimalString + "...");
				}
			}
		}, 2400);
		// and then enables the button
		enableRefreshHandler.postDelayed(new Runnable() {
			@Override
			public void run() {
				if (!isPaused && lastSetLayout == parentLayout) {
					refreshButton.setEnabled(true);
				}
			}
		}, 3000);
	}

	/**
	 * TODO: Javadoc
	 */
	private void initLayoutFromArrivedList(
			List<ExternalApplication> applications) {
		if (showInitialSensorHint()) {
			// even if there is an arrived list, do not show it, but show the
			// sensors hint
			initControlsShowSensorsHint();
		} else {
			if (applications.size() > 0) {
				initControlsNormalList(applications);
			} else {
				initControlsEmptyArrivedList(false);
			}
		}
		populateList(applications);  // TODO : Why should we execute this method when we do this in initControlsNormalList ?
	}

	/**
	 * Controls what to do and what to show in case we get an empty APK list.
	 * @param mayShowSensorsList true to show the sensor hint
	 */
	private void initControlsEmptyArrivedList(boolean mayShowSensorsList) {
		if (mayShowSensorsList) {
			initControlsShowSensorsHint();
		} else {
			if (lastSetLayout != LayoutState.EMPTYLIST_HINT) {
				// show an empty list, because the list we got was empty
				LinearLayout emptylistCtrls = (LinearLayout) getActivity()
						.findViewById(R.id.apklist_emptylistLayout);
				emptylistCtrls.setVisibility(View.VISIBLE);
				LinearLayout apkListCtrls = (LinearLayout) getActivity()
						.findViewById(R.id.apklist_mainListLayout);
				apkListCtrls.setVisibility(View.GONE);

				// show a hint, that there are no apks
				TextView mainHint = (TextView) getActivity()
						.findViewById(R.id.apklist_emptylistHintMain);
				mainHint.setText(R.string.availableApkList_emptyHint);

				// we don't need any buttons here
				Button actionBtn1 = (Button) getActivity()
						.findViewById(R.id.apklist_emptylistActionBtn1);
				Button actionBtn2 = (Button) getActivity()
						.findViewById(R.id.apklist_emptylistActionBtn2);
				actionBtn1.setVisibility(View.GONE);
				actionBtn2.setVisibility(View.GONE);
				
				// set last layout
				setLastSetLayout(LayoutState.EMPTYLIST_HINT);
			}
		}
	}

	/**
	 * Controls what to do and what to show in case we get a non empty list of
	 * applications.
	 * @param applications list of the applications to show
	 */
	private void initControlsNormalList(List<ExternalApplication> applications) {
		// show a "normal" non-empty list
		LinearLayout emptylistCtrls = (LinearLayout) getActivity()
				.findViewById(R.id.apklist_emptylistLayout);
		// TODO: fast switching between tabs causes NullPointerExc, where 
		//       emptylistCtrls and apkListCtrls will be here NULL
		if(emptylistCtrls != null)
			emptylistCtrls.setVisibility(View.GONE);
		
		LinearLayout apkListCtrls = (LinearLayout) getActivity()
				.findViewById(R.id.apklist_mainListLayout);
		
		if(apkListCtrls != null)
			apkListCtrls.setVisibility(View.VISIBLE);

		// set last layout
		setLastSetLayout(LayoutState.NORMAL_LIST);
		// and show the applications in the list
		populateList(applications);
	}

	/**
	 * Controls the displaying of the sensor hint.
	 */
	private void initControlsShowSensorsHint() {
		Log.d("initControlsShowSensorHint", "lastSetLayout = " + lastSetLayout);
		Log.d("initControlsShowSensorHint", "View " + WelcomeActivity.getInstance()
				.findViewById(R.id.apklist_emptylistLayout));
		
		if (lastSetLayout != LayoutState.SENSORS_HINT) {
			// show an empty list
			LinearLayout emptylistCtrls = (LinearLayout) WelcomeActivity.getInstance()
					.findViewById(R.id.apklist_emptylistLayout);
			emptylistCtrls.setVisibility(View.VISIBLE);
			LinearLayout apkListCtrls = (LinearLayout) WelcomeActivity.getInstance()
					.findViewById(R.id.apklist_mainListLayout);
			apkListCtrls.setVisibility(View.GONE);

			// show hint that there are no sensors enabled in the settings
			TextView mainHint = (TextView) WelcomeActivity.getInstance()
					.findViewById(R.id.apklist_emptylistHintMain);
			mainHint.setText(R.string.apklist_hint_sensors_main);

			// give user the option to set the sensors
			Button actionBtn1 = (Button) WelcomeActivity.getInstance()
					.findViewById(R.id.apklist_emptylistActionBtn1);
			Button actionBtn2 = (Button) WelcomeActivity.getInstance()
					.findViewById(R.id.apklist_emptylistActionBtn2);
			
			actionBtn1.setText(getString(R.string.availableTab_btnOk));
			actionBtn2.setText(getString(R.string.availableTab_btnCancel));
			actionBtn1.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					// disable sensor hint boolean in settings
					// and edit sensor settings
					PreferenceManager
							.getDefaultSharedPreferences(
									WelcomeActivity.getInstance()).edit()
							.putBoolean(PREFKEY_SHOW_SET_SENSORS_HINT, false)
							.commit();
					invokeSensorDialog();
				}
			});
			actionBtn2.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					// disable sensor hint boolean in settings
					// and call initControls again
					PreferenceManager
							.getDefaultSharedPreferences(
									WelcomeActivity.getInstance()).edit()
							.putBoolean(PREFKEY_SHOW_SET_SENSORS_HINT, false)
							.commit();
					initControls();
				}
			});
			
			// set last layout
			setLastSetLayout(LayoutState.SENSORS_HINT);
		}
	}

	/**
	 * Start the settings dialog for the sensors.
	 */
	protected void invokeSensorDialog() {
		Intent startPreference = new Intent(WelcomeActivity.getInstance(),
				MosesPreferences.class);
		startPreference.putExtra("startSensors", true);
		startActivity(startPreference);
	}

	/**
	 * Return if the sensor hint should be shown to the user.
	 * @return true if sensor hint should be presented
	 */
	private boolean showInitialSensorHint() {
		boolean enoughEnabledSensors = false;
		FragmentActivity activity = (FragmentActivity) this.getActivity();
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(WelcomeActivity.getInstance());
		try {			
			Log.d("AvailableFragment", "sISH() - activity = " + activity);
			Log.d("AvailableFragment", "showInitalSensorHint() - sensor_data = "
			+ prefs.getString("sensor_data", "[]"));
			
			JSONArray sensors = new JSONArray(prefs.getString("sensor_data","[]"));
			Log.d("AvailableFragment", "showInitalSensorHint() - sensors = "
					+ sensors);
			enoughEnabledSensors = !(sensors != null && sensors.length() < 1);
		} catch (JSONException e) {
			enoughEnabledSensors = false;
		}
		
		boolean doShow = prefs.getBoolean(PREFKEY_SHOW_SET_SENSORS_HINT, true);
		
		if (enoughEnabledSensors) {
			doShow = false;
			prefs.edit().putBoolean(PREFKEY_SHOW_SET_SENSORS_HINT, true).commit();
		}

		return doShow;
	}

	/**
	 * FIXME: The ProgressDialog doesn't show up.
	 * Handles installing APK from the Server.
	 * @param app the App to download and install
	 */
	protected void handleInstallApp(ExternalApplication app) {
	    
		final ProgressDialog progressDialog = new ProgressDialog(WelcomeActivity.getInstance()); // this.getActivity());
		
		Log.d(TAG, "progressDialog = " + progressDialog);
		
		final ApkDownloadManager downloader = new ApkDownloadManager(
		        app,
		        WelcomeActivity.getInstance().getApplicationContext(),// getActivity().getApplicationContext(),
				new ExecutableForObject() {
					@Override
					public void execute(final Object o) {
						if (o instanceof Integer)
						{
						    WelcomeActivity.getInstance().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    if (totalSize == -1) {
                                        totalSize = (Integer) o / 1024;
                                        progressDialog.setMax(totalSize);
                                    } else {
                                        progressDialog
                                                .incrementProgressBy(((Integer) o / 1024)
                                                        - progressDialog
                                                                .getProgress());
                                    }
                                }
                            });
							/* They were :
							 * Runnable runnable = new Runnable() {
								Integer temporary = (Integer) o / 1024;
								@Override
								public void run() {
									if (totalSize == -1) {
										totalSize = temporary;
										progressDialog.setMax(totalSize);
									} else {
										progressDialog
										.incrementProgressBy(
												temporary - progressDialog.getProgress());
									}
								}
							};
							getActivity().runOnUiThread(runnable);*/
						}
					}
				});
		
		progressDialog.setTitle(getString(R.string.downloadingApp));
		progressDialog.setMessage(getString(R.string.pleaseWait));
		progressDialog.setMax(0);
		progressDialog.setProgress(0);
		progressDialog.setOnCancelListener(new OnCancelListener() {
			@Override
			public void onCancel(DialogInterface dialog) {
				downloader.cancel();
			}
		});
		
		progressDialog.setCancelable(true);
		progressDialog.setButton(DialogInterface.BUTTON_POSITIVE, 
				"Cancel",
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						if (progressDialog.isShowing())
							progressDialog.cancel();
					}
				});
		progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
		
		Observer observer = new Observer() {
			@Override
			public void update(Observable observable, Object data) {
				if (downloader.getState() 
						== ApkDownloadManager.State.ERROR) {
					// error downloading
					if (progressDialog.isShowing()) {
						progressDialog.dismiss();
					}
					showMessageBoxErrorDownloading(downloader);
				} else if (downloader.getState() 
						== ApkDownloadManager.State.ERROR_NO_CONNECTION) {
					// error with connection
					if (progressDialog.isShowing()) {
						progressDialog.dismiss();
					}
					showMessageBoxErrorNoConnection(downloader);
				} else if (downloader.getState() 
						== ApkDownloadManager.State.FINISHED) {
					// success
					if (progressDialog.isShowing()) {
						progressDialog.dismiss();
					}
					installDownloadedApk(downloader.getDownloadedApk(),
							downloader.getExternalApplicationResult());
				}
			}
		};
		downloader.addObserver(observer);
		totalSize = -1;
//		progressDialog.show(); FIXME: commented out in case it throws an error
		downloader.start();
	}

	/**
	 * Shows an AlertDialog which informs the user about a missing internet
	 * connection encountered while downloading an app.
	 * @param downloader the download manager which encountered the error
	 */
	protected void showMessageBoxErrorNoConnection(ApkDownloadManager downloader) {
		new AlertDialog.Builder(WelcomeActivity.getInstance())
				.setMessage(getString(R.string.noInternetConnection_message))
				.setTitle(getString(R.string.noInternetConnection_title))
				.setCancelable(true)
				.setNeutralButton(getString(R.string.ok), null).show();
	}

	/**
	 * Shows an AlertDialog which informs the user about an error which occured
	 * while downloading an app.
	 * @param downloader the download manager which encountered the error
	 */
	protected void showMessageBoxErrorDownloading(ApkDownloadManager downloader) {
		new AlertDialog.Builder(WelcomeActivity.getInstance())
				.setMessage(getString(R.string.downloadApk_errorMessage, downloader.getErrorMsg()))
				.setTitle(getString(R.string.error))
				.setCancelable(true)
				.setNeutralButton(getString(R.string.ok), null).show();
	}

	/**
	 * Install an APK file on the device.
	 * @param originalApk the APK file
	 * @param externalAppRef the reference to the app on the MoSeS server
	 */
	private void installDownloadedApk(final File originalApk,
			final ExternalApplication externalAppRef) {
		final ApkInstallManager installer = 
				new ApkInstallManager(originalApk, externalAppRef, getActivity().getApplicationContext());
		installer.addObserver(new Observer() {
			@Override
			public void update(Observable observable, Object data) {
				if (installer.getState() 
						== ApkInstallManager.State.ERROR) {
					// nothing?
				} else if (installer.getState() 
						== ApkInstallManager.State.INSTALLATION_CANCELLED) {
					// TODO: how to handle if the user cancels the installation?
				} else if (installer.getState() 
						== ApkInstallManager.State.INSTALLATION_COMPLETED) {
					new APKInstalled(externalAppRef.getID());
					try {
						ApkInstallManager.registerInstalledApk(originalApk,
								externalAppRef,
								WelcomeActivity.getInstance() // It was : AvailableFragment.this.getActivity())
								.getApplicationContext(), false);
					} catch (IOException e) {
						Log.e("MoSeS.Install",
								"Problems with extracting package name from " +
								"apk, or problems with the " +
								"InstalledExternalApplicationsManager after " +
								"installing an app");
					}
				}
			}
		});
		installer.start();
	}

	/**
	 * Request the list of available apps from the server and initialize 
	 * the request APK controls.
	 */
	private void requestExternalApplications() {
		if (MosesService.getInstance() == null) {
			if (requestListRetries < 5) {
				Handler delayedRetryHandler = new Handler();
				delayedRetryHandler.postDelayed(new Runnable() {
					@Override
					public void run() {
						requestExternalApplications();
					}
				}, 1000);
				requestListRetries++;
			} else {
				// TODO: show error when all retries didn't work?
			}
		} else {
			requestListRetries = 0;
			lastListRefreshTime = System.currentTimeMillis();
			ApkMethods.getExternalApplications(AvailableFragment.this);
			initControlsOnRequestApks();
		}
	}

	/**
	 * This Method gets called after the Fragment received a message, that the
	 * list request is finished. It then sets the eternal applications and 
	 * initializes the necessary controls.
	 * @see de.da_sense.moses.client.abstraction.ApkListRequestObserver#apkListRequestFinished(java.util.List)
	 */
	@Override
	public void apkListRequestFinished(List<ExternalApplication> applications) {
		setExternalApps(applications);
		initLayoutFromArrivedList(applications);
	}	
	

	/**
	 * This method gets called after the Fragment received a message, that the
	 * list request failed. So far it only creates a log entry.
	 * TODO: receive failures that point out no connection, too;
	 * show user some hint about this
	 * @see de.da_sense.moses.client.abstraction.ApkListRequestObserver#apkListRequestFailed(java.lang.Exception)
	 */
	@Override
	public void apkListRequestFailed(Exception e) {
		Log.w("MoSeS.APKMETHODS",
				"invalid response for apk list request: " + e.getMessage());
	}

	/**
	 * Gets called from MainActivity, because Fragments don't override 
	 * onWindowFocusChanged. 
	 * TODO: Is there a better way? Reason this got added: to reload the list after a focus change.
	 */
	public void onWindowFocusChangedFragment(boolean hasFocus) {
		if (hasFocus && (lastListRefreshTime == null) ? true
				: (System.currentTimeMillis() - lastListRefreshTime > REFRESH_THRESHHOLD)) {
			requestExternalApplications();
		}
	}

	/**
	 * After the app is resumed we request the external applications,
	 * and set up a second request. 
	 * @see android.support.v4.app.Fragment#onResume()
	 */
	@Override
	public void onResume() {
		super.onResume();
		this.isPaused = false;
		if (lastSetLayout == LayoutState.SENSORS_HINT) {
			initControls();
		} else {
			boolean checkRefreshTime = (lastListRefreshTime == null)
					? true 
					: (System.currentTimeMillis() - lastListRefreshTime 
							> REFRESH_THRESHHOLD);
			if (checkRefreshTime) {
				requestExternalApplications();
			}
		}

		Handler secondTryConnect = new Handler();
		secondTryConnect.postDelayed(new Runnable() {
			@Override
			public void run() {
				if (!isPaused) {
					requestExternalApplications();
				}
			}
		}, 2500);
	}

	/**
	 * Return the mapping of the element from the non-filtered list to the 
	 * filtered list. To access the right ExternalApplication when clicking on 
	 * an element in the displayed (filtered) available list.
	 * @param id the element position in the filtered list
	 * @return the position in the non-filtered list
	 */
	public Integer getListIndexElement(Integer id) {
		return listIndex.get(id);
	}

	/**
	 * Populate the application list with the app names and their descriptions.
	 * @param applications
	 */
	private void populateList(List<ExternalApplication> applications) {
		// we don't want to display already installed apps / participated user studies	
		if (InstalledExternalApplicationsManager.getInstance() == null)
			InstalledExternalApplicationsManager.init(getActivity());
		LinkedList<InstalledExternalApplication> installedApps = 
				InstalledExternalApplicationsManager.getInstance().getApps();
		// and history apps
		if (HistoryExternalApplicationsManager.getInstance() == null)
			HistoryExternalApplicationsManager.init(getActivity());
		LinkedList<HistoryExternalApplication> historyApps = 
				HistoryExternalApplicationsManager.getInstance().getApps();
		
		HashSet<String> hashAppIDs = new HashSet<String>();
		// collect all IDs from installed apps
		for (InstalledExternalApplication installedApp : installedApps) {
			hashAppIDs.add(installedApp.getID());
		}
		// we want to get the real number of apps to show
		// special care has to be taken for history apps which might not anymore
		// be in the externalApps list received from server
		// thats why we check which external apps are in the history and not how many apps are in history
		HashSet<ExternalApplication> realHistApps = new HashSet<ExternalApplication>();
		for (ExternalApplication app : applications) {
			if (HistoryExternalApplicationsManager.getInstance().containsApp(app)) {
				realHistApps.add(app);
			}
		}
		// we set everything up, so that the installed apps 
		// and the history apps are (should be) disjoint 
		int numberOfApps = applications.size() - hashAppIDs.size() - realHistApps.size();
		
		Log.d(TAG, "installed: " + hashAppIDs.size() + " history: " + realHistApps.size() 
				+ " available before: " + applications.size() + " available now: " + numberOfApps);

		TextView instructionsView = (TextView) getActivity()
				.findViewById(R.id.availableApkHeaderInstructions);
		// show hint depending the number of available apps
		if (instructionsView != null) {
			if (numberOfApps <= 0) { //	changed from: if (applications.size() == 0)
				instructionsView.setText(R.string.availableApkList_emptyHint);
			} else {
				instructionsView.setText(R.string.availableApkList_defaultHint);
			}
		}
		
		List<Map<String, String>> listContent = new LinkedList<Map<String, String>>();
		int i = 0, j = 0;
		for (ExternalApplication app : applications) {
			HashMap<String, String> rowMap = new HashMap<String, String>();
			// only add it to displayed list, if it's not installed or in history
			if (!hashAppIDs.contains(app.getID()) && !realHistApps.contains(app)) {
				rowMap.put("name", app.getName());
				listContent.add(rowMap);
				listIndex.put(j, i);
				j++;
			}
			i++;
		}
		
		MosesListAdapter contentAdapter = new MosesListAdapter(getActivity(), 
				listContent, 
				R.layout.availableapkslistitem,
				new String[] { "name" }, 
				new int[] { R.id.apklistitemtext });
		
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

	/**
	 * @see com.actionbarsherlock.app.SherlockListFragment#onAttach(android.app.Activity)
	 */
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
	}

	/**
	 * @see com.actionbarsherlock.app.SherlockListFragment#onDetach()
	 */
	@Override
	public void onDetach() {
		super.onDetach();
	}
	
	
}
