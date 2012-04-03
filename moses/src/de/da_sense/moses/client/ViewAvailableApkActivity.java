package de.da_sense.moses.client;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;

import org.json.JSONArray;
import org.json.JSONException;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.Gallery;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import de.da_sense.moses.client.abstraction.ApkListRequestObserver;
import de.da_sense.moses.client.abstraction.ApkMethods;
import de.da_sense.moses.client.abstraction.ESensor;
import de.da_sense.moses.client.abstraction.apks.APKInstalled;
import de.da_sense.moses.client.abstraction.apks.ApkDownloadManager;
import de.da_sense.moses.client.abstraction.apks.ApkInstallManager;
import de.da_sense.moses.client.abstraction.apks.ExternalApplication;
import de.da_sense.moses.client.abstraction.apks.ImageAdapter;
import de.da_sense.moses.client.preferences.MosesPreferences;
import de.da_sense.moses.client.service.MosesService;
import de.da_sense.moses.client.service.helpers.ExecutorWithObject;

/**
 * Viewing and installing apks from the server
 * 
 * @author Simon L
 */
public class ViewAvailableApkActivity extends ListActivity implements ApkListRequestObserver {

	private static enum LayoutState {
		NORMAL_LIST, SENSORS_HINT, EMPTYLIST_HINT, PENDINGREQUEST, NOCONNECTIVITY;
	}

	private static final String PREFKEY_SHOW_SET_SENSORS_HINT = "showInitialSetSensorsHint";
	private static final int REFRESH_THRESHHOLD = 800;
	private ListView listView;
	private List<ExternalApplication> externalApps;
	private Long lastListRefreshTime = null;
	LayoutState lastSetLayout = null;

	public void setLastSetLayout(LayoutState lastSetLayout) {
		this.lastSetLayout = lastSetLayout;
		Log.d("MoSeS.UI", "Layouted showAvailableApkList to state " + lastSetLayout);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.availableapklist);

