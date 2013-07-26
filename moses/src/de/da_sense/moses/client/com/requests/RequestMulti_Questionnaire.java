package de.da_sense.moses.client.com.requests;

import org.json.JSONException;
import org.json.JSONObject;

import de.da_sense.moses.client.com.NetworkJSON;
import de.da_sense.moses.client.com.ReqTaskExecutor;
import de.da_sense.moses.client.service.MosesService;
import de.da_sense.moses.client.util.Log;

/**
 * 
 * @author Wladimir Schmidt & Co
 *
 */
public class RequestMulti_Questionnaire {
	
	private JSONObject j;
    private ReqTaskExecutor executor;
    

	public RequestMulti_Questionnaire(ReqTaskExecutor getQuestionnaireExecutor, String id) {
        j = new JSONObject();
        this.executor = getQuestionnaireExecutor;
        try {
            j.put("MESSAGE", "QUESTIONNAIRES_REQUEST");
            j.put("SESSIONID", MosesService.getInstance().getSessionID());
            j.put("APKID", id);
            Log.i("RequestMulti_Questionnaire", "SESSIONID =" + MosesService.getInstance().getSessionID()+ " APKID = " + id);
        } catch (JSONException ex) {
            executor.handleException(ex);
        }
	}

    public void send() {
        Log.d("RequestMulti_Questionnaire", j.toString());
        NetworkJSON task = new NetworkJSON();
        NetworkJSON.APIRequest req;
        req = task.new APIRequest();
        req.request = j;
        req.reqTaskExecutor = this.executor;
        task.execute(req);
    }
}

