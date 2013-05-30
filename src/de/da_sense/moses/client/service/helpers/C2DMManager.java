package de.da_sense.moses.client.service.helpers;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.preference.PreferenceManager;
import de.da_sense.moses.client.abstraction.HardwareAbstraction;
import de.da_sense.moses.client.com.NetworkJSON.BackgroundException;
import de.da_sense.moses.client.com.ReqTaskExecutor;
import de.da_sense.moses.client.com.requests.RequestC2DM;
import de.da_sense.moses.client.service.MosesService;
import de.da_sense.moses.client.util.Log;

/**
 * 
 * This Class contains information and methods concerning the
 * connection with Google's Cloud 2 Device Messaging Services.
 * It does not contain C2DM functionality, it only establishes
 * and controls the connection, but does not handle C2DM.
 *
 */
public class C2DMManager {

	/**
	 * MoSeS Googlemail Account for Cloud 2 Device Messaging Services
	 */
	private static final String MOSES_TUD_GOOGLEMAIL_COM = "moses.tud@googlemail.com";

	/**
	 * Contains the current registration ID for C2DM Services
	 */
	private static String c2dmRegistrationId;

	
	/**
	 * TODO
	 * @param baseService
	 */
	public static void requestC2DMId(Service baseService) {
		Intent registrationIntent = new Intent("com.google.android.c2dm.intent.REGISTER");
		registrationIntent.putExtra("app", PendingIntent.getBroadcast(baseService, 0, new Intent(), 0)); // boilerplate
		registrationIntent.putExtra("sender", MOSES_TUD_GOOGLEMAIL_COM);
		baseService.startService(registrationIntent);
	}

	/**
	 * Sets the RegistrationID to the given ID
	 * @param registrationId The ID to set to
	 */
	public static void setC2DMReceiverId(String registrationId) {
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
			Log.w("MoSeS.C2DM", "received c2dm id is null");
		}
		if (setNewC2DMID) {
			c2dmRegistrationId = registrationId;
			PreferenceManager.getDefaultSharedPreferences(MosesService.getInstance()).edit()
					.putString("c2dm_pref", registrationId).commit();
			sendC2DMIdToServer(registrationId, MosesService.getInstance());
		}
	}

	/**
	 * Sends the current C2DM Registration to the server.
	 */
	public static void sendCurrentC2DM() {
		sendC2DMIdToServer(
				PreferenceManager.getDefaultSharedPreferences(MosesService.getInstance()).getString("c2dm_pref", ""),
				MosesService.getInstance());
	}

	/**
	 * TODO
	 * @param registrationId
	 * @param context
	 */
	private static void sendC2DMIdToServer(final String registrationId, final Context context) {
		MosesService.getInstance().executeLoggedIn(EHookTypes.POSTLOGINSUCCESS, EMessageTypes.REQUESTC2DM,
				new Executable() {
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
										Log.i("MoSeS.C2DM", "synchronized c2dm id with moses server.");
									} else {
										Log.w("MoSeS.C2DM", "C2DM request returned NEGATIVE response: " + s);
									}

								} catch (JSONException e) {
									Log.e("MoSeS.C2DM", "C2DMToMosesServer returned malformed message");
								}
							}

							@Override
							public void handleException(Exception e) {
								// TODO: make very sure that the id is really
								// sent to
								// the server!
								Log.e("MoSeS.C2DM", "sendC2DM failed: " + e.getMessage(), e);
							}
						}, MosesService.getInstance().getSessionID(), HardwareAbstraction.extractDeviceId(),
								registrationId);

						request.send();
					}
				});
	}

}
