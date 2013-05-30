package de.da_sense.moses.client;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;
import java.util.Queue;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.Gallery;
import android.widget.ImageView;
import android.widget.TextView;
import de.da_sense.moses.client.abstraction.ESensor;
import de.da_sense.moses.client.abstraction.ExternalApplicationInfoRetriever;
import de.da_sense.moses.client.abstraction.ExternalApplicationInfoRetriever.State;
import de.da_sense.moses.client.abstraction.apks.APKInstalled;
import de.da_sense.moses.client.abstraction.apks.ApkDownloadManager;
import de.da_sense.moses.client.abstraction.apks.ApkInstallManager;
import de.da_sense.moses.client.abstraction.apks.ExternalApplication;
import de.da_sense.moses.client.abstraction.apks.ImageAdapter;
import de.da_sense.moses.client.service.helpers.ExecutableForObject;
import de.da_sense.moses.client.userstudy.UserStudyNotification;
import de.da_sense.moses.client.userstudy.UserStudyNotification.Status;
import de.da_sense.moses.client.userstudy.UserstudyNotificationManager;
import de.da_sense.moses.client.util.Log;

/**
 * Viewing and installing APKs from the server
 * 
 * @author Simon L
 */
public class ViewUserStudyActivity extends Activity {

	public static final String EXTRA_USER_STUDY_APK_ID = "UserStudyApkId";
	private UserStudyNotification handleSingleNotificationData;

	/**
	 * this public accessible queue can be used to queue auto
	 * accept/decline/later actions for notifications
	 */
	public static Queue<UserStudyNotification.Status> autoActions = new LinkedList<UserStudyNotification.Status>();

	/*
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// TODO: remove userstudy NOTIFICATION if it still exists in the bar,
		// because this could've been called from the "later" list (very
		// unlikely thou: requires double notification)
		String studyApkId = getIntent().getExtras().getString(EXTRA_USER_STUDY_APK_ID);
		if (studyApkId != null) {

			if (!WelcomeActivity.isLoginInformationComplete(this)) {
				// if either username or password are not set, redirect to moses
				// ui for login

				Intent intent = new Intent(this, WelcomeActivity.class);
				intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				intent.putExtra(ViewUserStudyActivity.EXTRA_USER_STUDY_APK_ID, studyApkId);
				startActivity(intent);
				finish();
			} else {
				UserStudyNotification notification = UserstudyNotificationManager.getInstance()
						.getNotificationForApkId(studyApkId);
				showActivityForNotification(notification);
			}
		} else {
			showActivityForNotification(null);
		}
	}

	/**
	 * Show the decision dialog for the given user study notification.
	 * @param notification
	 */
	private void showActivityForNotification(UserStudyNotification notification) {
		if (notification != null) {
			this.handleSingleNotificationData = notification;
			if (!notification.isDataComplete()) {
				requestApkInfo(notification.getApplication().getID());
			} else {
				showDescisionDialog(notification);
			}
		} else {
			Log.e("MoSeS.USERSTUDY", "aborting userstudy operation; no data");
			cancelActivity();
		}
	}

	/**
	 * sets result: cancelled and finishes.
	 */
	private void cancelActivity() {
		setResult(Activity.RESULT_CANCELED);
		finish();
	}

	/**
	 * sets result: ok and finishes
	 */
	protected void finishActivityOK() {
		setResult(Activity.RESULT_OK);
		finish();
	}
	
