/*******************************************************************************
 * Copyright 2013
 * Telecooperation (TK) Lab
 * Technische Universität Darmstadt
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
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

