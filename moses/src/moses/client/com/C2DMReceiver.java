package moses.client.com;

import moses.client.service.MosesService;
import moses.client.service.MosesService.LocalBinder;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

public class C2DMReceiver extends BroadcastReceiver {

	public static final String EXTRAFIELD_USERSTUDY_NOTIFICATION = "UserStudyNotification";
	private static final String C2DN_MESSAGETYPE_FIELD = "MESSAGE";
	private static final String C2DN_USERSTUDY_APKID_FIELD = "APKID";
	public static final String EXTRAFIELD_C2DM_ID = "c2dmId";
	private boolean isRegistered;

	@Override
	public void onReceive(Context context, Intent intent) {
		if (intent.getAction().equals("com.google.android.c2dm.intent.REGISTRATION")) {
			handleRegistration(context, intent);
		} else if (intent.getAction().equals("com.google.android.c2dm.intent.RECEIVE")) {
			handleMessage(context, intent);
		}
	}

	private void handleMessage(Context context, Intent intent) {
		String messagetype = intent.getExtras().getString(C2DN_MESSAGETYPE_FIELD);
		String apkidString = intent.getExtras().getString(C2DN_USERSTUDY_APKID_FIELD);
		boolean receivedGoodThing = false;
		if(messagetype != null) {
			if(apkidString != null) {
				Log.i("MoSeS.C2DM", "User study notification received!! APK ID = " + apkidString);
				receivedGoodThing = true;
			} else {
				Log.i("MoSeS.C2DM", "User study notification received but bad apkid (null)");
			}
		} else {
			Log.i("MoSeS.C2DM", "Notification received but bad MESSAGE String (null)");
		}
		
		if(receivedGoodThing) {
			Intent mosesServiceStartIntent = new Intent(context, MosesService.class);
			mosesServiceStartIntent.putExtra(EXTRAFIELD_USERSTUDY_NOTIFICATION, apkidString);
			// this directs to "onStartCommand(Intent, int, int)" in the service
			context.startService(mosesServiceStartIntent);
		}
	}

	private void handleRegistration(Context context, Intent intent) {
		String registrationId = intent.getStringExtra("registration_id");
		if (intent.getStringExtra("error") != null) {
			// TODO: handle errors
			// mögliche error-codes:
			// SERVICE_NOT_AVAILABLE The device can't read the response, or
			// there was a 500/503 from the server that can be retried later.
			// The application should use exponential back off and retry.
			// ACCOUNT_MISSING There is no Google account on the phone. The
			// application should ask the user to open the account manager and
			// add a Google account. Fix on the device side.
			// AUTHENTICATION_FAILED Bad password. The application should ask
			// the user to enter his/her password, and let user retry manually
			// later. Fix on the device side.
			// TOO_MANY_REGISTRATIONS The user has too many applications
			// registered. The application should tell the user to uninstall
			// some other applications, let user retry manually. Fix on the
			// device side.
			// INVALID_SENDER The sender account is not recognized.
			// PHONE_REGISTRATION_ERROR
		} else if (intent.getStringExtra("unregistered") != null) {
			// unregistration done
		} else if (registrationId != null) {
			isRegistered = true;
			sendRegisteredMsgToService(registrationId, context);
		}
	}

	private void sendRegisteredMsgToService(final String registrationId, Context context) {
		Intent intent = new Intent(context, MosesService.class);
		intent.putExtra(EXTRAFIELD_C2DM_ID, registrationId);
		// this directs to "onStartCommand(Intent, int, int)" in the service
		context.startService(intent);
	}
}
