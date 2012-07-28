package de.da_sense.moses.client.abstraction.apks;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import de.da_sense.moses.client.ViewAvailableApkActivity;
import de.da_sense.moses.client.abstraction.ApkMethods;
import de.da_sense.moses.client.abstraction.ESensor;
import de.da_sense.moses.client.abstraction.ExternalApplicationInfoRetriever;
import de.da_sense.moses.client.abstraction.ExternalApplicationInfoRetriever.State;
import de.da_sense.moses.client.R;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Point;
import de.da_sense.moses.client.service.helpers.ExecutableForObject;
import de.da_sense.moses.client.util.Log;
import android.text.method.ScrollingMovementMethod;
import android.view.Display;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.Gallery;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * References an installed application (additional to ExternalApplication, the
 * package name of the installed application must be specified)
 * 
 * @author Simon L
 * 
 */
public class InstalledExternalApplication extends ExternalApplication {

	private static final String SEPARATOR = "#IEA#";
	private String packageName;
	private boolean wasInstalledAsUserStudy;
	private boolean updateAvailable;
	private String installedVersion;

	/**
	 * Creates the reference to the external application by specifying the
	 * package name and version number
	 * 
	 * @param packageName
	 *            the name of the package of the application
	 * @param ID
	 *            the moses id of the application
	 * @param wasInstalledAsUserStudy
	 * @param version
	 *            the version of the app which is installed
	 */
	public InstalledExternalApplication(String packageName, String ID,
			boolean wasInstalledAsUserStudy, String version) {
		super(ID);

		// assume this version as the newest version
		String newestVersion = version;
		super.setNewestVersion(newestVersion);
		this.installedVersion = version;

		this.wasInstalledAsUserStudy = wasInstalledAsUserStudy;
		this.packageName = packageName;

		this.updateAvailable = false;
	}

	/**
	 * Creates the instance by adapting an already existing ExternalApplication
	 * reference. This has the advantage of copying already retrieved name and
	 * description over.
	 * 
	 * @param packageName
	 *            the package name of the installed app
	 * @param externalApp
	 *            the preexisting reference that will be adapted
	 * @param wasInstalledAsUserStudy
	 * @param version
	 *            the version of the app which is installed
	 */
	public InstalledExternalApplication(String packageName,
			ExternalApplication externalApp, boolean wasInstalledAsUserStudy,
			String version) {

		this(packageName, externalApp.getID(), wasInstalledAsUserStudy, version);

		if (externalApp.isDescriptionSet()) {
			setDescription(externalApp.getDescription());
		}
		if (externalApp.isNameSet()) {
			setName(externalApp.getName());
		}
		if (externalApp.isSensorsSet()) {
			setSensors(externalApp.getSensors());
		}
		if (externalApp.isNewestVersionSet()) {
			super.setNewestVersion(externalApp.getNewestVersion());
		} else {
			// assume this version as the newest version
			String newestVersion = version;
			super.setNewestVersion(newestVersion);
		}
	}

	/**
	 * Creates the instance by adapting an already existing ExternalApplication
	 * reference. This has the advantage of copying already retrieved name and
	 * description over. The external applicatiopn object must have its
	 * installed version set!
	 * 
	 * @param packageName
	 *            the package name of the installed app
	 * @param externalApp
	 *            the preexisting reference that will be adapted
	 * @param wasInstalledAsUserStudy
	 */
	public InstalledExternalApplication(String packageName,
			ExternalApplication externalApp, boolean wasInstalledAsUserStudy) {

		this(packageName, externalApp, wasInstalledAsUserStudy, externalApp
				.getNewestVersion());
	}

