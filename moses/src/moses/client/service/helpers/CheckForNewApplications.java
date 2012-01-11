package moses.client.service.helpers;

import java.util.List;

import moses.client.abstraction.ApkListRequestObserver;
import moses.client.abstraction.ApkMethods;
import moses.client.abstraction.apks.ExternalApplication;
import android.app.Activity;
import android.app.Service;
import android.content.Intent;
import android.os.Handler;

/**
 * @author Simon L
 * 
 */
public class CheckForNewApplications implements ApkListRequestObserver {
	private static boolean debug = true; // shows the notification for 1 time
											// ca. 20seconds after the start.
	private static int counterDebug = 0;
	private Handler mHandler = new Handler();
	private boolean stopChecking = false;

	List<ExternalApplication> lastApplicationList;

	private final int checkInterval = 1000 * 60 * 60; // every hour
	private final int checkIntervalNotInitialized = 1000 * 10;// debugmode:
																// every 10
																// seconds --
																// after 20
																// seconds, the
																// first
																// motification
																// should show
																// //every
																// minute

	private Runnable checkTask = new Runnable() {
		@Override
		public void run() {
			checkForNewApplications();

			int nextInterval = isInitialized() ? checkInterval : checkIntervalNotInitialized;

			// pinger.sendPing();
			if (!stopChecking) mHandler.postDelayed(this, nextInterval);
		}

	};
	private Service context;

	public CheckForNewApplications(Service baseContext) {
		this.context = baseContext;
		lastApplicationList = null;
	}

	protected void checkForNewApplications() {
		ApkMethods.getExternalApplications(this);
	}

	protected boolean isInitialized() {
		return lastApplicationList != null;
	}

	public void startChecking(boolean b) {
		if (b) {
			mHandler.removeCallbacks(checkTask);
			mHandler.postDelayed(checkTask, checkIntervalNotInitialized);
		} else {
			stopChecking = true;
		}
	}

	@Override
	public void apkListRequestFinished(List<ExternalApplication> result) {
		if (lastApplicationList != null) {
			// only check, if this is not the first list received by this object
			// (would be not meaningful)
			if (newApplicationsInDiff(result, lastApplicationList) || true) {
				showNotification();
			}
		}
		lastApplicationList = result;
	}

	private void showNotification() {
		Intent intent = new Intent(context, NotifyAboutNewApksActivity.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		context.startActivity(intent);
	}

	private static boolean newApplicationsInDiff(List<ExternalApplication> newApplicationList,
		List<ExternalApplication> lastApplicationList) {
		if (debug && (counterDebug < 2)) {
			counterDebug++;
			return true;
		}
		for (ExternalApplication newerApp : newApplicationList) {
			boolean found = false;
			for (ExternalApplication olderApp : lastApplicationList) {
				if (newerApp.equals(olderApp)) {
					found = true;
				}
			}
			if (!found) { return true; }
		}
		return false;
	}

	@Override
	public void apkListRequestFailed(Exception e) {
		// if the request failed, just wait for the next one
		// TODO: could be handled differently in the future
	}
}
