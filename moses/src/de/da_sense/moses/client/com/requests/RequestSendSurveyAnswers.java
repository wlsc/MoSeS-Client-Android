package de.da_sense.moses.client.com.requests;

import org.json.JSONException;
import org.json.JSONObject;

import de.da_sense.moses.client.abstraction.apks.InstalledExternalApplicationsManager;
import de.da_sense.moses.client.com.NetworkJSON;
import de.da_sense.moses.client.com.ReqTaskExecutor;
import de.da_sense.moses.client.userstudy.Form;
import de.da_sense.moses.client.userstudy.Question;
import de.da_sense.moses.client.util.Log;

/**
 * This class is used for answering a questionnaire. It provides some basic
 * methods for this
 * 
 * @author Ibrahim Alyahya
 * @author Zijad Maksuti
 * 
 */
public class  RequestSendSurveyAnswers{
	
	private JSONObject j;
    private ReqTaskExecutor e;
    private static final String LOG_TAG = RequestSendSurveyAnswers.class.getName();

    /**
     * Constructs a new {@link RequestSendSurveyAnswers}.
     * @param e the executor
     * @param sessionID the sesion ID
     * @param apkID apk for which answers are sent
     */
    public RequestSendSurveyAnswers(ReqTaskExecutor e, String sessionID, String apkID) {
        j = new JSONObject();
        this.e = e;
        try {
            j.put("MESSAGE", "SURVEY_RESULT");
            j.put("SESSIONID", sessionID);
            // setting answers
            for(Form form : InstalledExternalApplicationsManager.getInstance().getAppForId(apkID).getSurvey().getForms())
            	for(Question question : form.getQuestions())
            		j.put(String.valueOf(question.getId()), question.getAnswer());
        } catch (JSONException ex) {
            e.handleException(ex);
        }
    }

    public void send() {
        Log.d(LOG_TAG, j.toString());
        NetworkJSON task = new NetworkJSON();
        NetworkJSON.APIRequest req;
        req = task.new APIRequest();
        req.request = j;
        req.reqTaskExecutor = this.e;
        task.execute(req);
    }
}