	public static Dialog showAppInfoDialog(Context c, String name, String desc,
			List<Integer> sensors) {
		final Dialog d = new Dialog(c);
		d.setContentView(R.layout.app_info_dialog);
		d.setTitle("Application informations:");

		TextView t = (TextView) d.findViewById(R.id.appname);
		t.setText(name);
		t = (TextView) d.findViewById(R.id.description);
		t.setMovementMethod(ScrollingMovementMethod.getInstance());
		t.setText(desc);
		Gallery g = (Gallery) d.findViewById(R.id.sensors);
		Integer[] imageIds = new Integer[sensors.size()];
		String[] alternateText = new String[sensors.size()];
		for (int i = 0; i < sensors.size(); ++i) {
			imageIds[i] = ESensor.values()[sensors.get(i)].imageID();
			alternateText[i] = ESensor.values()[sensors.get(i)].toString();
		}
		g.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				((TextView) d.findViewById(R.id.sensorname))
						.setText(((ImageView) arg1).getContentDescription());
			}

		});
		g.setAdapter(new ImageAdapter(c, imageIds, alternateText));
		return d;
	}

	public static WindowManager.LayoutParams getDialogSize(Context c, Dialog d) {
		WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
		lp.copyFrom(d.getWindow().getAttributes());
		WindowManager wm = (WindowManager) c
				.getSystemService(Context.WINDOW_SERVICE);
		Display display = wm.getDefaultDisplay();
		if (display.getWidth() > display.getHeight()) { // Landscape
			if (display.getWidth() <= 426) {
			    lp.width = (int)(WindowManager.LayoutParams.MATCH_PARENT);
			} else {
				lp.width = (int)(WindowManager.LayoutParams.MATCH_PARENT);
			}

			if (display.getHeight() <= 320) {
			    lp.width = (int)(WindowManager.LayoutParams.MATCH_PARENT);
			} else {
			    lp.width = (int)(WindowManager.LayoutParams.MATCH_PARENT);
			}
		} else { // Portrait
			if (display.getWidth() <= 320) {
			    lp.width = (int)(WindowManager.LayoutParams.MATCH_PARENT);
			} else {
			    lp.width = (int)(WindowManager.LayoutParams.MATCH_PARENT); // TODO Ibrahim : Es war 320
			}

			if (display.getHeight() <= 426) {
			    lp.width = (int)(WindowManager.LayoutParams.MATCH_PARENT);
			} else {
			    lp.width = (int)(WindowManager.LayoutParams.MATCH_PARENT); // TODO Ibrahim : Es war 426
			}
		}
		return lp;
	}

	/**
	 * starts the application this object is referencing
	 * 
	 * @param baseActivity
	 * @throws NameNotFoundException
	 *             should only occur if the application was uninstalled after
	 *             the creation of this InstalledExternalApplication instance.
	 */
	public void startApplication(final Activity baseActivity) {
		ProgressDialog pd = new ProgressDialog(baseActivity);
		pd.setTitle("Application informations:");
		pd.setMessage("Retreiving data...");
		pd.show();
		final Dialog d = showAppInfoDialog(baseActivity, getName(),
				getDescription(), getSensors());
		Button b = (Button) d.findViewById(R.id.startapp);
		b.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				try {
					ApkMethods.startApplication(packageName, baseActivity);
				} catch (NameNotFoundException e) {
					Log.e("MoSeS.APK",
							"Appstart: app was not found - maybe because it was uninstalled since last database refresh");
				}
			}
		});

		b = (Button) d.findViewById(R.id.update);
		b.setVisibility(updateAvailable ? View.VISIBLE : View.GONE);

		final UpdateObserver updateObserver = new UpdateObserver() {
			@Override
			public void unsuccessful_exit() {
				// Message should've been already shown; just do nothing here
				if (!d.isShowing())
					d.show();
			}

			@Override
			public void success(InstalledExternalApplication updatedApp) {
				if (d.isShowing())
					d.dismiss();
			}

			@Override
			public void manual_abort() {
				if (!d.isShowing())
					d.show();
			}
		};
		b.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (d.isShowing())
					d.hide();
				InstalledExternalApplication.this.fetchUpdatedInfo(
						baseActivity, updateObserver);
			}
		});

		b = (Button) d.findViewById(R.id.close);
		b.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				d.dismiss();
			}
		});
		pd.dismiss();
		d.show();
		d.getWindow().setAttributes(getDialogSize(baseActivity, d));
	}

	protected void fetchUpdatedInfo(final Activity baseActivity,
			final UpdateObserver o) {
		final ExternalApplicationInfoRetriever infoRequester = new ExternalApplicationInfoRetriever(
				this.getID(), baseActivity);
		final ProgressDialog progressDialog = ProgressDialog.show(baseActivity,
				"Loading...", "Loading userstudy information", true, true,
				new OnCancelListener() {
					@Override
					public void onCancel(DialogInterface dialog) {
						infoRequester.cancel();
						o.manual_abort();
					}
				});
		infoRequester.sendEvenWhenNoNetwork = false;
		infoRequester.addObserver(new Observer() {
			@Override
			public void update(Observable observable, Object data) {
				if (infoRequester.getState() == State.DONE) {
					final ExternalApplication updatedApplication = new ExternalApplication(
							InstalledExternalApplication.this.getID());
					updatedApplication.setName(infoRequester.getResultName());
					updatedApplication.setDescription(infoRequester
							.getResultDescription());
					updatedApplication.setSensors(infoRequester
							.getResultSensors());
					progressDialog.dismiss();
					ViewAvailableApkActivity.showAppInfo(updatedApplication,
							baseActivity, new Runnable() {
								@Override
								public void run() {
									startInstallUpdate(updatedApplication,
											baseActivity, o);
								}
							}, new Runnable() {
								@Override
								public void run() {
									o.manual_abort();
								}
							});
				}
				if (infoRequester.getState() == State.ERROR) {
					Log.e("MoSeS.USERSTUDY",
							"Wanted to display user study, but couldn't get app informations because of: ",
							infoRequester.getException());
					progressDialog.dismiss();
					showMessageBoxError(baseActivity, "Error",
							"Error when retrieving update information.",
							errorMessageBoxOkayBtnListener(o));
				}
				if (infoRequester.getState() == State.NO_NETWORK) {
					Log.d("MoSeS.USERSTUDY",
							"Wanted to display user study, but couldn't get app informations because of: ",
							infoRequester.getException());
					progressDialog.dismiss();
					showMessageBoxErrorNoConnection(baseActivity, o);
				}
			}
		});
		infoRequester.start();
	}
	
	private int totalSize = -1;

	protected void startInstallUpdate(
			final ExternalApplication updatedApplication,
			final Activity baseActivity, final UpdateObserver o) {
		final ProgressDialog progressDialog = new ProgressDialog(baseActivity);
		final ApkDownloadManager downloader = new ApkDownloadManager(this,
				baseActivity, new ExecutableForObject() {

					@Override
					public void execute(final Object o) {
						if (o instanceof Integer) {
							baseActivity.runOnUiThread(new Runnable() {

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
		progressDialog.setButton("Cancel",
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
				if (downloader.getState() == ApkDownloadManager.State.ERROR_NO_CONNECTION) {
					progressDialog.dismiss();
					showMessageBoxErrorNoConnection(baseActivity, o);
				} else if (downloader.getState() == ApkDownloadManager.State.ERROR) {
					progressDialog.dismiss();
					showMessageBoxErrorDownloading(downloader, baseActivity, o);
				} else if (downloader.getState() == ApkDownloadManager.State.FINISHED) {
					progressDialog.dismiss();
					installDownloadedApk(downloader.getDownloadedApk(),
							updatedApplication, baseActivity, o);
				}
			}
		};
		downloader.addObserver(observer);
		totalSize = -1;
		progressDialog.show();
		downloader.start();
	}

	// /-----------------------------

	private void installDownloadedApk(final File result,
			final ExternalApplication updatedApplication,
			final Activity baseActivity, final UpdateObserver o) {
		final ApkInstallManager installer = new ApkInstallManager(result, this);
		installer.addObserver(new Observer() {
			@Override
			public void update(Observable observable, Object data) {
				if (installer.getState() == ApkInstallManager.State.ERROR) {
					showMessageBoxError(
							baseActivity,
							"Error",
							"An error occured when installing the user study app. Sorry!",
							errorMessageBoxOkayBtnListener(o));
				} else if (installer.getState() == ApkInstallManager.State.INSTALLATION_CANCELLED) {
					o.manual_abort();
				} else if (installer.getState() == ApkInstallManager.State.INSTALLATION_COMPLETED) {
					try {
						o.success(ApkInstallManager.registerInstalledApk(
								result, updatedApplication, baseActivity,
								InstalledExternalApplication.this
										.wasInstalledAsUserStudy()));
					} catch (IOException e) {
						Log.e("MoSeS.Install",
								"Problems with extracting package name from apk, or problems with the InstalledExternalApplicationsManager after installing an app");
						showMessageBoxError(
								baseActivity,
								"Error",
								"An error occured when saving the app database.",
								errorMessageBoxOkayBtnListener(o));
					}
				}
			}
		});
		installer.start();
	}

	protected void showMessageBoxErrorNoConnection(Activity baseActivity,
			UpdateObserver o) {
		showMessageBoxError(
				baseActivity,
				"No connection",
				"There seems to be no open internet connection present for downloading the app.",
				errorMessageBoxOkayBtnListener(o));
	}

	protected void showMessageBoxErrorDownloading(
			ApkDownloadManager downloader, Activity baseActivity,
			UpdateObserver o) {
		showMessageBoxError(baseActivity, "Error",
				"An error occured when trying to download the app: "
						+ downloader.getErrorMsg() + ".\nSorry!",
				errorMessageBoxOkayBtnListener(o));
	}

	protected void showMessageBoxError(Activity baseActivity, String title,
			String msg, DialogInterface.OnClickListener onClickListener) {
		new AlertDialog.Builder(baseActivity).setMessage(msg).setTitle(title)
				.setCancelable(true).setNeutralButton("OK", onClickListener)
				.show();
	}

	private DialogInterface.OnClickListener errorMessageBoxOkayBtnListener(
			final UpdateObserver o) {
		return new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int whichButton) {
				o.unsuccessful_exit();
			}
		};
	}

	// ----------------------------------

	/**
	 * @return the package name of the referenced application
	 */
	public String getPackageName() {
		return packageName;
	}

	public boolean wasInstalledAsUserStudy() {
		return wasInstalledAsUserStudy;
	}

	/**
	 * @return the version of the app which was installed
	 */
	public String getInstalledVersion() {
		return installedVersion;
	}

	/**
	 * @param installedVersion
	 *            sets the installed version
	 */
	public void setInstalledVersion(String installedVersion) {
		this.installedVersion = installedVersion;
	}

	@Override
	public String toString() {
		return packageName;
	}

	@Override
	public boolean equals(Object o) {
		if (o instanceof InstalledExternalApplication) {
			if (this.getPackageName() == null)
				return false;
			return this.getPackageName().equals(
					((InstalledExternalApplication) o).getPackageName());
		} else {
			return false;
		}
	}

	@Override
	public String asOnelineString() {
		return super.asOnelineString() + SEPARATOR + this.getPackageName()
				+ SEPARATOR
				+ Boolean.valueOf(wasInstalledAsUserStudy).toString()
				+ SEPARATOR + installedVersion + SEPARATOR
				+ Boolean.valueOf(updateAvailable).toString();
	}

	/**
	 * creates an installed external application from a string (@see
	 * {@link #asOnelineString()})
	 * 
	 * @param s
	 *            the string-encoded installed external application
	 * @return the decoded installed external application
	 */
	public static InstalledExternalApplication fromOnelineString(String s) {
		String[] split = s.split(SEPARATOR);
		ExternalApplication exApp = ExternalApplication
				.fromOnelineString(split[0]);
		InstalledExternalApplication result = new InstalledExternalApplication(
				split[1], exApp, Boolean.parseBoolean(split[2]), split[3]);
		boolean isUpdateAvailable = Boolean.parseBoolean(split[4]);
		result.setUpdateAvailable(isUpdateAvailable);
		return result;
	}

	public boolean isUpdateAvailable() {
		return updateAvailable;
	}

	public void setUpdateAvailable(boolean updateAvailable) {
		this.updateAvailable = updateAvailable;
	}

	@Override
	public boolean isDataComplete() {
		return super.isDataComplete();
	}

}
