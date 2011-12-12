/**
 * 
 */
package moses.client.abstraction;

import java.util.LinkedList;

import moses.client.com.ConnectionParam;
import moses.client.com.NetworkJSON.BackgroundException;
import moses.client.com.ReqTaskExecutor;
import moses.client.com.requests.RequestLogin;
import moses.client.com.requests.RequestSetHardwareParameters;

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

	private class ReqClassChooseSensors implements ReqTaskExecutor {

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
				if (RequestSetHardwareParameters.parameterSetOnServer(j)) {
					// TODO handling success
				} else {
					// TODO handling errors
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
		alertDialog.setTitle("Error:");
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
				new ReqClassChooseSensors(), sessionID, Build.MANUFACTURER
						+ " " + Build.MODEL + " " + Build.FINGERPRINT,
				Build.VERSION.SDK, sensors.toArray(new Integer[sensors.size()]));
		rSetHWParams.send();

	}
}
