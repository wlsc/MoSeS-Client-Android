/**
 * 
 */
package moses.client.abstraction;

import moses.client.com.ConnectionParam;
import moses.client.com.NetworkJSON.BackgroundException;
import moses.client.com.ReqTaskExecutor;
import moses.client.com.requests.RequestGetListAPK;
import moses.client.com.requests.RequestLogin;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;

/**
 * This class offers methods getting the informations about available
 * APKs from the server, it is also used 
 * @author Zijad Maksuti
 *
 */

public class APKAbstraction {
	
	private static AlertDialog infoDialog; // used for showing the results
	
	/**
	 * Constructs a new APKAbstraction
	 * which methods can be used for getting informations about available APKs
	 * from the server
	 * @param c the Context in which the APKAbstraction should operate
	 */
	public APKAbstraction(Context c) {
		infoDialog = new AlertDialog.Builder(c).create();
		infoDialog.setTitle("INFO:");
		infoDialog.setButton(DialogInterface.BUTTON_POSITIVE, "Ok",
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						infoDialog.dismiss();
					}
				});
	}
	
	/**
	 * This method sends a request to the server in order
	 * to get the list of available APKs (for the specified filter
	 */
	public void getAPKs() {
		String sessionID = RequestLogin.getSessionID(); // obtain the session id
		
		RequestGetListAPK rGetListAPK = new RequestGetListAPK(
				new ReqClassGetListAPK(), sessionID);
		
		rGetListAPK.send();
	}
	
	
	
	private class ReqClassGetListAPK implements ReqTaskExecutor {

		@Override
		public void handleException(Exception e) {
			infoDialog.setMessage("FAILURE ON REQUESTING LIST OF APKs: " + e.getMessage());
			infoDialog.show();
		}

		@Override
		public void postExecution(String s) {
			JSONObject j = null;
			try {
				j = new JSONObject(s);
				// TODO handling
				if (RequestGetListAPK.isListRetrived(j)) {
					// get the informations about available APKs
					StringBuffer sb = new StringBuffer(512);
					sb.append("Request sent successfully, server returned positive response");
					sb.append("\n").append("-List of APKs-").append("\n");
					
					JSONArray apkInformations = j.getJSONArray("APK_LIST");
					for(int i=0; i<apkInformations.length(); i++){
						sb.append("\n");
						JSONObject apkInformation = apkInformations.getJSONObject(i);
						sb.append("APK ID: ").append(apkInformation.getString("ID")).append("\n");
						sb.append("NAME: ").append(apkInformation.getString("NAME")).append("\n");
						sb.append("DESCRIPTION: ").append(apkInformation.getString("DESCR")).append("\n");
					}
					
					infoDialog.setMessage(sb.toString());
					infoDialog.show();
				} else {
					// TODO handling
					infoDialog.setMessage("Request not successfull! Server returned negative response");
					infoDialog.show();
				}
			} catch (JSONException e) {
				this.handleException(e);
			}
		}

		@Override
		public void updateExecution(BackgroundException c) {
			if (c.c != ConnectionParam.EXCEPTION) {
				infoDialog.setMessage(c.c.toString());
				infoDialog.show();
			} else {
				handleException(c.e);
			}
		}
	}
	
	
	
}
