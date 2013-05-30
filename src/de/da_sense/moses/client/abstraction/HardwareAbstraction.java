/**
 * 
 */
package de.da_sense.moses.client.abstraction;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Handler;
import android.preference.PreferenceManager;
import de.da_sense.moses.client.R;
import de.da_sense.moses.client.com.ConnectionParam;
import de.da_sense.moses.client.com.NetworkJSON.BackgroundException;
import de.da_sense.moses.client.com.ReqTaskExecutor;
import de.da_sense.moses.client.com.requests.RequestChangeDeviceIDParameters;
import de.da_sense.moses.client.com.requests.RequestGetFilter;
import de.da_sense.moses.client.com.requests.RequestGetHardwareParameters;
import de.da_sense.moses.client.com.requests.RequestLogin;
import de.da_sense.moses.client.com.requests.RequestSetFilter;
import de.da_sense.moses.client.com.requests.RequestSetHardwareParameters;
import de.da_sense.moses.client.service.MosesService;
import de.da_sense.moses.client.service.helpers.C2DMManager;
import de.da_sense.moses.client.service.helpers.EHookTypes;
import de.da_sense.moses.client.service.helpers.EMessageTypes;
import de.da_sense.moses.client.service.helpers.Executable;
import de.da_sense.moses.client.util.Log;

/**
 * This class provides basic support for hardware sync with server
 * 
 * @author Jaco Hofmann
 * 
 */
public class HardwareAbstraction {

    /**
     * This class is used to store informaions of a device
     */
	public static class HardwareInfo {
	    /** device id that user gives to a device */
		private String deviceID;
		/** sdk version of a device */
		private String sdkbuildversion;
		/** vendor of a device */
		private String vendor;
		/** model of a device */
		private String model;
		/** set of sensors that a device provides */
		private List<Integer> sensors;
		
		/**
		 * This method is used to create a hardwareinfo
		 * @param deviceID of a device
		 * @param vendor of a device
		 * @param model of a device
		 * @param sdkbuildversion sd version of a device
		 * @param sensors of a device
		 */
		public HardwareInfo(String deviceID, String vendor, String model, String sdkbuildversion, List<Integer> sensors) {
			super();
			this.deviceID = deviceID;
			this.sdkbuildversion = sdkbuildversion;
			this.sensors = sensors;
			this.vendor = vendor;
			this.model = model;
		}
		
		/**
		 * getter method for vendor
		 * @return
		 */
		public String getDeviceVendor() {
			return vendor;
		}
		
		/**
         * getter method for model
         * @return
         */
		public String getDeviceModel() {
			return model;
		}
		
		/**
         * getter method for device id
         * @return
         */
		public String getDeviceID() {
			return deviceID;
		}
		
		/**
         * getter method for sdk version
         * @return
         */
		public String getSdkbuildversion() {
			return sdkbuildversion;
		}
		
		/**
         * getter method for set of sensors
         * @return
         */
		public List<Integer> getSensors() {
			return sensors;
		}
	}

	/**
	 * This class is used for NetworkJSON calls as a response for a filter call
	 */
	private class ReqClassGetFilter implements ReqTaskExecutor {

	    /**
	     * to handle an exception
         * @param e Exception
	     */
		@Override
		public void handleException(Exception e) {
			Log.d("MoSeS.HARDWARE_ABSTRACTION", "FAILURE: " + e.getMessage());
		}