	/**
	 * Requests the information for the user study with the application.
	 * @param id the apkid for the user study app
	 */
	private void requestApkInfo(final String id) {
		// TODO: this was already commented out ... probably save to remove
		// if(id.equals("11")) {
		// handleSingleNotificationData.getApplication().setName("Test");
		// handleSingleNotificationData.getApplication().setDescription("asdasd asdkjhasdkj asdjkh\n\n\n\n\nas\n\n\ndas\n\n\ndf\n\n\nvb");
		// List<Integer> sensors = new LinkedList<Integer>();
		// sensors.add(1);
		// handleSingleNotificationData.getApplication().setSensors(sensors);
		// UserstudyNotificationManager.getInstance().updateNotification(handleSingleNotificationData);
		// try {
		// UserstudyNotificationManager.getInstance().saveToDisk(ViewUserStudyActivity.this);
		// } catch (IOException e) {
		// Log.w("MoSeS.APK", "couldnt save manager: ", e);
		// }
		// showDescisionDialog(handleSingleNotificationData);
		// return;
		// }
		final ExternalApplicationInfoRetriever infoRequester = new ExternalApplicationInfoRetriever(id, this);
		final ProgressDialog progressDialog = ProgressDialog.show(this, "Loading...", "Loading userstudy information",
				true, true, new OnCancelListener() {
					@Override
					public void onCancel(DialogInterface dialog) {

					}
				});
		infoRequester.sendEvenWhenNoNetwork = false;
		infoRequester.addObserver(new Observer() {
			@Override
			public void update(Observable observable, Object data) {
				if (infoRequester.getState() == State.DONE) {
					// TODO:
					handleSingleNotificationData.getApplication().setName(infoRequester.getResultName());
					handleSingleNotificationData.getApplication().setDescription(infoRequester.getResultDescription());
					handleSingleNotificationData.getApplication().setSensors(infoRequester.getResultSensors());
					handleSingleNotificationData.getApplication().setStartDate(infoRequester.getResultStartDate());
					handleSingleNotificationData.getApplication().setEndDate(infoRequester.getResultEndDate());
					handleSingleNotificationData.getApplication().setApkVersion(infoRequester.getResultApkVersion());
					
					UserstudyNotificationManager.getInstance().updateNotification(handleSingleNotificationData);
					try {
						UserstudyNotificationManager.getInstance().saveToDisk(ViewUserStudyActivity.this);
					} catch (IOException e) {
						Log.w("MoSeS.APK", "couldnt save manager: ", e);
					}
					progressDialog.dismiss();
					showDescisionDialog(handleSingleNotificationData);
				}
				if (infoRequester.getState() == State.ERROR) {
					Log.e("MoSeS.USERSTUDY",
							"Wanted to display user study, but couldn't get app informations because of: ",
							infoRequester.getException());
					progressDialog.dismiss();
					showMessageBoxError(infoRequester);
				}
				if (infoRequester.getState() == State.NO_NETWORK) {
					Log.d("MoSeS.USERSTUDY",
							"Wanted to display user study, but couldn't get app informations because of: ",
							infoRequester.getException());
					progressDialog.dismiss();
					showMessageBoxNoNetwork(id);
				}
			}
		});
		infoRequester.start();
	}
	
