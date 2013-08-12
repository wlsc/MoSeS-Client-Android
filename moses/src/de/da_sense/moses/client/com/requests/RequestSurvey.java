package de.da_sense.moses.client.com.requests;

import org.json.JSONException;
import org.json.JSONObject;

import de.da_sense.moses.client.com.NetworkJSON;
import de.da_sense.moses.client.com.ReqTaskExecutor;
import de.da_sense.moses.client.service.MosesService;
import de.da_sense.moses.client.util.Log;

/**
 * 
 * Requests a survey assigned to the specified APK from the server.
 * 
 * @author Wladimir Schmidt
 * @author Zijad Maksuti
 *
 */
public class RequestSurvey {
	
	private JSONObject j;
    private ReqTaskExecutor executor;
    private static final String LOG_TAG = RequestSurvey.class.getName();
    

	public RequestSurvey(ReqTaskExecutor getQuestionnaireExecutor, String id) {
        j = new JSONObject();
        this.executor = getQuestionnaireExecutor;
        try {
            j.put("MESSAGE", "GET_SURVEY");
            j.put("SESSIONID", MosesService.getInstance().getSessionID());
            j.put("APKID", id);
            Log.d(LOG_TAG, "SESSIONID =" + MosesService.getInstance().getSessionID()+ " APKID = " + id);
        } catch (JSONException ex) {
            executor.handleException(ex);
        }
	}

    public void send() {
        Log.d(LOG_TAG, j.toString());
        NetworkJSON task = new NetworkJSON();
        NetworkJSON.APIRequest req;
        req = task.new APIRequest();
        req.request = j;
        req.reqTaskExecutor = this.executor;
        task.execute(req);
    }
}

