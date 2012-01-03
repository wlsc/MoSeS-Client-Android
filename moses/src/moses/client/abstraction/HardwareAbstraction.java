/**
 * 
 */
package moses.client.abstraction;

import java.util.LinkedList;
import java.util.List;

import moses.client.com.ConnectionParam;
import moses.client.com.NetworkJSON.BackgroundException;
import moses.client.com.ReqTaskExecutor;
import moses.client.com.requests.RequestGetFilter;
import moses.client.com.requests.RequestGetHardwareParameters;
import moses.client.com.requests.RequestLogin;
import moses.client.com.requests.RequestSetFilter;
import moses.client.com.requests.RequestSetHardwareParameters;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Build;

/**
 * This class provides basic support for hardware sync with server
 * 
 * @author Jaco Hofmann
 * 
 */
public class HardwareAbstraction {

	private class ReqClassSetHWParams implements ReqTaskExecutor {

		@Override
		public void handleException(Exception e) {
			alertDialog.setMessage("FAILURE: " + e.getMessage());
			alertDialog.show();
		}

		@Override
		public void postExecution(String s) {
			JSONObject j = null;
			try {
				j = new JSONObject(s);
				// TODO handling
				if (RequestSetHardwareParameters.parameterSetOnServer(j)) {
					alertDialog.setMessage("Parameters set successfully, server returned positive response");
					alertDialog.show();
				} else {
					// TODO handling
					alertDialog.setMessage("Parameters NOT set successfully! Server returned negative response");
					alertDialog.show();
				}
			} catch (JSONException e) {
				this.handleException(e);
			}
		}

		@Override
		public void updateExecution(BackgroundException c) {
			if (c.c != ConnectionParam.EXCEPTION) {
				alertDialog.setMessage(c.c.toString());
				alertDialog.show();
			} else {
				handleException(c.e);
			}
		}
	}
	
	
	private class ReqClassGetHWParams implements ReqTaskExecutor {

		@Override
		public void handleException(Exception e) {
			alertDialog.setMessage("FAILURE: " + e.getMessage());
			alertDialog.show();
		}

		@Override
		public void postExecution(String s) {
			JSONObject j = null;
			try {
				j = new JSONObject(s);
				// TODO handling
				if (RequestGetHardwareParameters.parameterAcquiredFromServer(j)) {
					StringBuffer sb = new StringBuffer(256);
					sb.append("Parameters retrived successfully from server");
					sb.append("\n").append("Device id:").append(j.get("DEVICEID"));
					sb.append("\n").append("Android version:").append(j.get("ANDVER"));
					// parse the sensors from JSON Object
					SensorManager senMan = (SensorManager) appContext
							.getSystemService(Context.SENSOR_SERVICE);
					JSONArray sensors = j.getJSONArray("SENSORS");
					sb.append("\n").append("SENSORS:").append("\n");
					for (int i=0; i<sensors.length(); i++) {
						sb.append("\n");
						sb.append(senMan.getDefaultSensor(sensors.getInt(i)).getName());
					}
					alertDialog.setMessage(sb.toString());
					alertDialog.show();
				} else {
					// TODO handling
					alertDialog.setMessage("Parameters NOT retrived successfully from server! :(");
					alertDialog.show();
				}
			} catch (JSONException e) {
				this.handleException(e);
			}
		}

		@Override
		public void updateExecution(BackgroundException c) {
			if (c.c != ConnectionParam.EXCEPTION) {
				alertDialog.setMessage(c.c.toString());
				alertDialog.show();
			} else {
				handleException(c.e);
			}
		}
	}
	
	private class ReqClassSetFilter implements ReqTaskExecutor {

		@Override
		public void handleException(Exception e) {
			alertDialog.setMessage("FAILURE SETTING FILTER: " + e.getMessage());
			alertDialog.show();
		}

		@Override
		public void postExecution(String s) {
			JSONObject j = null;
			try {
				j = new JSONObject(s);
				// TODO handling
				if (RequestSetFilter.filterSetOnServer(j)) {
					alertDialog.setMessage("Filter set successfully, server returned positive response");
					alertDialog.show();
				} else {
					// TODO handling
					alertDialog.setMessage("Filter NOT set successfully! Server returned negative response");
					alertDialog.show();
				}
			} catch (JSONException e) {
				this.handleException(e);
			}
		}

		@Override
		public void updateExecution(BackgroundException c) {
			if (c.c != ConnectionParam.EXCEPTION) {
				alertDialog.setMessage(c.c.toString());
				alertDialog.show();
			} else {
				handleException(c.e);
			}
		}
	}
	
	
	private class ReqClassGetFilter implements ReqTaskExecutor {

