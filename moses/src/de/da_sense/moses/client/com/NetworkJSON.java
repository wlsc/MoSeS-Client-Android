package de.da_sense.moses.client.com;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import android.os.AsyncTask;
import android.util.Log;
import de.da_sense.moses.client.service.MosesService;
import de.da_sense.moses.client.util.Toaster;

/**
 * This class gives basic request capability.
 * 
 * @author Jaco Hofmann
 */

public class NetworkJSON extends AsyncTask<NetworkJSON.APIRequest, NetworkJSON.BackgroundException, String> {

	// Needed for testing
	private static CountDownLatch signal = new CountDownLatch(1);
	
	private final String LOG_TAG = NetworkJSON.class.getName();

	/**
	 * The Class APIRequest.
	 */
	public class APIRequest {

		/** The request. */
		public JSONObject request;

		/** The Instance of ReqTaskExecutor. */
		public ReqTaskExecutor reqTaskExecutor;
	}

	/**
	 * The Class BackgroundException.
	 */
	public class BackgroundException {

		/** Instance of ConnectionParam - Specifies the current state of the connection. */
		public ConnectionParam c;

		/** Instance of Exception. */
		public Exception e;

		/**
		 * Instantiates a new background exception.
		 * 
		 * @param c
		 *            A ConnectionParam c
		 * @param e
		 *            An Exception e
		 */
		private BackgroundException(ConnectionParam c, Exception e) {
			this.c = c;
			this.e = e;
		}
	}

	/** The url. */
	public static String url;

	private static boolean debug = false;

	private static FakeResponseGenerator response = null;

	/** The e. */
	private ReqTaskExecutor e;


	/**
	 * Goes through the stages INIT (get the ReqTaskExecutor and the prepare the return),
	 * CONNECTING (Call doPost with the JSONObject in params[0] and parse the result into a
	 * string), EXEPTION (only if an Exception occurred) and POSTEXECUTE (return the result)
	 * and issues updates whenever it's reaching a new stage.
	 *  
	 * @param params One or more instances of APIRequest
	 */
	@Override
	protected String doInBackground(NetworkJSON.APIRequest... params) {
		publishProgress(new BackgroundException(ConnectionParam.INIT, null));
		e = params[0].reqTaskExecutor;
		String ret = "";
		if (debug) {
			signal = new CountDownLatch(1);
			return response.generateAnswer(params[0].request);
		}
		try {
			publishProgress(new BackgroundException(ConnectionParam.CONNECTING, null));
			HttpResponse re = doPost(url, params[0].request);
			ret = EntityUtils.toString(re.getEntity());
		} catch (Exception e) {
			publishProgress(new BackgroundException(ConnectionParam.EXCEPTION, e));
		}
		publishProgress(new BackgroundException(ConnectionParam.POSTEXECUTE, null));
		return ret;
	}

	/**
	 * Establishes a connection to the Server, sends the JSONObject, returns the
	 * response and updates current stage to CONNECTED (see doInBackground for
	 * more information).
	 * 
	 * @param url
	 *            the url
	 * @param j
	 *            the JSONObject to send to the Server
	 * @return the http response
	 * @throws ClientProtocolException
	 *             the client protocol exception
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	private HttpResponse doPost(String url, JSONObject j) throws ClientProtocolException, IOException {
		HttpClient httpclient = new DefaultHttpClient();
		HttpPost httppost = new HttpPost(url);

		List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(1);
		nameValuePairs.add(new BasicNameValuePair("HTTP_JSON", j.toString()));

		httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

		HttpResponse response;
		response = httpclient.execute(httppost);
		publishProgress(new BackgroundException(ConnectionParam.CONNECTED, null));
		return response;
	}

	/**
	 * Called after doInBackground and given it's result as parameter. Calls
	 * the postExecution Method of the given Request.
	 * 
	 * @param result The result from doInBackground
	 */
	@Override
	protected void onPostExecute(String result) {
		e.postExecution(result);
		signal.countDown();
		try {
			JSONObject j = new JSONObject(result);
			if(j.getString("STATUS").equals("INVALID_SESSION")) {
				MosesService.getInstance().loggedOut();
				MosesService.getInstance().login();
			}
		} catch (JSONException e1) {
			/*
			 * One of the reason for a malformed answer could be the loss of Internet connection.
			 * If so, do not throw any notifications to user from here, higher layers should do that
			 */
			MosesService ms = MosesService.getInstance();
			if(ms != null){
				if(ms.isOnlineOrIsConnecting()){
					// Server's answer was not malformed due to an absent Internet connection
					Log.e(LOG_TAG, "onPostExecute() " + e1);
					Toaster.showBadServerResponseToast();
				}
			}
			else{
				Log.w(LOG_TAG, "onPostExecute() MosesService was not running.");
			}
			
			
		}
	}

	/**
	 * On progress update.
	 * 
	 * @param c
	 *            the c
	 */
	@Override
	protected void onProgressUpdate(BackgroundException... c) {
		if (c != null && e != null)
			e.updateExecution(c[0]);
	}
}
