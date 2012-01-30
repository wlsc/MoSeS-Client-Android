package moses.client.com;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

import android.os.AsyncTask;

/**
 * This class gives basic request capability.
 * 
 * @author Jaco Hofmann
 */

public class NetworkJSON
		extends
		AsyncTask<NetworkJSON.APIRequest, NetworkJSON.BackgroundException, String> {

	/**
	 * The Class APIRequest.
	 */
	public class APIRequest {

		/** The request. */
		public JSONObject request;

		/** The e. */
		public ReqTaskExecutor e;
	}

	/**
	 * The Class BackgroundException.
	 */
	public class BackgroundException {

		/** The c. */
		public ConnectionParam c;

		/** The e. */
		public Exception e;

		/**
		 * Instantiates a new background exception.
		 * 
		 * @param c
		 *            the c
		 * @param e
		 *            the e
		 */
		public BackgroundException(ConnectionParam c, Exception e) {
			this.c = c;
			this.e = e;
		}
	}

	/** The url. */
	public static String url;

	/** The e. */
	private ReqTaskExecutor e;

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.os.AsyncTask#doInBackground(Params[])
	 */
	@Override
	protected String doInBackground(NetworkJSON.APIRequest... params) {
		publishProgress(new BackgroundException(ConnectionParam.INIT, null));
		e = params[0].e;
		String ret = "";
		try {
			publishProgress(new BackgroundException(ConnectionParam.CONNECTING,
					null));
			HttpResponse re = doPost(url, params[0].request);
			ret = EntityUtils.toString(re.getEntity());
		} catch (Exception e) {
			publishProgress(new BackgroundException(ConnectionParam.EXCEPTION,
					e));
		}
		publishProgress(new BackgroundException(ConnectionParam.POSTEXECUTE,
				null));
		return ret;
	}

	/**
	 * Do post.
	 * 
	 * @param url
	 *            the url
	 * @param j
	 *            the j
	 * @return the http response
	 * @throws ClientProtocolException
	 *             the client protocol exception
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	private HttpResponse doPost(String url, JSONObject j)
			throws ClientProtocolException, IOException {
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.os.AsyncTask#onPostExecute(java.lang.Object)
	 */
	@Override
	protected void onPostExecute(String result) {
		e.postExecution(result);
	}

	/**
	 * On progress update.
	 * 
	 * @param c
	 *            the c
	 */
	protected void onProgressUpdate(BackgroundException... c) {
		if (c != null && e != null)
			e.updateExecution(c[0]);
	}
}
