package moses.client.com;

import moses.client.service.MosesService;
import moses.client.service.MosesService.LocalBinder;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.widget.Toast;

public class C2DMReceiver extends BroadcastReceiver {

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
		String msgtype = intent.getExtras().getString(C2DMConfig.FIELD_MSGTYPE);
	    String msg = intent.getExtras().getString(C2DMConfig.FIELD_MSG);
	    if(msgtype.equals(C2DMConfig.MSGTYPE_TOAST)) {
	    	Toast.makeText(context, "Message received: " + msg, Toast.LENGTH_LONG);
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
//		ServiceConnection mConnection = new ServiceConnection() {
//			@Override
//			public void onServiceConnected(ComponentName className, IBinder service) {
//				// We've bound to LocalService, cast the IBinder and get
//				// LocalService instance
//				LocalBinder binder = (LocalBinder) service;
//				MosesService mService = binder.getService();
//				//notify service about registration id
//				mService.setC2DMReceiverId(registrationId);
//			}
//			@Override
//			public void onServiceDisconnected(ComponentName arg0) {
//			}
//		};
//		// Connect to the service
//		Intent intent = new Intent(context, MosesService.class);
//		context.bindService(intent, mConnection, 0);
	}
}
