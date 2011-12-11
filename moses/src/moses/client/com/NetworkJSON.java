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
import android.widget.TextView;

public class NetworkJSON extends
		AsyncTask<NetworkJSON.APIRequest, ConnectionParam, String> {
	
	public static String url; 
	
	private ReqTaskExecutor e;
	
	private HttpResponse doPost(String url, JSONObject j)
			throws ClientProtocolException, IOException {
		HttpClient httpclient = new DefaultHttpClient();
		HttpPost httppost = new HttpPost(url);

		List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(1);
		nameValuePairs.add(new BasicNameValuePair("HTTP_JSON", j.toString()));

		httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

		HttpResponse response;
		response = httpclient.execute(httppost);
		publishProgress(ConnectionParam.CONNECTED);
		return response;
	}

	@Override
	protected String doInBackground(NetworkJSON.APIRequest... params) {
		publishProgress(ConnectionParam.INIT);
		e = params[0].e;
		String ret = "";
		try {
			publishProgress(ConnectionParam.CONNECTING);
			HttpResponse re = doPost(url, params[0].request);
			ret = EntityUtils.toString(re.getEntity());
		} catch (Exception e) {
			this.e.handleException(e);
		}
		publishProgress(ConnectionParam.POSTEXECUTE);
		return ret;
	}

	protected void onProgressUpdate(ConnectionParam c) {
		e.updateExecution(c);
	}

	protected void onPostExecute(String result) {
		e.postExecution(result);
	}

	public class APIRequest {
		public JSONObject request;
		public ReqTaskExecutor e;
	}
}