	/**
	 * Show an error message, in case something went wrong with retrieving the information for the userstudy.
	 * @param infoRequester
	 */
	protected void showMessageBoxError(ExternalApplicationInfoRetriever infoRequester) {
		new AlertDialog.Builder(ViewUserStudyActivity.this)
				.setMessage(
						"An error occured when retrieving the informations for a user study: "
								+ infoRequester.getErrorMessage()
								+ ".\nSorry! This was a shock for both of us. Maybe you could try again from the available tab later? Thanks!")
				.setTitle("Error").setCancelable(true).setNeutralButton("OK", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int whichButton) {
						cancelActivity();
					}
				}).show();
	}

	/**
	 * Show an error message in case the device doesn't have an internet connection.
	 * @param id
	 */
	protected void showMessageBoxNoNetwork(String id) {
		new AlertDialog.Builder(ViewUserStudyActivity.this)
				.setMessage(
						"Sorry, I wanted to show you the details of a user study of MoSeS. "
								+ "But it seems you have no active net connection. If you got this fixed, please select the user study again. Thanks!")
				.setTitle("No connection").setCancelable(true)
				.setNeutralButton("OK", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int whichButton) {
						cancelActivity();
					}
				}).show();
	}

	/**
	 * ?
	 * Just writes a log message.
	 * @param view
	 */
	public void dialogClickLater(View view) {
		Log.i("MoSeS.USERSTUDY", "click listener works");
	}

	/**
	 * Show the dialog were the user is asked, if he/she wants to participate 
	 * in a user study.
	 * @param notification the notification object with the user study infos
	 */
	protected void showDescisionDialog(final UserStudyNotification notification) {
	    // TODO show start date & end date & apk version
		Log.i("MoSeS.USERSTUDY", notification.getApplication().getID());
		final Dialog myDialog = new Dialog(ViewUserStudyActivity.this);
		myDialog.setContentView(R.layout.userstudynotificationdialog);
		myDialog.setTitle("A new user study is available for you");
		((TextView) myDialog.findViewById(R.id.userstudydialog_name)).setText("Name: "
				+ notification.getApplication().getName());
		((TextView) myDialog.findViewById(R.id.userstudydialog_descr)).setText(""
				+ notification.getApplication().getDescription());

		final String sensorsNeutralDescr = "Used sensors: ";
		((TextView) myDialog.findViewById(R.id.sensors_descr)).setText(sensorsNeutralDescr);
		List<Integer> sensors = notification.getApplication().getSensors();
		Gallery g = (Gallery) myDialog.findViewById(R.id.sensors);
		Integer[] imageIds = new Integer[sensors.size()];
		String[] alternateText = new String[sensors.size()];
		for (int i = 0; i < sensors.size(); ++i) {
			imageIds[i] = ESensor.values()[sensors.get(i)].imageID();
			alternateText[i] = ESensor.values()[sensors.get(i)].toString();
		}
		g.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				((TextView) myDialog.findViewById(R.id.sensors_descr)).setText(sensorsNeutralDescr
						+ ((ImageView) arg1).getContentDescription());
			}

		});
		g.setAdapter(new ImageAdapter(ViewUserStudyActivity.this, imageIds, alternateText));

		OnClickListener clickListenerYes = new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Log.i("MoSeS.USERSTUDY", "starting download process...");
				downloadUserstudyApp(notification);
				myDialog.dismiss();
			}
		};
		View.OnClickListener clickListenerNo = new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				notification.setStatus(Status.DENIED);
				UserstudyNotificationManager.getInstance().updateNotification(notification);
				UserstudyNotificationManager.getInstance().removeNotificationWithApkId(notification.getApplication().getID());

				myDialog.dismiss();
				cancelActivity();
			}
		};
		View.OnClickListener clickListenerLater = new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				myDialog.dismiss();
				cancelActivity();
			}
		};
		((Button) myDialog.findViewById(R.id.userstudydialog_btn_yay)).setOnClickListener(clickListenerYes);
		((Button) myDialog.findViewById(R.id.userstudydialog_btn_nay)).setOnClickListener(clickListenerNo);
		((Button) myDialog.findViewById(R.id.userstudydialog_btn_later)).setOnClickListener(clickListenerLater);

		myDialog.setOwnerActivity(ViewUserStudyActivity.this);
		myDialog.show();

	}

	/**
	 * Initiates the download of the APK for a user study and show the progress
	 * while downloading.
	 * @param notification the notification object with the user study infos
	 */
	protected void downloadUserstudyApp(final UserStudyNotification notification) {
		final ProgressDialog progressDialog = new ProgressDialog(this);
		final ApkDownloadManager downloader = new ApkDownloadManager(notification.getApplication(),
				getApplicationContext(), new ExecutableForObject() {

					@Override
					public void execute(Object o) {
						if (o instanceof Double) {
							progressDialog.setProgress((int) (((Double) o) * 100));
						}
					}
				});
		progressDialog.setTitle("Downloading the app...");
		progressDialog.setMessage("Please wait.");
		progressDialog.setMax(100);
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
		progressDialog.show();
			/*
			 * Observer Design Pattern
			 */
		Observer observer = new Observer() {
			@Override
			public void update(Observable observable, Object data) {
				if (downloader.getState() == ApkDownloadManager.State.ERROR_NO_CONNECTION) {
					progressDialog.dismiss();
					showMessageBoxErrorNoConnection(downloader);
				} else if (downloader.getState() == ApkDownloadManager.State.ERROR) {
					progressDialog.dismiss();
					showMessageBoxErrorDownloading(downloader);
				} else if (downloader.getState() == ApkDownloadManager.State.FINISHED) {
					progressDialog.dismiss();
					installDownloadedApk(downloader.getDownloadedApk(), downloader.getExternalApplicationResult(),
							notification);
				}
			}
		};
		downloader.addObserver(observer);
		downloader.start();
	}
	
	/**
	 * Shows an error message for a missing internet connection while downloading
	 * the APK.
	 * @param downloader
	 */
	protected void showMessageBoxErrorNoConnection(ApkDownloadManager downloader) {
		new AlertDialog.Builder(ViewUserStudyActivity.this)
				.setMessage("There seems to be no open internet connection present for downloading the app.")
				.setTitle("No connection").setCancelable(true)
				.setNeutralButton("OK", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int whichButton) {
						cancelActivity();
					}
				}).show();
	}

	/**
	 * Shows an error message if something while downloading the APK went wrong.
	 * @param downloader
	 */
	protected void showMessageBoxErrorDownloading(ApkDownloadManager downloader) {
		showMessageBoxError("Error", "An error occured when trying to download the app: " + downloader.getErrorMsg()
				+ ".\nSorry!", cancelActivityOnClickListener());
	}

	/**
	 * General method for showing an error message. Used by the other 
	 * showMessageBoxError... methods.
	 * @param title the title for the dialog
	 * @param msg the message for the dialog
	 * @param onClickListener onClickListener for the button in the message
	 */
	protected void showMessageBoxError(String title, String msg, DialogInterface.OnClickListener onClickListener) {
		new AlertDialog.Builder(ViewUserStudyActivity.this).setMessage(msg).setTitle(title).setCancelable(true)
				.setNeutralButton("OK", onClickListener).show();
	}

	/**
	 * Cancels the activity.
	 * @return
	 */
	private DialogInterface.OnClickListener cancelActivityOnClickListener() {
		return new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int whichButton) {
				cancelActivity();
			}
		};

	}

	/**
	 * Initializes the installation of a downloaded APK.
	 * @param result the APK
	 * @param externalAppRef reference to the {@link ExternalApplication}
	 * @param notification the corresponding user study notification
	 */
	private void installDownloadedApk(final File result, final ExternalApplication externalAppRef,
			final UserStudyNotification notification) {
		final ApkInstallManager installer = new ApkInstallManager(result, externalAppRef);
		installer.addObserver(new Observer() {
			@Override
			public void update(Observable observable, Object data) {
				if (installer.getState() == ApkInstallManager.State.ERROR) {
					showMessageBoxError("Error", "An error occured when installing the user study app. Sorry!",
							cancelActivityOnClickListener());
				} else if (installer.getState() == ApkInstallManager.State.INSTALLATION_CANCELLED) {
					cancelActivity();
				} else if (installer.getState() == ApkInstallManager.State.INSTALLATION_COMPLETED) {
					new APKInstalled(externalAppRef.getID());
					notification.setStatus(Status.ACCEPTED);
					UserstudyNotificationManager.getInstance().updateNotification(notification);
					try {
						ApkInstallManager.registerInstalledApk(result, externalAppRef,
								ViewUserStudyActivity.this.getApplicationContext(), true);
						UserstudyNotificationManager.getInstance().removeNotificationWithApkId(externalAppRef.getID());
						UserstudyNotificationManager.getInstance().saveToDisk(getApplicationContext());
					} catch (IOException e) {
						Log.e("MoSeS.Install",
								"Problems with extracting package name from apk," +
								" or problems with the InstalledExternalApplicationsManager " +
								"after installing an app");
					}
					finishActivityOK();
				}
			}
		});
		installer.start();
	}

	/**
	 * Concatenate an exception to the stack trace.
	 * @param e the exception to add to the stack trace
	 * @return the stack trace
	 */
	public static String concatStacktrace(Exception e) {
		String stackTrace = "";
		for (int i = 0; i < e.getStackTrace().length; i++) {
			stackTrace += e.getStackTrace()[i];
		}
		return stackTrace;
	}

}
