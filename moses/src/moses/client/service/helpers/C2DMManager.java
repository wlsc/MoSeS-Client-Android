package moses.client.service.helpers;

import moses.client.abstraction.HardwareAbstraction;
import moses.client.com.ReqTaskExecutor;
import moses.client.com.NetworkJSON.BackgroundException;
import moses.client.com.requests.RequestC2DM;
import moses.client.service.MosesService;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

public class C2DMManager {

	private static final String MOSES_TUD_GOOGLEMAIL_COM = "moses.tud@googlemail.com";

	private static String c2dmRegistrationId;

	public static void requestC2DMId(Service baseService) {
		Intent registrationIntent = new Intent("com.google.android.c2dm.intent.REGISTER");
		registrationIntent.putExtra("app", PendingIntent.getBroadcast(baseService, 0, new Intent(), 0)); // boilerplate
		registrationIntent.putExtra("sender", MOSES_TUD_GOOGLEMAIL_COM);
		baseService.startService(registrationIntent);
	}

	public static void setC2DMReceiverId(String registrationId) {
		// TODO: if the c2dm id changed, resend?
		Log.i("MoSeS.C2DM", "received C2DM " + registrationId);
		boolean setNewC2DMID = false;
		if (registrationId != null) {
			if (c2dmRegistrationId == null) {
				setNewC2DMID = true;
			} else {
				if (!registrationId.equals(c2dmRegistrationId)) {
					setNewC2DMID = true;
				}
			}
		} else {
			// TODO: unexpected error.. registrationId == null (should never
			// occur thou)
		}
		if (setNewC2DMID) {
			c2dmRegistrationId = registrationId;
			PreferenceManager.getDefaultSharedPreferences(MosesService.getInstance()).edit().putString("c2dm_pref", registrationId).commit();
			sendC2DMIdToServer(registrationId, MosesService.getInstance());
		}
	}

	public static void sendCurrentC2DM() {
		sendC2DMIdToServer(PreferenceManager.getDefaultSharedPreferences(MosesService.getInstance()).getString("c2dm_pref", ""),MosesService.getInstance());
	}

	// TODO: make very sure that the id is really sent to the server!
	// TODO: what if session id is yet unknown?
	private static void sendC2DMIdToServer(final String registrationId, final Context context) {
		MosesService.getInstance().executeLoggedIn(new Executor() {
			@Override
			public void execute() {
				final RequestC2DM request = new RequestC2DM(new ReqTaskExecutor() {
					@Override
					public void updateExecution(BackgroundException c) {
					}

					@Override
					public void postExecution(String s) {
						// request sent!
						try {
							JSONObject j = new JSONObject(s);
							if (RequestC2DM.C2DMRequestAccepted(j)) {
								Toast.makeText(context, "C2DM send request returned POSITIVE", Toast.LENGTH_LONG)
									.show();
								Log.i("MoSeS.C2DM", "synchronized c2dm id with moses server.");
							} else {
								Toast.makeText(context, "C2DM send request returned NEGATIVE", Toast.LENGTH_LONG)
									.show();
								Log.w("MoSeS.C2DM", "C2DM request returned NEGATIVE response: " + s);
							}

						} catch (JSONException e) {
							Toast.makeText(context, "C2DMToMosesServer returned malformed message", Toast.LENGTH_LONG)
								.show();
							Log.e("MoSeS.C2DM", "C2DMToMosesServer returned malformed message");
						}
					}

					@Override
					public void handleException(Exception e) {
						// TODO: make very sure that the id is really sent to
						// the server!
						Toast.makeText(context, "sendC2DM failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
					}
				}, MosesService.getInstance().getSessionID(), HardwareAbstraction.extractDeviceId(), registrationId);

				request.send();
			}
		});
	}

}
