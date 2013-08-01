package de.da_sense.moses.client.service.helpers;

import java.io.IOException;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Service;
import android.content.Context;
import android.os.AsyncTask;
import android.preference.PreferenceManager;

import com.google.android.gms.gcm.GoogleCloudMessaging;

import de.da_sense.moses.client.R;
import de.da_sense.moses.client.abstraction.HardwareAbstraction;
import de.da_sense.moses.client.com.NetworkJSON.BackgroundException;
import de.da_sense.moses.client.com.ReqTaskExecutor;
import de.da_sense.moses.client.com.requests.RequestC2DM;
import de.da_sense.moses.client.preferences.MosesPreferences;
import de.da_sense.moses.client.service.MosesService;
import de.da_sense.moses.client.util.Log;

/**
 * 
 * This Class contains information and methods concerning the
 * connection with GCM Services: registering and obtaining an ID from googles server AND sending the ID to MoSeS server.
 * 
 * @author Wladimir Schmidt
 * @author Zijad Maksuti
 * 
 */
public class C2DMManager {
	
	private static String LOG_TAG = C2DMManager.class.getName();

	/**
	 * Contains the current registration ID for C2DM Services
	 */
	private static String c2dmRegistrationId;

	
	/**
	 * Starts 
	 * @param baseService
	 */
	public static void requestC2DMId(Service baseService) {
		AsyncObtainGCMID task = new AsyncObtainGCMID();
		task.execute(baseService);
	}

	/**
	 * Sets the RegistrationID to the given ID
	 * @param registrationId The ID to set to
	 */
	public static void setC2DMReceiverId(String registrationId) {
		Log.i(LOG_TAG, "setC2DMReceiverId() received GCM ID " + registrationId);
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
					.putString(MosesPreferences.PREF_GCM_ID, registrationId).commit();
			sendC2DMIdToServer(registrationId, MosesService.getInstance());
		}
	}

	/**
	 * Sends the current C2DM Registration to the server.
	 */
	public static void sendCurrentC2DM() {
		sendC2DMIdToServer(
				PreferenceManager.getDefaultSharedPreferences(MosesService.getInstance()).getString(MosesPreferences.PREF_GCM_ID, ""),
				MosesService.getInstance());
	}

	/**
	 * TODO
	 * @param registrationId
	 * @param context
	 */
	private static void sendC2DMIdToServer(final String registrationId, final Context context) {
		MosesService.getInstance().executeLoggedIn(HookTypesEnum.POST_LOGIN_SUCCESS, MessageTypesEnum.REQUEST_C2DM,
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
										Log.i(LOG_TAG, "synchronized GCM ID with moses server.");
									} else {
										Log.w(LOG_TAG, "C2DM request returned NEGATIVE response: " + s);
									}

								} catch (JSONException e) {
									Log.e(LOG_TAG, "C2DMToMosesServer returned malformed message");
									e.printStackTrace();
								}
							}

							@Override
							public void handleException(Exception e) {
								// TODO: make sure that the id is sent to the server after this failure
								Log.e(LOG_TAG, "sendC2DM failed: " + e.getMessage(), e);
							}
						}, MosesService.getInstance().getSessionID(), HardwareAbstraction.extractDeviceIdFromSharedPreferences(),
								registrationId);

						request.send();
					}
				});
	}


	/**
	 * This task obtains an ID for reciving of GCM push messages. After the id has been obtained, the task
	 * subsequently invokes {@link C2DMManager#setC2DMReceiverId(String)} 
	 * 
	 * @author Zijad Maksuti
	 * 
	 */
	private static class AsyncObtainGCMID extends AsyncTask<Context, Void, Void> {
		
		protected Void doInBackground(Context... params) {
			Context context = params[0];
			GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(context);
			try {
				setC2DMReceiverId(gcm.register(context.getString(R.string.GoogleProjectNumber)));
			} catch (IOException e) {
				Log.w(LOG_TAG, "AsyncObtainGCMID: problem registering for GCM. ID is not obtained");
				e.printStackTrace();
			}
			return null;
		}

	}
}
