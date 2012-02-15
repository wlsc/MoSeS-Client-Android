/**
 * 
 */
package moses.client.abstraction;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import moses.client.R;
import moses.client.com.ConnectionParam;
import moses.client.com.NetworkJSON.BackgroundException;
import moses.client.com.ReqTaskExecutor;
import moses.client.com.requests.RequestGetFilter;
import moses.client.com.requests.RequestGetHardwareParameters;
import moses.client.com.requests.RequestLogin;
import moses.client.com.requests.RequestSetFilter;
import moses.client.com.requests.RequestSetHardwareParameters;
import moses.client.service.MosesService;
import moses.client.service.MosesService.MosesSettings;
import moses.client.service.helpers.Executor;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Build;
import android.preference.PreferenceManager;
import android.util.Log;

/**
 * This class provides basic support for hardware sync with server
 * 
 * @author Jaco Hofmann
 * 
 */
public class HardwareAbstraction {

	public static class HardwareInfo {
		private String deviceID;
		private String sdkbuildversion;
		private String vendor;
		private String model;
		private List<Integer> sensors;

		public HardwareInfo(String deviceID, String vendor, String model,
				String sdkbuildversion, List<Integer> sensors) {
			super();
			this.deviceID = deviceID;
			this.sdkbuildversion = sdkbuildversion;
			this.sensors = sensors;
			this.vendor = vendor;
			this.model = model;
		}

		public String getDeviceVendor() {
			return vendor;
		}

		public String getDeviceModel() {
			return model;
		}

		public String getDeviceID() {
			return deviceID;
		}

		public String getSdkbuildversion() {
			return sdkbuildversion;
		}

		public List<Integer> getSensors() {
			return sensors;
		}
	}

	private class ReqClassGetFilter implements ReqTaskExecutor {

		@Override
		public void handleException(Exception e) {
			Log.d("MoSeS.HARDWARE_ABSTRACTION", "FAILURE: " + e.getMessage());
		}

		@Override
		public void postExecution(String s) {
			JSONObject j = null;
			try {
				j = new JSONObject(s);
				if (RequestGetFilter.parameterAcquiredFromServer(j)) {
					JSONArray filter = j.getJSONArray("FILTER");
					if (MosesService.getInstance() != null)
						MosesService.getInstance().setFilter(filter);
				} else {
					Log.d("MoSeS.HARDWARE_ABSTRACTION",
							"Parameters NOT retrived successfully! Server returned negative response");
				}
			} catch (JSONException e) {
				this.handleException(e);
			}
		}

		@Override
		public void updateExecution(BackgroundException c) {
			if (c.c == ConnectionParam.EXCEPTION) {
				handleException(c.e);
			}
		}
	}

	/**
	 * Get all available sensors from the operating system.
	 * 
	 * @return All available sensors on this device
	 */
	public static List<Sensor> getSensors() {
		if (MosesService.getInstance() != null) {
			List<Sensor> sensors = new ArrayList<Sensor>();
			SensorManager s = (SensorManager) MosesService.getInstance()
					.getSystemService(Context.SENSOR_SERVICE);
			for (Sensor sen : s.getSensorList(Sensor.TYPE_ALL))
				sensors.add(sen);

			return sensors;
		} else {
			return null;
		}
	}

	private class ReqClassGetHWParams implements ReqTaskExecutor {

		@Override
		public void handleException(Exception e) {
			Log.d("MoSeS.HARDWARE_ABSTRACTION", "FAILURE: " + e.getMessage());
		}

