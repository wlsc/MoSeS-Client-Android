package moses.client;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.os.AsyncTask;
import android.util.JsonToken;
import android.widget.TextView;

public class NetworkJSON extends
		AsyncTask<NetworkJSON.APIRequest, String, String> {

	private String result;
	private TextView txtv;

	private HttpResponse doPost(String url, JSONObject j)
			throws ClientProtocolException, IOException {
		HttpClient httpclient = new DefaultHttpClient();
		HttpPost httppost = new HttpPost(url);

		List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(1);
		nameValuePairs.add(new BasicNameValuePair("HTTP_JSON", j.toString()));

		httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
		
		HttpResponse response;
		response = httpclient.execute(httppost);
		return response;
	}

	@Override
	protected String doInBackground(NetworkJSON.APIRequest... params) {
		txtv = params[0].txtv;
		publishProgress("Connecting...");
		String ret = "";
		try {
			HttpResponse re = doPost(params[0].url, params[0].request);
			ret = EntityUtils.toString(re.getEntity());
		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return ret;
	}

	protected void onProgressUpdate(String r) {
		txtv.setText(r);
	}

	protected void onPostExecute(String result) {
		txtv.setText(result);
	}

	public String getResult() {
		return result;
	}

	public class APIRequest {
		public String url;
		public JSONObject request;
		public TextView txtv;
	}
}
