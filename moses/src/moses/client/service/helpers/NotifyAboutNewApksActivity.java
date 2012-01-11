package moses.client.service.helpers;

import moses.client.R;
import moses.client.ViewAvailableApkActivity;
import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

public class NotifyAboutNewApksActivity extends Activity {

	public static NotifyAboutNewApksActivity staticActivityReference;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		showNotification();
		finish();
	}

	protected void showNotification() {
		showNotification("New sensing applications are available!\nClick here to view all applications", "MoSeS",
			false, CheckForNewApplications.class.getCanonicalName().hashCode());
	}

	private void showNotification(String text, String title, boolean ongoing, int id) {

		Notification notification = new Notification(R.drawable.ic_launcher, text, System.currentTimeMillis());
		if (ongoing) {
			notification.flags = Notification.FLAG_ONGOING_EVENT;
		} else {
			notification.flags = Notification.FLAG_AUTO_CANCEL;
		}
		PendingIntent contentIntent = PendingIntent.getActivity(getContext(), 0, new Intent(getContext(),
			ViewAvailableApkActivity.class), PendingIntent.FLAG_UPDATE_CURRENT);

		notification.setLatestEventInfo(getContext(), title, text, contentIntent);
		NotificationManager mNotificationManager = (NotificationManager) getActivity().getSystemService(
			Context.NOTIFICATION_SERVICE);
		mNotificationManager.notify(id, notification);

	}

	private Activity getActivity() {
		return this;
	}

	private Context getContext() {
		return getActivity().getApplicationContext();
	}

}