		@Override
		public void postExecution(String s) {
			JSONObject j = null;
			try {
				j = new JSONObject(s);
				if (RequestGetHardwareParameters.parameterAcquiredFromServer(j)) {
					StringBuffer sb = new StringBuffer(256);
					sb.append("Parameters retrived successfully from server");
					sb.append("\n").append("Device id:")
							.append(j.get("DEVICEID"));
					sb.append("\n").append("Android version:")
							.append(j.get("ANDVER"));
					JSONArray sensors = j.getJSONArray("SENSORS");
					sb.append("\n").append("SENSORS:").append("\n");
					for (int i = 0; i < sensors.length(); i++) {
						sb.append("\n");
						sb.append(ESensor.values()[sensors.getInt(i)]);
					}
					Log.d("MoSeS.HARDWARE_ABSTRACTION", sb.toString());
					AlertDialog ad = new AlertDialog.Builder(appContext)
							.create();
					ad.setCancelable(false); // This blocks the 'BACK' button
					ad.setMessage(sb.toString());
					ad.setButton("OK", new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							dialog.dismiss();
						}
					});
					ad.show();
				} else {
					Log.d("MoSeS.HARDWARE_ABSTRACTION",
							"Parameters NOT retrived successfully from server! :(");
				}
			} catch (JSONException e) {
				this.handleException(e);
			}
		}

		@Override
		public void updateExecution(BackgroundException c) {
			if (c.c == ConnectionParam.EXCEPTION) {
				handleException(c.e);
			}
		}
	}

	private class ReqClassSetFilter implements ReqTaskExecutor {

		@Override
		public void handleException(Exception e) {
			Log.d("MoSeS.HARDWARE_ABSTRACTION",
					"FAILURE SETTING FILTER: " + e.getMessage());
		}

		@Override
		public void postExecution(String s) {
			JSONObject j = null;
			try {
				j = new JSONObject(s);
				// TODO handling
				if (RequestSetFilter.filterSetOnServer(j)) {
					Log.d("MoSeS.HARDWARE_ABSTRACTION",
							"Filter set successfully, server returned positive response");
				} else {
					Log.d("MoSeS.HARDWARE_ABSTRACTION",
							"Filter NOT set successfully! Server returned negative response");
				}
			} catch (JSONException e) {
				this.handleException(e);
			}
		}

		@Override
		public void updateExecution(BackgroundException c) {
			if (c.c == ConnectionParam.EXCEPTION) {
				handleException(c.e);
			}
		}
	}

	private class ReqClassSetHWParams implements ReqTaskExecutor {

		@Override
		public void handleException(Exception e) {
			Log.d("MoSeS.HARDWARE_ABSTRACTION", "FAILURE: " + e.getMessage());
		}

		@Override
		public void postExecution(String s) {
			Log.d("HARDWARE_ABSTRACTION", "From server: " + s);
			JSONObject j = null;
			try {
				j = new JSONObject(s);
				if (RequestSetHardwareParameters.parameterSetOnServer(j)) {
					Log.d("MoSeS.HARDWARE_ABSTRACTION",
							"Parameters set successfully, server returned positive response");
				} else {
					if (j.getString("STATUS").equals(
							"FAILURE_DEVICEID_DUPLICATED")) {
						showForceDialog(j.getString("VENDOR_NAME"),
								j.getString("MODEL_NAME"),
								j.getString("ANDVER"));
					} else {
						Log.d("MoSeS.HARDWARE_ABSTRACTION",
								"Parameters NOT set successfully! Invalid session id.");
					}
				}
			} catch (JSONException e) {
				this.handleException(e);
			}
		}

		@Override
		public void updateExecution(BackgroundException c) {
			if (c.c == ConnectionParam.EXCEPTION) {
				handleException(c.e);
			}
		}

		public void showForceDialog(String vendor, String model, String andver) {
			if (MosesService.getInstance() != null) {
				AlertDialog a = new AlertDialog.Builder(
						MosesService.getInstance()).create();
				a.setTitle(R.string.dialog_duplicated_devid_title);
				a.setMessage(MosesService.getInstance().getString(
						R.string.dialog_duplicated_devid_text)
						+ "\nVendor: "
						+ vendor
						+ "\nModel: "
						+ model
						+ "\nSDK Version: "
						+ andver
						+ "\n"
						+ MosesService.getInstance().getString(
								R.string.dialog_duplicated_devid_text2));
				a.setButton(
						MosesService.getInstance().getString(
								R.string.dialog_duplicated_devid_update),
						new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface arg0, int arg1) {
								MosesService.getInstance()
										.syncDeviceInformation(true);
								arg0.dismiss();
							}

						});
				a.setButton2(
						MosesService.getInstance().getString(
								R.string.dialog_duplicated_devid_cancle),
						new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface arg0, int arg1) {
								arg0.dismiss();
							}

						});
				a.setIcon(R.drawable.ic_launcher);
			}
		}
	}

	private Context appContext;

	public HardwareAbstraction(Context c) {
		appContext = c;
	}

	/**
	 * This method sends a Request to the website for obtainint the filter
	 * stored for this device
	 */
	public void getFilter() {

		if (MosesService.getInstance() != null)
			MosesService.getInstance().executeLoggedIn(new Executor() {

				@Override
				public void execute() {
					final RequestGetFilter rGetFilter = new RequestGetFilter(
							new ReqClassGetFilter(), RequestLogin
									.getSessionID(), extractDeviceId());
					rGetFilter.send();
				}
			});
	}

	/**
	 * This method reads the sensor list stored for the device on the server
	 */
	public void getHardwareParameters() {
		// *** SENDING GET_HARDWARE_PARAMETERS REQUEST TO SERVER ***//
		if (MosesService.getInstance() != null)
			MosesService.getInstance().executeLoggedIn(new Executor() {

				@Override
				public void execute() {
					new RequestGetHardwareParameters(new ReqClassGetHWParams(),
							RequestLogin.getSessionID(), extractDeviceId())
							.send();
				}
			});
	}

	/**
	 * This method sends a set_filter Request to the website
	 */
	public void setFilter(final String filter) {
		// *** SENDING GET_HARDWARE_PARAMETERS REQUEST TO SERVER ***//

		if (MosesService.getInstance() != null)
			MosesService.getInstance().executeLoggedIn(new Executor() {

				@Override
				public void execute() {
					RequestSetFilter rSetFilter = new RequestSetFilter(
							new ReqClassSetFilter(), RequestLogin
									.getSessionID(), extractDeviceId(), filter);
					rSetFilter.send();
				}
			});
	}

	/**
	 * This method reads the sensors currently chosen by the user and returns
	 * them
	 */
	private HardwareInfo retrieveHardwareParameters() {
		// *** SENDING SET_HARDWARE_PARAMETERS REQUEST TO SERVER ***//

		LinkedList<Integer> sensors = new LinkedList<Integer>();
		SensorManager s = (SensorManager) appContext
				.getSystemService(Context.SENSOR_SERVICE);
		for (Sensor sen : s.getSensorList(Sensor.TYPE_ALL)) {
			sensors.add(sen.getType());
		}

		return new HardwareInfo(extractDeviceId(), Build.MANUFACTURER,
				Build.MODEL, Build.VERSION.SDK, sensors);
	}

	public void sendDeviceInformationToMosesServer(HardwareInfo hardware,
			String c2dmRegistrationId, String sessionId) {
	}

	public static String extractDeviceId() {
		String deviceid = "";
		if (MosesService.getInstance() != null)
			deviceid = PreferenceManager.getDefaultSharedPreferences(
					MosesService.getInstance()).getString("deviceid_pref", "");
		return deviceid;
	}

	/**
	 * Device informations like hardware parameters, and cloud notification
	 * (c2dm) identification/connection tokens are sent to the moses server
	 * 
	 * @param c2dmRegistrationId
	 * @param sessionID
	 */
	public void syncDeviceInformation(final boolean force) {
		if (MosesService.getInstance() != null)
			MosesService.getInstance().executeLoggedInPriority(new Executor() {

				@Override
				public void execute() {
					HardwareInfo hwInfo = retrieveHardwareParameters();
					new RequestSetHardwareParameters(new ReqClassSetHWParams(),
							hwInfo, force, RequestLogin.getSessionID()).send();
				}
			});
	}
}
