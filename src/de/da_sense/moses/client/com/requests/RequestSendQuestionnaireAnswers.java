package de.da_sense.moses.client.com.requests;

import org.json.JSONException;
import org.json.JSONObject;

import de.da_sense.moses.client.com.NetworkJSON;
import de.da_sense.moses.client.com.ReqTaskExecutor;
import de.da_sense.moses.client.util.Log;

/**
 * This class is used for answering a questionnaire. It provides some basic
 * methods for this
 * 
 * @author Ibrahim Alyahya
 * 
 */
public class  RequestSendQuestionnaireAnswers{
	
	private JSONObject j;
    private ReqTaskExecutor e;

    public RequestSendQuestionnaireAnswers(ReqTaskExecutor e, String sessionID, Integer apkid, String answerString) {
        j = new JSONObject();
        this.e = e;
        try {
            j.put("MESSAGE", "ANSWER_QUESTIONNAIRE");
            j.put("SESSIONID", sessionID);
            j.put("APKID", apkid);
            j.put("ANSWERS", answerString);
            Log.i("RequestSendQuestionnaireAnswers", "SESSIONID =" + sessionID+ " APKID = " + apkid + " ANSWERS = " + answerString);
        } catch (JSONException ex) {
            e.handleException(ex);
        }
    }

    public void send() {
        Log.d("RequestSendQuestionnaireAnswers", j.toString());
        NetworkJSON task = new NetworkJSON();
        NetworkJSON.APIRequest req;
        req = task.new APIRequest();
        req.request = j;
        req.reqTaskExecutor = this.e;
        task.execute(req);
    }
}