		initControls();
	}

	public void apkInstallClickHandler(View v) {
		if (MosesService.isOnline(getApplicationContext()) && v != null) {
			int pos = listView.getPositionForView(v);
			final ExternalApplication app = externalApps.get(pos);
			showAppInfo(app, this, new Runnable() {
				@Override
				public void run() {
					handleInstallApp(app);
				}
			}, new Runnable() {
				@Override
				public void run() {
				}
			});
		} else {
			showNoConnectionInfoBox();
		}
	}

	private void showNoConnectionInfoBox() {
		new AlertDialog.Builder(this)
				.setMessage("Cannot display the app information because no internet connection seems to be present")
				.setTitle("No connection").setCancelable(true)
				// .setNeutralButton("OK",
				// new DialogInterface.OnClickListener() {
				// public void onClick(DialogInterface dialog, int
				// whichButton){}
				// })
				.show();
	}

	public static void showAppInfo(final ExternalApplication app, Activity baseActivity,
			final Runnable installAppClickAction, final Runnable cancelClickAction) {
		ProgressDialog pd = new ProgressDialog(baseActivity);
		pd.setTitle("Application informations:");
		pd.setMessage("Retreiving data...");
		pd.show();
		final Dialog d = new Dialog(baseActivity);
		d.setContentView(R.layout.app_info_dialog);
		d.setTitle("App informations:");
		WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
		lp.copyFrom(d.getWindow().getAttributes());
		lp.width = LayoutParams.FILL_PARENT;
		lp.height = LayoutParams.FILL_PARENT;

		TextView t = (TextView) d.findViewById(R.id.appname);
		t.setText(app.getName());
		t = (TextView) d.findViewById(R.id.description);
		t.setText(app.getDescription());
		Gallery g = (Gallery) d.findViewById(R.id.sensors);
		Integer[] imageIds = new Integer[app.getSensors().size()];
		String[] alternateText = new String[app.getSensors().size()];
		for (int i = 0; i < app.getSensors().size(); ++i) {
			imageIds[i] = ESensor.values()[app.getSensors().get(i)].imageID();
			alternateText[i] = ESensor.values()[app.getSensors().get(i)].toString();
		}
		g.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				((TextView) d.findViewById(R.id.sensorname)).setText(((ImageView) arg1).getContentDescription());
			}

		});
		g.setAdapter(new ImageAdapter(baseActivity, imageIds, alternateText));
		Button b = (Button) d.findViewById(R.id.startapp);
		b.setText("Install");
		b.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Log.i("MoSes.Install", "starting install process for app " + app.toString());
				d.dismiss();
				installAppClickAction.run();
			}
		});
		b = (Button) d.findViewById(R.id.update);
		b.setVisibility(View.GONE);
		b = (Button) d.findViewById(R.id.close);
		b.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				d.dismiss();
				cancelClickAction.run();
			}
		});
		pd.dismiss();
		d.show();
		d.getWindow().setAttributes(lp);
	}

	/**
	 * Inits the controls.
	 */
	private void initControls() {
		initControlsOnRequestApks();
		requestExternalApplications();
	}

	private boolean appsLocallyInCacheStillAvailable() {
		return externalApps != null && externalApps.size() > 0;
	}

	private void initControlsOnRequestApks() {
		if (showInitialSensorHint()) {
			initControlsShowSensorsHint();
		} else {
			if (appsLocallyInCacheStillAvailable()) {
				initControlsNormalList(externalApps);
			} else {
				if (MosesService.isOnline(getApplicationContext())) {
					initControlsPendingListRequest();
				} else {
					initControlsNoConnectivity();
				}
			}
		}
	}

	private void initControlsNoConnectivity() {
		if (lastSetLayout != LayoutState.NOCONNECTIVITY) {
			LinearLayout emptylistCtrls = (LinearLayout) findViewById(R.id.apklist_emptylistLayout);
			emptylistCtrls.setVisibility(View.VISIBLE);
			LinearLayout apkListCtrls = (LinearLayout) findViewById(R.id.apklist_mainListLayout);
			apkListCtrls.setVisibility(View.GONE);

			TextView mainHint = (TextView) findViewById(R.id.apklist_emptylistHintMain);
			mainHint.setText(R.string.apklist_hint_noconnectivity);

			final Button actionBtn1 = (Button) findViewById(R.id.apklist_emptylistActionBtn1);
			final Button actionBtn2 = (Button) findViewById(R.id.apklist_emptylistActionBtn2);
			actionBtn1.setText("Refresh");
			actionBtn2.setVisibility(View.GONE);

			refreshResfreshBtnTimeout(actionBtn1, "Retry", LayoutState.NOCONNECTIVITY);

			actionBtn1.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					refreshResfreshBtnTimeout(actionBtn1, "Retry", LayoutState.NOCONNECTIVITY);
					requestExternalApplications();
				}
			});
			setLastSetLayout(LayoutState.NOCONNECTIVITY);
		}
	}

	private void initControlsPendingListRequest() {
		if (lastSetLayout != LayoutState.PENDINGREQUEST) {
			LinearLayout emptylistCtrls = (LinearLayout) findViewById(R.id.apklist_emptylistLayout);
			emptylistCtrls.setVisibility(View.VISIBLE);
			LinearLayout apkListCtrls = (LinearLayout) findViewById(R.id.apklist_mainListLayout);
			apkListCtrls.setVisibility(View.GONE);

			TextView mainHint = (TextView) findViewById(R.id.apklist_emptylistHintMain);
			mainHint.setText(R.string.apklist_hint_pendingrequest);

			final Button actionBtn1 = (Button) findViewById(R.id.apklist_emptylistActionBtn1);
			final Button actionBtn2 = (Button) findViewById(R.id.apklist_emptylistActionBtn2);
			actionBtn1.setText("Refresh");
			actionBtn2.setVisibility(View.GONE);

			refreshResfreshBtnTimeout(actionBtn1, "Refresh", LayoutState.PENDINGREQUEST);

			actionBtn1.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					refreshResfreshBtnTimeout(actionBtn1, "Refresh", LayoutState.PENDINGREQUEST);
					requestExternalApplications();
				}
			});
			setLastSetLayout(LayoutState.PENDINGREQUEST);
		}
	}

	private void refreshResfreshBtnTimeout(final Button refreshButton, final String minimalString,
			final LayoutState parentLayout) {
		refreshButton.setEnabled(false);
		refreshButton.setText(minimalString);
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
		enableRefreshHandler.postDelayed(new Runnable() {
			@Override
			public void run() {
				if (!isPaused && lastSetLayout == parentLayout) {
					refreshButton.setEnabled(true);
				}
			}
		}, 3000);
	}

	private void initLayoutFromArrivedList(List<ExternalApplication> applications) {
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
		populateList(applications);
	}

	private void initControlsEmptyArrivedList(boolean mayShowSensorsList) {
		if (mayShowSensorsList) {
			initControlsShowSensorsHint();
		} else {
			if (lastSetLayout != LayoutState.EMPTYLIST_HINT) {
				LinearLayout emptylistCtrls = (LinearLayout) findViewById(R.id.apklist_emptylistLayout);
				emptylistCtrls.setVisibility(View.VISIBLE);
				LinearLayout apkListCtrls = (LinearLayout) findViewById(R.id.apklist_mainListLayout);
				apkListCtrls.setVisibility(View.GONE);

				TextView mainHint = (TextView) findViewById(R.id.apklist_emptylistHintMain);
				mainHint.setText(R.string.availableApkList_emptyHint);

				Button actionBtn1 = (Button) findViewById(R.id.apklist_emptylistActionBtn1);
				Button actionBtn2 = (Button) findViewById(R.id.apklist_emptylistActionBtn2);
				actionBtn1.setVisibility(View.GONE);
				actionBtn2.setVisibility(View.GONE);
				setLastSetLayout(LayoutState.EMPTYLIST_HINT);
			}
		}
	}

	private void initControlsNormalList(List<ExternalApplication> applications) {
		LinearLayout emptylistCtrls = (LinearLayout) findViewById(R.id.apklist_emptylistLayout);
		emptylistCtrls.setVisibility(View.GONE);
		LinearLayout apkListCtrls = (LinearLayout) findViewById(R.id.apklist_mainListLayout);
		apkListCtrls.setVisibility(View.VISIBLE);

		setLastSetLayout(LayoutState.NORMAL_LIST);
		populateList(applications);
	}

	private void initControlsShowSensorsHint() {
		if (lastSetLayout != LayoutState.SENSORS_HINT) {
			LinearLayout emptylistCtrls = (LinearLayout) findViewById(R.id.apklist_emptylistLayout);
			emptylistCtrls.setVisibility(View.VISIBLE);
			LinearLayout apkListCtrls = (LinearLayout) findViewById(R.id.apklist_mainListLayout);
			apkListCtrls.setVisibility(View.GONE);

			TextView mainHint = (TextView) findViewById(R.id.apklist_emptylistHintMain);
			mainHint.setText(R.string.apklist_hint_sensors_main);

			Button actionBtn1 = (Button) findViewById(R.id.apklist_emptylistActionBtn1);
			Button actionBtn2 = (Button) findViewById(R.id.apklist_emptylistActionBtn2);
			actionBtn1.setText("Yes");
			actionBtn2.setText("No, Thanks");
			actionBtn2.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					PreferenceManager.getDefaultSharedPreferences(ViewAvailableApkActivity.this).edit()
							.putBoolean(PREFKEY_SHOW_SET_SENSORS_HINT, false).commit();
					initControls();
				}
			});
			actionBtn1.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					PreferenceManager.getDefaultSharedPreferences(ViewAvailableApkActivity.this).edit()
							.putBoolean(PREFKEY_SHOW_SET_SENSORS_HINT, false).commit();
					invokeSensorDialog();
				}
			});
			setLastSetLayout(LayoutState.SENSORS_HINT);
		}
	}

	protected void invokeSensorDialog() {
		Intent startPreference = new Intent(getApplicationContext(), MosesPreferences.class);
		startPreference.putExtra("startSensors", true);
		startActivity(startPreference);
	}

	private int totalSize = -1;

	private boolean showInitialSensorHint() {
		boolean enoughEnabledSensors = false;
		try {
			JSONArray sensors = new JSONArray(PreferenceManager.getDefaultSharedPreferences(this).getString(
					"sensor_data", "[]"));
			enoughEnabledSensors = !(sensors != null && sensors.length() < 1);
		} catch (JSONException e) {
			enoughEnabledSensors = false;
		}
		boolean doShow = PreferenceManager.getDefaultSharedPreferences(this).getBoolean(PREFKEY_SHOW_SET_SENSORS_HINT,
				true);
		if (enoughEnabledSensors) {
			doShow = false;
			PreferenceManager.getDefaultSharedPreferences(this).edit().putBoolean(PREFKEY_SHOW_SET_SENSORS_HINT, true)
					.commit();
		}

		return doShow;
	}

	private void handleInstallApp(ExternalApplication app) {
		final ProgressDialog progressDialog = new ProgressDialog(this);
		final ApkDownloadManager downloader = new ApkDownloadManager(app, getApplicationContext(),
				new ExecutorWithObject() {

					@Override
					public void execute(final Object o) {
						if (o instanceof Integer) {
							runOnUiThread(new Runnable() {

								@Override
								public void run() {
									if (totalSize == -1) {
										totalSize = (Integer) o / 1024;
										progressDialog.setMax(totalSize);
									} else {
										progressDialog.incrementProgressBy(((Integer) o / 1024)
												- progressDialog.getProgress());
									}
								}
							});

						}
					}
				});
		progressDialog.setTitle("Downloading the app...");
		progressDialog.setMessage("Please wait.");
		progressDialog.setMax(0);
		progressDialog.setProgress(0);
		progressDialog.setOnCancelListener(new OnCancelListener() {

			@Override
			public void onCancel(DialogInterface dialog) {
				downloader.cancel();
			}
		});
		progressDialog.setCancelable(true);
		progressDialog.setButton("Cancel", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				progressDialog.cancel();
			}
		});
		progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
		Observer observer = new Observer() {
			@Override
			public void update(Observable observable, Object data) {
				if (downloader.getState() == ApkDownloadManager.State.ERROR) {
					progressDialog.dismiss();
					showMessageBoxErrorDownloading(downloader);
				} else if (downloader.getState() == ApkDownloadManager.State.ERROR_NO_CONNECTION) {
					progressDialog.dismiss();
					showMessageBoxErrorNoConnection(downloader);
				} else if (downloader.getState() == ApkDownloadManager.State.FINISHED) {
					progressDialog.dismiss();
					installDownloadedApk(downloader.getDownloadedApk(), downloader.getExternalApplicationResult());
				}
			}
		};
		downloader.addObserver(observer);
		totalSize = -1;
		progressDialog.show();
		downloader.start();
	}

	protected void showMessageBoxErrorNoConnection(ApkDownloadManager downloader) {
		new AlertDialog.Builder(ViewAvailableApkActivity.this)
				.setMessage("There seems to be no open internet connection present for downloading the app.")
				.setTitle("No connection").setCancelable(true)
				.setNeutralButton("OK", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int whichButton) {
					}
				}).show();
	}

	protected void showMessageBoxErrorDownloading(ApkDownloadManager downloader) {
		new AlertDialog.Builder(ViewAvailableApkActivity.this)
				.setMessage(
						"An error occured when trying to download the app: " + downloader.getErrorMsg() + ".\nSorry!")
				.setTitle("Error").setCancelable(true).setNeutralButton("OK", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int whichButton) {
					}
				}).show();
	}

	private void installDownloadedApk(final File originalApk, final ExternalApplication externalAppRef) {
		final ApkInstallManager installer = new ApkInstallManager(originalApk, externalAppRef);
		installer.addObserver(new Observer() {
			@Override
			public void update(Observable observable, Object data) {
				if (installer.getState() == ApkInstallManager.State.ERROR) {
				} else if (installer.getState() == ApkInstallManager.State.INSTALLATION_CANCELLED) {
					// TODO:how to handle if the user cancels the installation?
				} else if (installer.getState() == ApkInstallManager.State.INSTALLATION_COMPLETED) {
					new APKInstalled(externalAppRef.getID());
					try {
						ApkInstallManager.registerInstalledApk(originalApk, externalAppRef,
								ViewAvailableApkActivity.this.getApplicationContext(), false);
					} catch (IOException e) {
						Log.e("MoSeS.Install",
								"Problems with extracting package name from apk, or problems with the InstalledExternalApplicationsManager after installing an app");
					}
				}
			}
		});
		installer.start();
	}

	int requestListRetries = 0;
	private boolean isPaused;

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
				// TODO:show error when all retries didn't work?
			}
		} else {
			requestListRetries = 0;
			lastListRefreshTime = System.currentTimeMillis();
			ApkMethods.getExternalApplications(ViewAvailableApkActivity.this);
			initControlsOnRequestApks();
		}
	}

	@Override
	public void apkListRequestFinished(List<ExternalApplication> applications) {
		externalApps = applications;
		initLayoutFromArrivedList(applications);
	}

	@Override
	public void apkListRequestFailed(Exception e) {
		// TODO: receive failures that point out no connection, too.
		// TODO: show user some hint about this
		Log.w("MoSeS.APKMETHODS", "invalid response for apk list request: " + e.getMessage());
	}

	@Override
	public void onWindowFocusChanged(boolean hasFocus) {
		if (hasFocus && (lastListRefreshTime == null) ? true
				: (System.currentTimeMillis() - lastListRefreshTime > REFRESH_THRESHHOLD)) {
			requestExternalApplications();
		}
	}

	@Override
	protected void onPause() {
		super.onPause();
		this.isPaused = true;
	}

	@Override
	protected void onResume() {
		super.onResume();
		this.isPaused = false;
		if (lastSetLayout == LayoutState.SENSORS_HINT) {
			initControls();
		} else {
			if ((lastListRefreshTime == null) ? true
					: (System.currentTimeMillis() - lastListRefreshTime > REFRESH_THRESHHOLD)) {
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

	private void populateList(List<ExternalApplication> applications) {
		listView = getListView();
		String[] items = new String[applications.size()];
		int counter = 0;
		for (ExternalApplication app : applications) {
			items[counter] = app.getName();
			counter++;
		}

		TextView instructionsView = (TextView) findViewById(R.id.availableApkHeaderInstructions);
		if (instructionsView != null) {
			if (applications.size() == 0) {
				instructionsView.setText(R.string.availableApkList_emptyHint);
			} else {
				instructionsView.setText(R.string.availableApkList_defaultHint);
			}
		}

		List<Map<String, String>> listContent = new LinkedList<Map<String, String>>();
		for (ExternalApplication app : applications) {
			HashMap<String, String> rowMap = new HashMap<String, String>();
			rowMap.put("name", app.getName());
			rowMap.put("description", app.getDescription());
			listContent.add(rowMap);

		}
		SimpleAdapter contentAdapter = new SimpleAdapter(this, listContent, R.layout.availableabkslistitem,
				new String[] { "name", "description" }, new int[] { R.id.apklistitemtext, R.id.apklistitemdescription });

		listView.setAdapter(contentAdapter);
	}

	public static String concatStacktrace(Exception e) {
		String stackTrace = "";
		for (int i = 0; i < e.getStackTrace().length; i++) {
			stackTrace += e.getStackTrace()[i];
		}
		return stackTrace;
	}

}
