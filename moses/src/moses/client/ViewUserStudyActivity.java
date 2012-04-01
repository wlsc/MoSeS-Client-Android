package moses.client;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;
import java.util.Queue;

import moses.client.abstraction.ExternalApplicationInfoRetriever;
import moses.client.abstraction.ExternalApplicationInfoRetriever.State;
import moses.client.abstraction.apks.APKInstalled;
import moses.client.abstraction.apks.ApkDownloadManager;
import moses.client.abstraction.apks.ApkInstallManager;
import moses.client.abstraction.apks.ExternalApplication;
import moses.client.com.NetworkJSON.BackgroundException;
import moses.client.com.ReqTaskExecutor;
import moses.client.com.requests.RequestGetApkInfo;
import moses.client.service.MosesService;
import moses.client.service.helpers.EHookTypes;
import moses.client.service.helpers.EMessageTypes;
import moses.client.service.helpers.Executor;
import moses.client.userstudy.UserStudyNotification;
import moses.client.userstudy.UserStudyNotification.Status;
import moses.client.userstudy.UserstudyNotificationManager;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Viewing and installing apks from the server
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
	 * (non-Javadoc)
	 * 
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

			if (!MosesActivity.isLoginInformationComplete(this)) {
				// if either username or password are not set, redirect to moses
				// ui for login

				Intent intent = new Intent(this, MosesActivity.class);
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

	private void showActivityForNotification(UserStudyNotification notification) {
		if (notification != null) {
			this.handleSingleNotificationData = notification;
			requestApkInfo(notification.getApplication().getID());
		} else {
			Log.e("MoSeS.USERSTUDY", "aborting userstudy operation; no data");
			cancelActivity();
		}
	}

	/**
	 * sets result:cancelled and finishes.
	 */
	private void cancelActivity() {
		setResult(Activity.RESULT_CANCELED);
		finish();
	}

	protected void finishActivityOK() {
		setResult(Activity.RESULT_OK);
		finish();
	}

	private void requestApkInfo(final String id) {
		final ExternalApplicationInfoRetriever infoRequester = new ExternalApplicationInfoRetriever(id, this);
		final ProgressDialog progressDialog = ProgressDialog.show(this, "Loading...", "Loading userstudy information", true, true, new OnCancelListener() {
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

	protected void showMessageBoxError(ExternalApplicationInfoRetriever infoRequester) {
		AlertDialog alertDialog = new AlertDialog.Builder(ViewUserStudyActivity.this)
				.setMessage(
						"An error occured when retrieving the informations for a user study: " + infoRequester.getErrorMessage()
						+".\nSorry! This was a shock for both of us. Maybe you could try again from the user study tab later? Thanks!")
				.setTitle("Error").setCancelable(true)
				.setNeutralButton("OK", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						cancelActivity();
					}
				}).show();
	}

	protected void showMessageBoxNoNetwork(String id) {
		AlertDialog alertDialog = new AlertDialog.Builder(ViewUserStudyActivity.this)
				.setMessage(
						"Sorry, I wanted to show you the details of a user study of MoSeS. "
								+ "But it seems you have no active net connection. If you got this fixed, please select the user study again from the user study tab (the 3rd one). Thanks!")
				.setTitle("No connection").setCancelable(true)
				.setNeutralButton("OK", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						cancelActivity();
					}
				}).show();
	}

	public void dialogClickLater(View view) {
		Log.i("MoSeS.USERSTUDY", "click listener works");
	}

	protected void showDescisionDialog(final UserStudyNotification notification) {
		Log.i("MoSeS.USERSTUDY", notification.getApplication().getID());
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				final Dialog myDialog = new Dialog(ViewUserStudyActivity.this);
				myDialog.setContentView(R.layout.userstudynotificationdialog);
				myDialog.setTitle("A new user study \"" + notification.getApplication().getName()
						+ "\" is available for you");
				((TextView) myDialog.findViewById(R.id.userstudydialog_name)).setText("Name: "
						+ notification.getApplication().getName());
				((TextView) myDialog.findViewById(R.id.userstudydialog_descr)).setText(""
						+ notification.getApplication().getDescription());
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
						UserstudyNotificationManager.getInstance().removeNotificationWithApkId(
								notification.getApplication().getID());

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
		});

	}

	protected void downloadUserstudyApp(final UserStudyNotification notification) {
		final ApkDownloadManager downloader = new ApkDownloadManager(notification.getApplication(),
				getApplicationContext());
		Observer observer = new Observer() {
			@Override
			public void update(Observable observable, Object data) {
				if (downloader.getState() == ApkDownloadManager.State.ERROR) {
					cancelActivity();
				} else if (downloader.getState() == ApkDownloadManager.State.FINISHED) {
					installDownloadedApk(downloader.getDownloadedApk(), downloader.getExternalApplicationResult(),
							notification);
				}
			}
		};
		downloader.addObserver(observer);
		downloader.start();
	}

	private void installDownloadedApk(final File result, final ExternalApplication externalAppRef,
			final UserStudyNotification notification) {
		final ApkInstallManager installer = new ApkInstallManager(result, externalAppRef);
		installer.addObserver(new Observer() {
			@Override
			public void update(Observable observable, Object data) {
				if (installer.getState() == ApkInstallManager.State.ERROR) {
					cancelActivity();
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
								"Problems with extracting package name from apk, or problems with the InstalledExternalApplicationsManager after installing an app");
					}
					finishActivityOK();
				}
			}
		});
		installer.start();
	}

	public static String concatStacktrace(Exception e) {
		String stackTrace = "";
		for (int i = 0; i < e.getStackTrace().length; i++) {
			stackTrace += e.getStackTrace()[i];
		}
		return stackTrace;
	}

}