		@Override
		public void handleException(Exception e) {
			alertDialog.setMessage("FAILURE: " + e.getMessage());
			alertDialog.show();
		}

		@Override
		public void postExecution(String s) {
			JSONObject j = null;
			try {
				j = new JSONObject(s);
				// TODO handling
				if (RequestGetFilter.parameterAcquiredFromServer(j)) {
					StringBuffer sb = new StringBuffer(256);
					sb.append("Filter retrived successfully, server returned positive response");
					sb.append("\n");
					sb.append("stored filter:");
					sb.append("\n");
					SensorManager senMan = (SensorManager) appContext.getSystemService(Context.SENSOR_SERVICE);
					JSONArray filter = j.getJSONArray("FILTER");
					for (int i=0; i<filter.length(); i++) {
						sb.append("\n");
						sb.append(senMan.getDefaultSensor(filter.getInt(i)).getName());
					}
					alertDialog.setMessage(sb.toString());
					alertDialog.show();
				} else {
					// TODO handling
					alertDialog.setMessage("Parameters NOT retrived successfully! Server returned negative response");
					alertDialog.show();
				}
			} catch (JSONException e) {
				this.handleException(e);
			}
		}

		@Override
		public void updateExecution(BackgroundException c) {
			if (c.c != ConnectionParam.EXCEPTION) {
				alertDialog.setMessage(c.c.toString());
				alertDialog.show();
			} else {
				handleException(c.e);
			}
		}
	}
	
	
	private AlertDialog alertDialog;

	private Context appContext;

	public HardwareAbstraction(Context c) {
		alertDialog = new AlertDialog.Builder(c).create();
		alertDialog.setTitle("INFO:");
		alertDialog.setButton(DialogInterface.BUTTON_POSITIVE, "Ok",
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						alertDialog.dismiss();
					}
				});
		appContext = c;
	}

	/**
	 * TODO: Implement this function with getHardwareParameters to check if the
	 * information on the server has to be updated
	 */
	public void checkHardwareParameters() {
		setHardwareParameters();
	}

	/**
	 * This method reads the sensors currently chosen by the user and send the
	 * appropriate update to the MoSeS-Website
	 */
	private void setHardwareParameters() {
		// *** SENDING SET_HARDWARE_PARAMETERS REQUEST TO SERVER ***//
		String sessionID = RequestLogin.getSessionID(); // obtain the session id

		LinkedList<Integer> sensors = new LinkedList<Integer>();
		SensorManager s = (SensorManager) appContext
				.getSystemService(Context.SENSOR_SERVICE);
		for (Sensor sen : s.getSensorList(Sensor.TYPE_ALL)) {
			sensors.add(sen.getType());
		}

		RequestSetHardwareParameters rSetHWParams = new RequestSetHardwareParameters(
				new ReqClassSetHWParams(), sessionID, Build.MANUFACTURER
						+ " " + Build.MODEL + " " + Build.FINGERPRINT,
				Build.VERSION.SDK, sensors);
		rSetHWParams.send();

	}

	/**
	 * This method reads the sensor list stored for the device on the server
	 */
	public void getHardwareParameters() {
		// *** SENDING GET_HARDWARE_PARAMETERS REQUEST TO SERVER ***//
		String sessionID = RequestLogin.getSessionID(); // obtain the session id
	
		RequestGetHardwareParameters rGetHWParams = new RequestGetHardwareParameters(
				new ReqClassGetHWParams(), sessionID, Build.MANUFACTURER
						+ " " + Build.MODEL + " " + Build.FINGERPRINT);
		rGetHWParams.send();
	}
	
	/**
	 * This method sends a set_filter Request to the website
	 */
	public void setFilter(List<Integer> filter){
		// *** SENDING GET_HARDWARE_PARAMETERS REQUEST TO SERVER ***//
			String sessionID = RequestLogin.getSessionID(); // obtain the session id
		
			RequestSetFilter rSetFilter = new RequestSetFilter(
					new ReqClassSetFilter(), sessionID, Build.MANUFACTURER
					+ " " + Build.MODEL + " " + Build.FINGERPRINT, filter);
			rSetFilter.send();
	}

	/**
	 * This method sends a Request to the website for obtainint
	 * the filter stored for this device
	 */
	public void getFilter() {
		String sessionID = RequestLogin.getSessionID(); // obtain the session id
		
		RequestGetFilter rGetFilter = new RequestGetFilter(
				new ReqClassGetFilter(), sessionID, Build.MANUFACTURER
				+ " " + Build.MODEL + " " + Build.FINGERPRINT);
		rGetFilter.send();
		
	}
}