		/**
		 * to post an execution
         * @param s the JSONObject as String
		 */
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
							"Parameters NOT retrieved successfully! Server returned negative response");
				}
			} catch (JSONException e) {
				this.handleException(e);
			}
		}
		
		/**
		 * to update an execution
         * @param c BackgroundException
		 */
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
			SensorManager s = (SensorManager) MosesService.getInstance().getSystemService(Context.SENSOR_SERVICE);
			for (Sensor sen : s.getSensorList(Sensor.TYPE_ALL))
				sensors.add(sen);
			return sensors;
		} else {
			return null;
		}
	}

	/** ProgressDialog that describes the actual status of the network connection */
	private ProgressDialog gethwprogressdialog = null;
	/** handler for the runnables */
	private Handler handler = new Handler();

	/**
	 * This class is used for NetworkJSON calls as a response for a sensors call
	 */
	private class ReqClassGetHWParams implements ReqTaskExecutor {

	    /**
         * to handle an exception
         * @param e Exception
         */
		@Override
		public void handleException(Exception e) {
			Log.d("MoSeS.HARDWARE_ABSTRACTION", "FAILURE: " + e.getMessage());
			// change the status of the network connection
			gethwprogressdialog.setMessage("Error while retrieving Hardware Informations.");
			handler.postDelayed(new Runnable() {
				@Override
				public void run() {
					gethwprogressdialog.dismiss();
				}
			}, 2000);
		}

		/**
         * to post an execution
         * @param s the JSONObject as String
         */
		@Override
		public void postExecution(String s) {
			JSONObject j = null;
			try {
				j = new JSONObject(s);
				if (RequestGetHardwareParameters.parameterAcquiredFromServer(j)) {
					StringBuffer sb = new StringBuffer(256);
					// add the hardware parameters to sb
					sb.append("Parameters retrieved successfully from server");
					sb.append("\n").append("Device id:").append(j.get("DEVICEID"));
					sb.append("\n").append("Android version:").append(j.get("ANDVER"));
					JSONArray sensors = j.getJSONArray("SENSORS");
					sb.append("\n").append("SENSORS:").append("\n");
					// adding sensors to sb
					for (int i = 0; i < sensors.length(); i++) {
						sb.append("\n");
						sb.append(ESensor.values()[sensors.getInt(i)]);
					}
					Log.d("MoSeS.HARDWARE_ABSTRACTION", sb.toString());
					AlertDialog ad = new AlertDialog.Builder(appContext).create();
					// prepare a dialog for this information
					ad.setCancelable(false); // This blocks the 'BACK' button
					ad.setIcon(R.drawable.ic_launcher);
					ad.setMessage(sb.toString());
					ad.setButton("OK", new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							dialog.dismiss();
						}
					});
					gethwprogressdialog.dismiss();
					// show the dialog
					ad.show();
				} else {
					Log.d("MoSeS.HARDWARE_ABSTRACTION", "Parameters NOT retrieved successfully from server! :(");
				}
			} catch (JSONException e) {
				this.handleException(e);
			}
		}

		/**
         * to update an execution
         * @param c BackgroundException
         */
		@Override
		public void updateExecution(BackgroundException c) {
			if (c.c == ConnectionParam.EXCEPTION) {
				handleException(c.e);
			}
		}
	}

	/**
	 * This class is used for NetworkJSON calls as a request for filter
	 */
	private class ReqClassSetFilter implements ReqTaskExecutor {

	    /**
         * to handle an exception
         * @param e Exception
         */
		@Override
		public void handleException(Exception e) {
			Log.d("MoSeS.HARDWARE_ABSTRACTION", "FAILURE SETTING FILTER: " + e.getMessage());
		}

		/**
         * to post an execution
         * @param s the JSONObject as String
         */
		@Override
		public void postExecution(String s) {
			JSONObject j = null;
			try {
				j = new JSONObject(s);
				if (RequestSetFilter.filterSetOnServer(j)) {
					Log.d("MoSeS.HARDWARE_ABSTRACTION", "Filter set successfully, server returned positive response");
				} else {
					Log.d("MoSeS.HARDWARE_ABSTRACTION",
							"Filter NOT set successfully! Server returned negative response");
				}
			} catch (JSONException e) {
				this.handleException(e);
			}
		}

		/**
         * to update an execution
         * @param c BackgroundException
         */
		@Override
		public void updateExecution(BackgroundException c) {
			if (c.c == ConnectionParam.EXCEPTION) {
				handleException(c.e);
			}
		}
	}

	/**
     * This class is used for NetworkJSON calls as a request to update the hardware parameters of a device
     */
	private class ReqClassUpdateHWParams implements ReqTaskExecutor {

	    /**
         * to handle an exception
         * @param e Exception
         */
		@Override
		public void handleException(Exception e) {
			Log.d("MoSeS.HARDWARE_ABSTRACTION", "FAILURE: " + e.getMessage());
			MosesService.getInstance().noOnSharedPreferenceChanged(true);
			// recover the last device id of this device
			PreferenceManager
					.getDefaultSharedPreferences(appContext)
					.edit()
					.putString(
							"deviceid_pref",
							PreferenceManager.getDefaultSharedPreferences(MosesService.getInstance()).getString(
									"lastdeviceid", "")).commit();
			MosesService.getInstance().noOnSharedPreferenceChanged(false);
		}

		/**
         * to post an execution
         * @param s the JSONObject as String
         */
		@Override
		public void postExecution(String s) {
			JSONObject j = null;
			try {
				Log.d("MoSeS.HARDWARE_ABSTRACTION", "Received: " + s);
				j = new JSONObject(s);
				// if this message contains SUCCESS as value of STATUS which represents that the status of updating the device id
				if (j.getString("STATUS").equals("SUCCESS")) {
					Log.d("MoSeS.HARDWARE_ABSTRACTION",
							"Updated device id successfully, server returned positive response");
					MosesService.getInstance().noOnSharedPreferenceChanged(true);
					// set the new device id as last device id
					PreferenceManager
							.getDefaultSharedPreferences(appContext)
							.edit()
							.putString(
									"lastdeviceid",
									PreferenceManager.getDefaultSharedPreferences(MosesService.getInstance())
											.getString("deviceid_pref", "")).commit();
					MosesService.getInstance().noOnSharedPreferenceChanged(false);
					MosesService.getInstance().executeLoggedIn(EHookTypes.POSTLOGINSUCCESSPRIORITY,
							EMessageTypes.REQUESTSETHARDWAREPARAMETERS, new Executable() {
								@Override
								public void execute() {
									syncDeviceInformation(true);
								}
							});
				} else if (j.getString("STATUS").equals("FAILURE_DEVICEID_DUPLICATED")) {
				    // if there is a duplication with another device id
					showForceDialog(j.getString("VENDOR_NAME"), j.getString("MODEL_NAME"), j.getString("ANDVER"),
							MosesService.getInstance().getActivityContext(), true);
				}  else if (j.getString("STATUS").equals("FAILURE_DEVICEID_NOT_SET")) {
                    // if there is no device id been set for this device
					MosesService.getInstance().executeLoggedIn(EHookTypes.POSTLOGINSUCCESSPRIORITY,
							EMessageTypes.REQUESTSETHARDWAREPARAMETERS, new Executable() {
								@Override
								public void execute() {
									syncDeviceInformation(false);
								}
							});
				}
				else
				{
				    // if the session id is invalid
					Log.d("MoSeS.HARDWARE_ABSTRACTION", "Update device id FAILED! Invalid session id.");
					MosesService.getInstance().noOnSharedPreferenceChanged(true);
					// set the new device id as last device id
					PreferenceManager
							.getDefaultSharedPreferences(appContext)
							.edit()
							.putString(
									"deviceid_pref",
									PreferenceManager.getDefaultSharedPreferences(MosesService.getInstance())
											.getString("lastdeviceid", "")).commit();
					MosesService.getInstance().noOnSharedPreferenceChanged(false);
				}
			} catch (JSONException e) {
				this.handleException(e);
			}
		}

		/**
         * to update an execution
         * @param c BackgroundException
         */
		@Override
		public void updateExecution(BackgroundException c) {
			if (c.c == ConnectionParam.EXCEPTION) {
				handleException(c.e);
			}
		}
	}

	/**
     * This class is used for NetworkJSON calls as a request to set the hardware parameters of a device
     */
	private class ReqClassSetHWParams implements ReqTaskExecutor {

	    /**
         * to handle an exception
         * @param e Exception
         */
		@Override
		public void handleException(Exception e) {
			Log.d("MoSeS.HARDWARE_ABSTRACTION", "FAILURE: " + e.getMessage());
			MosesService.getInstance().noOnSharedPreferenceChanged(true);
			// recover the last device id for this device
			PreferenceManager
					.getDefaultSharedPreferences(appContext)
					.edit()
					.putString(
							"deviceid_pref",
							PreferenceManager.getDefaultSharedPreferences(MosesService.getInstance()).getString(
									"lastdeviceid", "")).commit();
			MosesService.getInstance().noOnSharedPreferenceChanged(false);
		}

		/**
         * to post an execution
         * @param s the JSONObject as String
         */
		@Override
		public void postExecution(String s) {
			JSONObject j = null;
			try {
				Log.d("MoSeS.HARDWARE", "SetHWParams request response: " + s);
				j = new JSONObject(s);
				// if the parameters already been set on server
				if (RequestSetHardwareParameters.parameterSetOnServer(j)) {
					Log.d("MoSeS.HARDWARE_ABSTRACTION",
							"Parameters set successfully, server returned positive response");
					MosesService.getInstance().noOnSharedPreferenceChanged(true);
					// set the new device id as last device id
					PreferenceManager
							.getDefaultSharedPreferences(appContext)
							.edit()
							.putBoolean("deviceidsetsuccessfully", true)
							.putString(
									"lastdeviceid",
									PreferenceManager.getDefaultSharedPreferences(MosesService.getInstance())
											.getString("deviceid_pref", "")).commit();
					MosesService.getInstance().noOnSharedPreferenceChanged(false);
					// sending the current C2DM of this device
					C2DMManager.sendCurrentC2DM();
					MosesService.getInstance().uploadFilter();
				} else if (j.getString("STATUS").equals("FAILURE_DEVICEID_DUPLICATED")) {
				    // if there is a duplication with another device id
					showForceDialog(j.getString("VENDOR_NAME"), j.getString("MODEL_NAME"), j.getString("ANDVER"),
							MosesService.getInstance().getActivityContext(), false);
				} else {
				    // if the session id is invalid
					Log.d("MoSeS.HARDWARE_ABSTRACTION", "Parameters NOT set successfully! Invalid session id.");
				}
			} catch (JSONException e) {
				this.handleException(e);
			}
		}

		/**
         * to update an execution
         * @param c BackgroundException
         */
		@Override
		public void updateExecution(BackgroundException c) {
			if (c.c == ConnectionParam.EXCEPTION) {
				handleException(c.e);
			}
		}
	}

	/**
	 * Show this dialog when the device id that user set is duplicated with another device id
	 * @param vendor of this device
	 * @param model of this device
	 * @param andver the android version of this device
	 * @param c Context
	 * @param update whether there was a valid device id or not
	 */
	public void showForceDialog(String vendor, String model, String andver, Context c, boolean update) {
		AlertDialog a = new AlertDialog.Builder(c).create();
		// prepare this dialog
		a.setIcon(R.drawable.ic_launcher);
		a.setTitle(R.string.dialog_duplicated_deviceID_title);
		a.setMessage(MosesService.getInstance().getString(R.string.dialog_duplicated_deviceID_text) + "\nVendor: "
				+ vendor + "\nModel: " + model + "\nSDK Version: " + andver + "\n"
				+ MosesService.getInstance().getString(R.string.dialog_duplicated_deviceID_text2));
		a.setIcon(R.drawable.ic_launcher);
		if (update) {
		    // there was a valid device id for this device
			a.setButton(MosesService.getInstance().getString(R.string.dialog_duplicated_deviceID_update),
					new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface arg0, int arg1) {
							MosesService.getInstance().executeLoggedIn(EHookTypes.POSTLOGINSUCCESSPRIORITY,
									EMessageTypes.REQUESTUPDATEHARDWAREPARAMETERS, new Executable() {
										@Override
										public void execute() {
											HardwareAbstraction.this.changeDeviceID(true);
										}
									});
							arg0.dismiss();
						}
					});
			a.setButton2(MosesService.getInstance().getString(R.string.dialog_duplicated_deviceID_cancel),
					new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface arg0, int arg1) {
							MosesService.getInstance().noOnSharedPreferenceChanged(true);
							PreferenceManager.getDefaultSharedPreferences(MosesService.getInstance()).edit()
									.putString("deviceid_pref", "").commit();
							MosesService.getInstance().noOnSharedPreferenceChanged(false);
							MosesService.getInstance().notSetDeviceID();
							arg0.dismiss();
						}

					});
		} else {
		    // there was not a valid device id for this device
			a.setButton(MosesService.getInstance().getString(R.string.dialog_duplicated_deviceID_update),
					new DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface arg0, int arg1) {
							MosesService.getInstance().executeLoggedIn(EHookTypes.POSTLOGINSUCCESSPRIORITY,
									EMessageTypes.REQUESTSETHARDWAREPARAMETERS, new Executable() {

										@Override
										public void execute() {
											HardwareAbstraction.this.syncDeviceInformation(true);
										}
									});
							arg0.dismiss();
						}
					});
			a.setButton2(MosesService.getInstance().getString(R.string.dialog_duplicated_deviceID_cancel),
					new DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface arg0, int arg1) {
							MosesService.getInstance().noOnSharedPreferenceChanged(true);
							PreferenceManager.getDefaultSharedPreferences(MosesService.getInstance()).edit()
									.putString("deviceid_pref", "").commit();
							MosesService.getInstance().noOnSharedPreferenceChanged(false);
							MosesService.getInstance().notSetDeviceID();
							arg0.dismiss();
						}

					});
		}
		a.setIcon(R.drawable.ic_launcher);
		a.show();
	}

	/** the context */
	private Context appContext;

	/**
	 * This method is used to create HardwareAbstraction
	 * @param c Context
	 */
	public HardwareAbstraction(Context c) {
		appContext = c;
	}

	/**
	 * This method sends a Request to the website for obtaining the filter
	 * stored for this device
	 */
	public void getFilter() {
		if (MosesService.getInstance() != null)
			MosesService.getInstance().executeLoggedIn(EHookTypes.POSTLOGINSUCCESS, EMessageTypes.REQUESTGETFILTER,
					new Executable() {
						@Override
						public void execute() {
							final RequestGetFilter rGetFilter = new RequestGetFilter(new ReqClassGetFilter(),
									RequestLogin.getSessionID(), extractDeviceId());
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
			MosesService.getInstance().executeLoggedIn(EHookTypes.POSTLOGINSUCCESS,
					EMessageTypes.REQUESTGETHARDWAREPARAMETERS, new Executable() {
						@Override
						public void execute() {
							gethwprogressdialog = new ProgressDialog(appContext);
							gethwprogressdialog.setTitle("Hardware Informations");
							gethwprogressdialog.setMessage("Retrieving...");
							gethwprogressdialog.show();
							new RequestGetHardwareParameters(new ReqClassGetHWParams(), RequestLogin.getSessionID(),
									extractDeviceId()).send();
						}
					});
	}

	/**
	 * This method sends a set_filter Request to the website
	 */
	public void setFilter(final String filter) {
		// *** SENDING GET_HARDWARE_PARAMETERS REQUEST TO SERVER ***//
		if (MosesService.getInstance() != null)
			MosesService.getInstance().executeLoggedIn(EHookTypes.POSTLOGINSUCCESS, EMessageTypes.REQUESTSETFILTER,
					new Executable() {
						@Override
						public void execute() {
							RequestSetFilter rSetFilter = new RequestSetFilter(new ReqClassSetFilter(), RequestLogin
									.getSessionID(), extractDeviceId(), filter);
							rSetFilter.send();
						}
					});
	}

	/**
	 * This method reads the sensors currently chosen by the user
	 * @return the actual Hardwareinfo
	 */
	private HardwareInfo retrieveHardwareParameters() {
		// *** SENDING SET_HARDWARE_PARAMETERS REQUEST TO SERVER ***//
		LinkedList<Integer> sensors = new LinkedList<Integer>();
		SensorManager s = (SensorManager) appContext.getSystemService(Context.SENSOR_SERVICE);
		for (Sensor sen : s.getSensorList(Sensor.TYPE_ALL)) {
			sensors.add(sen.getType());
		}
		return new HardwareInfo(extractDeviceId(), Build.MANUFACTURER, Build.MODEL, Build.VERSION.SDK, sensors);
	}

	/**
	 * to get device id that been stored
	 * @return device id as String
	 */
	public static String extractDeviceId() {
		String deviceid = "";
		if (MosesService.getInstance() != null)
			deviceid = PreferenceManager.getDefaultSharedPreferences(MosesService.getInstance()).getString(
					"deviceid_pref", "");
		return deviceid;
	}

	/**
	 * to change device id of this device
	 * @param force whether user want to change the device id or not
	 */
	public void changeDeviceID(final boolean force) {
		if (MosesService.getInstance() != null) {
			if (!PreferenceManager
					.getDefaultSharedPreferences(MosesService.getInstance())
					.getString("deviceid_pref", "")
					.equals(PreferenceManager.getDefaultSharedPreferences(MosesService.getInstance()).getString(
							"lastdeviceid", ""))) {
			    // if the new device id is not like the last device id
				MosesService.getInstance().executeLoggedIn(EHookTypes.POSTLOGINSUCCESSPRIORITY,
						EMessageTypes.REQUESTUPDATEHARDWAREPARAMETERS, new Executable() {
							@Override
							public void execute() {
							    // changing the device id
								new RequestChangeDeviceIDParameters(new ReqClassUpdateHWParams(), force,
										PreferenceManager.getDefaultSharedPreferences(MosesService.getInstance())
												.getString("deviceid_pref", ""), PreferenceManager.getDefaultSharedPreferences(MosesService.getInstance())
												.getString("lastdeviceid", ""), RequestLogin.getSessionID()).send();
							}
						});
			}
		}
	}

	/**
	 * to synchronize the server with the actual information of this device
	 * @param force boolean
	 */
	public void syncDeviceInformation(final boolean force) {
		if (MosesService.getInstance() != null) {
			MosesService.getInstance().executeLoggedIn(EHookTypes.POSTLOGINSUCCESSPRIORITY,
					EMessageTypes.REQUESTSETHARDWAREPARAMETERS, new Executable() {
						@Override
						public void execute() {
							HardwareInfo hwInfo = retrieveHardwareParameters();
							new RequestSetHardwareParameters(new ReqClassSetHWParams(), hwInfo, force, RequestLogin
									.getSessionID()).send();
						}
					});
		}
	}
}
