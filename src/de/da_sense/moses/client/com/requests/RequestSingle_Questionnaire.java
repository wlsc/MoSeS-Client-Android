package de.da_sense.moses.client.com.requests;

import org.json.JSONException;
import org.json.JSONObject;

import de.da_sense.moses.client.com.NetworkJSON;
import de.da_sense.moses.client.com.ReqTaskExecutor;
import de.da_sense.moses.client.service.MosesService;
import de.da_sense.moses.client.util.Log;

/**
 * Request for a Single_Questionnaire
 * @author Florian
 *
 */
public class RequestSingle_Questionnaire {
	
	private JSONObject j;
    ReqTaskExecutor executor;
    

	public RequestSingle_Questionnaire(ReqTaskExecutor executor, String apkid, String questID) {
        j = new JSONObject();
        this.executor = executor;
        try {
            j.put("MESSAGE", "GET_QUESTIONNAIRE");
            j.put("SESSIONID", MosesService.getInstance().getSessionID());
            j.put("APKID", apkid);
            j.put("QUESTID", questID);
            Log.i("RequestSingle_Questionnaire", "SESSIONID =" + MosesService.getInstance().getSessionID()+ " APKID = " + apkid + " QuestID = " + questID);
        } catch (JSONException ex) {
            executor.handleException(ex);
        }
	}

    public void send() {
        Log.d("RequestSingle_Questionnaire", j.toString());
        NetworkJSON task = new NetworkJSON();
        NetworkJSON.APIRequest req;
        req = task.new APIRequest();
        req.request = j;
        req.reqTaskExecutor = this.executor;
        task.execute(req);
    }
}