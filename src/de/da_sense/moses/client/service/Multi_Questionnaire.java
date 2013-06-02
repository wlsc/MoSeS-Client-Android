package de.da_sense.moses.client.service;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import de.da_sense.moses.client.abstraction.apks.InstalledExternalApplicationsManager;
import de.da_sense.moses.client.com.ConnectionParam;
import de.da_sense.moses.client.com.NetworkJSON.BackgroundException;
import de.da_sense.moses.client.com.ReqTaskExecutor;
import de.da_sense.moses.client.com.requests.RequestSendQuestionnaireAnswers;
import de.da_sense.moses.client.com.requests.RequestSingle_Questionnaire;
import de.da_sense.moses.client.util.Log;


/**
 * 
 * @author Wladimir Schmidt & Co
 *
 */
public class Multi_Questionnaire {

	/**
	 * Defining a log tag to this class
	 */
	private static final String TAG = "Multi_Questionnaire";

	/**
	 * Boolean if this Questionnaire has been sent to the Server
	 */
	private Boolean sentToServer = false;

	/**
	 * An array of all single_Questionnaires
	 */
	private Single_Questionnaire[] questionnaires;
	
	/**
	 * The possible types of a question
	 */
	private static final int SINGLE_QUESTION = 1, MULTIPLE_QUESTION = 2,
			OPEN_QUESTION = 3;

	/**
	 * The Separator used to separate Questions inside a Questionnaire-String
	 */
	private static final String QUESTIONNAIRE_SEPARATOR = "#SG#";
	
	/**
	 * The Separator used to separate parts inside a Question-String
	 */
	private static final String QUESTION_SEPARATOR = "#sep#";	
	
	/**
	 * The Separator used to separate different answers inside the answer-String
	 */
	private static final String ANSWER_SEPARATOR = "#SA#";
	
	/**
	 * The ID of the APK
	 */
	private int apkid;
	
	/**
	 * Amount of tries to send this Questionnaire's answers
	 */
	private int triesAnswers = 0;
	
	/**
	 * IDs for the HardcodedQuestionnaires, -1 if not part of
	 * this Multi_Questionnaire
	 */
	private int SUS, Standard1, Standard2, Standard3 = -1;

	/**
	 * Constructor for Multi_Questionnaire. For further explanation on how the method
	 * works in detail see in-code comments.
	 * @param oneLine The String from which to parse the Multi_Questionnaire
	 */
	public Multi_Questionnaire(String oneLine){
		Log.d(TAG, "Multi_Questionnaire() received String " + oneLine);
		try {
			// Parse the string into JSONObject
			JSONObject j = new JSONObject(oneLine);
			// Get the Values for all four Hardcoded Questionnaires
			SUS = j.getInt("SUS");
			Standard1 = j.getInt("STANDARD1");
			Standard2 = j.getInt("STANDARD2");
			Standard3 = j.getInt("STANDARD3");
			// If the int is > 0 it has this kind of questionnaire and
			// the int is the Questionnaire-ID of it
			boolean hasSUS = SUS > 0;
			boolean hasStandard1 = Standard1 > 0;
			boolean hasStandard2 = Standard2 > 0;
			boolean hasStandard3 = Standard3 > 0;
			// Get the Questionnaire IDs of the non-standard Questionnaires
			JSONArray dynamic = (j.getString("QUESTIDS").equals("[]")) ? new JSONArray() : j.getJSONArray("QUESTIDS");
			// Get the APKID
			this.apkid = j.getInt("APKID");
			// Count how many Questionnaires there are in total
			int amountQuestionnaires = (hasSUS) ? 1 : 0;
			amountQuestionnaires = (hasStandard1) ? amountQuestionnaires + 1 : amountQuestionnaires;
			amountQuestionnaires = (hasStandard2) ? amountQuestionnaires + 1 : amountQuestionnaires;
			amountQuestionnaires = (hasStandard3) ? amountQuestionnaires + 1 : amountQuestionnaires;
			amountQuestionnaires = amountQuestionnaires + dynamic.length();
			questionnaires = new Single_Questionnaire[amountQuestionnaires];
			// Create Single_Questionnaire for each standardquestionnaire if it is
			// a part of this and fill them in (Hardcoded Questioncontent)
			int current = 0;
			if(hasSUS){
				questionnaires[current] = new Single_Questionnaire(getSUS());
				current++;
			}
			if(hasStandard1){
				questionnaires[current] = new Single_Questionnaire(getStandard1());
				current++;
			}
			if(hasStandard2){
				questionnaires[current] = new Single_Questionnaire(getStandard2());
				current++;
			}
			if(hasStandard3){
				questionnaires[current] = new Single_Questionnaire(getStandard3());
				current++;
			}
			// Create Single_Questionnaires for the dynamic Questionnaires. These just
			// Contain the Questionnaire id unlike the hardcoded ones.
			if (amountQuestionnaires - current > 0){
				for(int i = 0; i < dynamic.length(); i++){
					questionnaires[i+current] = new Single_Questionnaire(apkid, dynamic.getInt(i));
				}
			}
			// Try to fill in the content for each Single_Questionnaire. For the standard
			// Versions this is easy. They are already initialized with empty answers, so
			// if the oneLineString did not contain any answers for them this does nothing.
			// For the dynamic Versions this first tries if there is content in the
			// oneLineString. In this case content does refer to the complete question with
			// id, type, text, possible answers and current answer. If it is not saved
			// in the String it requests the Questionnaire from the server by identifying it with
			// the Questionnaire ID.
			JSONObject parseMe = new JSONObject();
			for(int i = 0; i < questionnaires.length; i++){
				try {
					parseMe = j.getJSONObject(questionnaires[i].getQuestID());
					questionnaires[i].parseAdditional(parseMe);
				} catch (JSONException e) {
					if (questionnaires[i].getType().equals("dynamic")){
						getContentFromServer(i);
					}
				}
			}
		} catch (JSONException e) {
			Log.d(TAG, "Error while parsing questionnaires from oneLineString");
			e.printStackTrace();
		}
		
	}
	
	/**
	 * This method is called if the i-th Single_Questionnaire
	 * is not a stadardquestionnaire and has no saved answers
	 * @param i
	 */
	private void getContentFromServer(int i) {
		new RequestSingle_Questionnaire(new Single_QuestionnaireExecutor(), String.valueOf(apkid), questionnaires[i].getQuestID()).send();
	}

	/**
	 * Hardcoded JSONObject of an SUS-Questionnaire to
	 * save communication space
	 * @return JSONObject containing all SUS Questions
	 */
	private JSONObject getSUS() {
		JSONObject susJSON = new JSONObject();
		try {
			susJSON.put("APKID", apkid);
			susJSON.put("NAME", "SUS");
			susJSON.put("TYPE", "SUS");
			susJSON.put("Amount", 10);
			susJSON.put("QUESTID", "4");
			susJSON.put("QUESTIONS", 
					SINGLE_QUESTION+ QUESTION_SEPARATOR + "1" + QUESTION_SEPARATOR + "I think that I would like to use this system frequently"+ QUESTION_SEPARATOR + "1" + ANSWER_SEPARATOR + "2" + ANSWER_SEPARATOR + "3" + ANSWER_SEPARATOR + "4" + ANSWER_SEPARATOR + "5"+ QUESTION_SEPARATOR + QUESTIONNAIRE_SEPARATOR + 
					SINGLE_QUESTION+ QUESTION_SEPARATOR + "2" + QUESTION_SEPARATOR + "I found the system unnecessarily complex"+ QUESTION_SEPARATOR + "1" + ANSWER_SEPARATOR + "2" + ANSWER_SEPARATOR + "3" + ANSWER_SEPARATOR + "4" + ANSWER_SEPARATOR + "5"+ QUESTION_SEPARATOR + QUESTIONNAIRE_SEPARATOR + 
					SINGLE_QUESTION+ QUESTION_SEPARATOR + "3" + QUESTION_SEPARATOR + "I thought the system was easy to use"+ QUESTION_SEPARATOR + "1" + ANSWER_SEPARATOR + "2" + ANSWER_SEPARATOR + "3" + ANSWER_SEPARATOR + "4" + ANSWER_SEPARATOR + "5"+ QUESTION_SEPARATOR + QUESTIONNAIRE_SEPARATOR + 
					SINGLE_QUESTION+ QUESTION_SEPARATOR + "4" + QUESTION_SEPARATOR + "I think that I would need the support of a technical person to be able to use this system"+ QUESTION_SEPARATOR + "1" + ANSWER_SEPARATOR + "2" + ANSWER_SEPARATOR + "3" + ANSWER_SEPARATOR + "4" + ANSWER_SEPARATOR + "5"+ QUESTION_SEPARATOR + QUESTIONNAIRE_SEPARATOR + 
					SINGLE_QUESTION+ QUESTION_SEPARATOR + "5" + QUESTION_SEPARATOR + "I found the various functions in this system were well integrated"+ QUESTION_SEPARATOR + "1" + ANSWER_SEPARATOR + "2" + ANSWER_SEPARATOR + "3" + ANSWER_SEPARATOR + "4" + ANSWER_SEPARATOR + "5"+ QUESTION_SEPARATOR + QUESTIONNAIRE_SEPARATOR + 
					SINGLE_QUESTION+ QUESTION_SEPARATOR + "6" + QUESTION_SEPARATOR + "I thought there was too much inconsistency in this system"+ QUESTION_SEPARATOR + "1" + ANSWER_SEPARATOR + "2" + ANSWER_SEPARATOR + "3" + ANSWER_SEPARATOR + "4" + ANSWER_SEPARATOR + "5"+ QUESTION_SEPARATOR + QUESTIONNAIRE_SEPARATOR + 
					SINGLE_QUESTION+ QUESTION_SEPARATOR + "7" + QUESTION_SEPARATOR + "I would imagine that most people would learn to use this system very quickly"+ QUESTION_SEPARATOR + "1" + ANSWER_SEPARATOR + "2" + ANSWER_SEPARATOR + "3" + ANSWER_SEPARATOR + "4" + ANSWER_SEPARATOR + "5"+ QUESTION_SEPARATOR + QUESTIONNAIRE_SEPARATOR + 
					SINGLE_QUESTION+ QUESTION_SEPARATOR + "8" + QUESTION_SEPARATOR + "I found the system very cumbersome to use"+ QUESTION_SEPARATOR + "1" + ANSWER_SEPARATOR + "2" + ANSWER_SEPARATOR + "3" + ANSWER_SEPARATOR + "4" + ANSWER_SEPARATOR + "5"+ QUESTION_SEPARATOR + QUESTIONNAIRE_SEPARATOR + 
					SINGLE_QUESTION + QUESTION_SEPARATOR + "9" + QUESTION_SEPARATOR + "I felt very confident using the system"+ QUESTION_SEPARATOR + "1" + ANSWER_SEPARATOR + "2" + ANSWER_SEPARATOR + "3" + ANSWER_SEPARATOR + "4" + ANSWER_SEPARATOR + "5"+ QUESTION_SEPARATOR + QUESTIONNAIRE_SEPARATOR + 
					SINGLE_QUESTION + QUESTION_SEPARATOR + "10"+ QUESTION_SEPARATOR + "I needed to learn a lot of things before I could get going with this system"+ QUESTION_SEPARATOR + "1" + ANSWER_SEPARATOR + "2" + ANSWER_SEPARATOR + "3" + ANSWER_SEPARATOR + "4" + ANSWER_SEPARATOR + "5"+ QUESTION_SEPARATOR);
		} catch (JSONException e1) {
			Log.d(TAG,
					"Error while adding SUS Questions");
			e1.printStackTrace();
		}
		return susJSON;
	}

	/**
	 * Hardcoded JSONObject of an Standard1-Questionnaire to
	 * save communication space
	 * @return JSONObject containing all Standard1-Questions
	 */
	private JSONObject getStandard1() {
		JSONObject standard1JSON = new JSONObject();
		try {
			standard1JSON.put("APKID", apkid);
			standard1JSON.put("NAME", "Standardfragebogen1");
			standard1JSON.put("TYPE", "STANDARD1");
			standard1JSON.put("Amount", 3);
			standard1JSON.put("QUESTID","1");
			standard1JSON.put("QUESTIONS", SINGLE_QUESTION+ QUESTION_SEPARATOR + "11" + QUESTION_SEPARATOR + "Standard1- question1"+ QUESTION_SEPARATOR + "1" + ANSWER_SEPARATOR + "2" + ANSWER_SEPARATOR + "3"+ QUESTION_SEPARATOR + QUESTIONNAIRE_SEPARATOR + 
					MULTIPLE_QUESTION+ QUESTION_SEPARATOR + "12" + QUESTION_SEPARATOR + "Standard1- question2"+ QUESTION_SEPARATOR + "1" + ANSWER_SEPARATOR + "2" + ANSWER_SEPARATOR + "3"+ QUESTION_SEPARATOR + QUESTIONNAIRE_SEPARATOR + 
					OPEN_QUESTION+ QUESTION_SEPARATOR + "13" + QUESTION_SEPARATOR + "Standard1- question3"+ QUESTION_SEPARATOR + "" + QUESTION_SEPARATOR);
		} catch (JSONException e1) {
			Log.d(TAG,
					"Error while adding Standard1 Questions");
			e1.printStackTrace();
		}
		return standard1JSON;
	}

	/**
	 * Hardcoded JSONObject of an Standard2-Questionnaire to
	 * save communication space
	 * @return JSONObject containing all Standard2-Questions
	 */
	private JSONObject getStandard2() {
		JSONObject standard2JSON = new JSONObject();
		int amountOfQuestions = 0;
		try {
			standard2JSON.put("APKID", apkid);
			standard2JSON.put("NAME", "Standardfragebogen2");
			standard2JSON.put("TYPE", "STANDARD2");
			standard2JSON.put("Amount", amountOfQuestions + 2);
			standard2JSON.put("QUESTID", "2");
			standard2JSON.put("QUESTIONS", 
					SINGLE_QUESTION+ QUESTION_SEPARATOR + "14" + QUESTION_SEPARATOR + "Standard2- question1"+ QUESTION_SEPARATOR + "Yes" + ANSWER_SEPARATOR + "Maybe" + ANSWER_SEPARATOR + "No"+ QUESTION_SEPARATOR + QUESTIONNAIRE_SEPARATOR + 
					OPEN_QUESTION+ QUESTION_SEPARATOR + "15" + QUESTION_SEPARATOR + "Standard2- question2"+ QUESTION_SEPARATOR + " "+ QUESTION_SEPARATOR);
		} catch (JSONException e1) {
			Log.d(TAG,
					"Error while adding Standard2 Questions");
			e1.printStackTrace();
		}
		return standard2JSON;
	}

	/**
	 * Hardcoded JSONObject of an Standard3-Questionnaire to
	 * save communication space
	 * @return JSONObject containing all Standard3-Questions
	 */
	private JSONObject getStandard3() {
		int amountOfQuestions = 0;
		JSONObject standard3JSON = new JSONObject();
		try {
			standard3JSON.put("APKID", apkid);
			standard3JSON.put("NAME", "Standardfragebogen3");
			standard3JSON.put("TYPE", "STANDARD3");
			standard3JSON.put("Amount", amountOfQuestions + 2);
			standard3JSON.put("QUESTID", "3");
			standard3JSON.put("QUESTIONS", 
					MULTIPLE_QUESTION+ QUESTION_SEPARATOR + "16" + QUESTION_SEPARATOR + "Standard3- question1"+ QUESTION_SEPARATOR + "1" + ANSWER_SEPARATOR + "2" + ANSWER_SEPARATOR + "3"+ QUESTION_SEPARATOR + QUESTIONNAIRE_SEPARATOR + 
					OPEN_QUESTION+ QUESTION_SEPARATOR + "17" + QUESTION_SEPARATOR + "Standard3- question2"+ QUESTION_SEPARATOR + " "+ QUESTION_SEPARATOR); 
		} catch (JSONException e1) {
			Log.d(TAG,
					"Error while adding Standard3 Questions");
			e1.printStackTrace();
		}
		return standard3JSON;
	}

	/**
	 * Returns true if the Questionnaire was sent to the server, else false.
	 * 
	 * @return sentToServer
	 */
	public boolean hasBeenSent() {
		return sentToServer;
	}
	
	/**
	 * toString() Method, returns the String representation of an JSONObject containing
	 * all of this Multi_Questionnaires information in order to recreate this instance
	 * from the string.
	 */
	public String toString(){
		JSONObject j = new JSONObject();
		JSONArray questIDS = new JSONArray();
		try {
			j.put("SUS", "-1");
			j.put("STANDARD1", "-1");
			j.put("STANDARD2", "-1");
			j.put("STANDARD3", "-1");
		} catch (JSONException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		for (int i = 0; i < questionnaires.length; i++){
			String type = questionnaires[i].getType();
			if (type == "dynamic"){
				String ID = questionnaires[i].getQuestID();
				questIDS.put(ID);
				try {
					j.put(ID, questionnaires[i].getContent());
				} catch (JSONException e) {
					Log.d(TAG, "Error in toString while putting dynamic questionnaire[" + i +"]");
					e.printStackTrace();
				}
			} else if (type == "SUS") {
				String ID = questionnaires[i].getQuestID();
				try {
					j.put("SUS", ID);				
					j.put(ID, questionnaires[i].getAnswers());
				} catch (JSONException e) {
					Log.d(TAG, "Error in toString while putting SUS questionnaire");
					e.printStackTrace();
				}
			} else if (type == "STANDARD1"){
				String ID = questionnaires[i].getQuestID();
				try {
					j.put("STANDARD1", ID);				
					j.put(ID, questionnaires[i].getAnswers());
				} catch (JSONException e) {
					Log.d(TAG, "Error in toString while putting STANDARD1 questionnaire");
					e.printStackTrace();
				}				
			} else if (type == "STANDARD2"){
				String ID = questionnaires[i].getQuestID();
				try {
					j.put("STANDARD2", ID);				
					j.put(ID, questionnaires[i].getAnswers());
				} catch (JSONException e) {
					Log.d(TAG, "Error in toString while putting STANDARD2 questionnaire");
					e.printStackTrace();
				}	
			} else if (type == "STANDARD3"){
				String ID = questionnaires[i].getQuestID();
				try {
					j.put("STANDARD3", ID);				
					j.put(ID, questionnaires[i].getAnswers());
				} catch (JSONException e) {
					Log.d(TAG, "Error in toString while putting STANDARD3 questionnaire");
					e.printStackTrace();
				}	
			} else {
				Log.d(TAG, "Error in toString Method, Single_Questionnaire had no known type");
			}
				
		}
		try {
			j.put("QUESTIDS", questIDS.toString());
		} catch (JSONException e) {
			Log.d(TAG, "Error while putting the QUESTIDS");
			e.printStackTrace();
		}
		return j.toString();
	}

	/**
	 * @return the apkid
	 */
	public int getApkid() {
		return apkid;
	}

	/**
	 * @param apkid the apkid to set
	 */
	public void setApkid(int apkid) {
		this.apkid = apkid;
	}

	/**
	 * Returns the Single_Questionnaire with index i
	 * @param i The Index
	 * @return questionnaires[i]
	 */
	public Single_Questionnaire getSingleQuestionnaire(int i) {
		return questionnaires[i];
	}
	/**
	 * Returns the Single_Questionnaire with ID
	 * @param i The ID
	 */
	private void getContentFromServer(String questionnaireID) {
		int index = getIndexByID(questionnaireID);
		if (index >= 0) getSingleQuestionnaire(index);
	}
	
	/**
	 * Returns he index of a questionnaire with ID
	 * @param ID
	 * @return index
	 */
	private int getIndexByID(String ID){
		for (int i = 0; i < questionnaires.length; i++){
			if (questionnaires[i].getQuestID().equals(ID))
				return i;
		}
		return -1;
	}

	/**
	 * Returns the last index of questionnaires
	 * @return The last Index
	 */
	public int getLastIndex() {
		return questionnaires.length -1;
	}
	
	/**
	 * Sets content of the single_Questionnaire with this ID 
	 * to the given string
	 * @param questionnaireID
	 */
	private void setSingle_Questionnaire(String questionnaireID, String content) {
		int index = getIndexByID(questionnaireID);
		if (index >= 0)
			try {
				questionnaires[index].parseAdditional(new JSONObject(content));
			} catch (JSONException e) {
				Log.d(TAG, "Error while parsing additional Content");
				e.printStackTrace();
			}
	}		

	/**
	 * Sets if this Multi_Questionnaire was successfully sent to
	 * the server
	 * @param b
	 */
	public void setSentToServer(boolean b) {
		sentToServer = b;		
	}
	

	/**
	 * Sends this Multi_Questionnaire's answers to the server. If the sessionID
	 * is invalid it will try 2 more times, else it fails.
	 */
	public void sendAnswersToServer() {
		triesAnswers = 0;
		retrySendAnswersToServer();
	}
	
	/**
	 * Retries to send the answers.
	 */
	private void retrySendAnswersToServer(){
		triesAnswers++;		
		if (triesAnswers > 3){
			//
		} else {
			StringBuffer answers = new StringBuffer();
			for (Single_Questionnaire q : questionnaires){
				answers.append(q.getAnswersToSend());
			}
			answers.substring(0, answers.lastIndexOf(QUESTIONNAIRE_SEPARATOR));
			new RequestSendQuestionnaireAnswers(new SendQuestionnaireAnswers(), MosesService.getInstance().getSessionID(), apkid, answers.toString()).send();
			}
		}
	
	/**
	 * Implementation of ReqTaskExecutor to handle the return of the server
	 * for a SetQuestionnaireRequest
	 */
	private class SendQuestionnaireAnswers implements ReqTaskExecutor {
		@Override
		public void handleException(Exception e) {
			Log.d("SendQuestionnaireAnswers", "Failed because of an exception: " + e.getMessage());
		}

		@Override
		public void postExecution(String s) {
			try {
				JSONObject j = new JSONObject(s);
				Log.d("SendQuestionnaireAnswers", "postExecution return was: "+s);
				String APKID = j.getString("APKID");
				String Status = j.getString("STATUS");
				if (Status.equals("SUCCESS")){
					Log.d("SendQuestionnaireAnswers", "Successfully set the answers");
					Log.d("SendQuestionnaireAnswers", "IEA Manager = "+InstalledExternalApplicationsManager.getInstance()+" IEA = " + InstalledExternalApplicationsManager.getInstance().getAppForId(APKID).asOnelineString());
					InstalledExternalApplicationsManager.getInstance().getAppForId(APKID).getMultiQuestionnaire().setSentToServer(true);
					InstalledExternalApplicationsManager.getInstance().checkAndAddHistory(InstalledExternalApplicationsManager.getInstance().getAppForId(APKID));
					InstalledExternalApplicationsManager.getInstance().forgetExternalApplication(InstalledExternalApplicationsManager.getInstance().getAppForId(APKID));
				} else if (Status.equals("FAILURE_INVALID_ANSWERS")){
					Log.d("SendQuestionnaireAnswers", "Failed to set the answers, because of invalid answers");
					// TODO Handle wrong answers
				} else if (Status.equals("FAILURE_INVALID_APKID")){
					Log.d("SendQuestionnaireAnswers", "Failed to set the answers, because of invalid apk");
					// TODO Handle wrong APKID
				} else if (Status.equals("FAILURE_INVALID_SESSION")){
					Log.d("SendQuestionnaireAnswers", "Failed to set the answers, because of invalid Session ID. Trying again");
					// Retries to send the answers
					MosesService.getInstance().login();
					InstalledExternalApplicationsManager.getInstance().getAppForId(APKID).getMultiQuestionnaire().retrySendAnswersToServer();
				}
			} catch (JSONException e) {
				this.handleException(e);
			}
		}

		@Override
		public void updateExecution(BackgroundException c) {
			if (c.c == ConnectionParam.EXCEPTION) {
				handleException(c.e);
			}
		}
	}	
	
	/**
	 * Implementation of ReqTaskExecutor to handle the return of the server
	 * for a SetQuestionnaireRequest
	 */
	private class Single_QuestionnaireExecutor implements ReqTaskExecutor {
		@Override
		public void handleException(Exception e) {
			Log.d("Single_QuestionnaireExecutor", "Failed because of an exception: " + e.getMessage());
		}

		@Override
		public void postExecution(String s) {
			try {
				JSONObject j = new JSONObject(s);
				Log.d("Single_QuestionnaireExecutor", "postExecution return was: "+s);
				String APKID = j.getString("APKID");
				String Status = j.getString("STATUS");				
				String questionnaireID = (j.getString("QUESTID"));
				if (Status.equals("SUCCESS")){
					String questions = j.getString("QUESTIONS");
					Log.d("Single_QuestionnaireExecutor", "Successfully received the contents");
					InstalledExternalApplicationsManager.getInstance().getAppForId(APKID).getMultiQuestionnaire().setSingle_Questionnaire(questionnaireID, questions);
				} else if (Status.equals("FAILURE_NO_QUESTION_FOUND")){
					Log.d("Single_QuestionnaireExecutor", "Failed to receive the questionnaire, because of no question found");
					// TODO Handle wrong answers
				} else if (Status.equals("FAILURE_NO_QUESTIONNAIRE_FOUND")){
					Log.d("Single_QuestionnaireExecutor", "Failed to receive the questionnaire, because of no questionnaire found");
					// TODO Handle wrong answers
				} else if (Status.equals("INVALID_QUESTID")){
					Log.d("Single_QuestionnaireExecutor", "Failed to receive the questionnaire, because of invalid questid");
					// TODO Handle wrong APKID
				} else if (Status.equals("INVALID_SESSION")){
					Log.d("Single_QuestionnaireExecutor", "Failed to set the answersreceive the questionnaire, because of invalid Session ID. Trying again");
					// Retries to send the answers
					MosesService.getInstance().login();
					InstalledExternalApplicationsManager.getInstance().getAppForId(APKID).getMultiQuestionnaire().getContentFromServer(questionnaireID);
				}
			} catch (JSONException e) {
				this.handleException(e);
			}
		}

		@Override
		public void updateExecution(BackgroundException c) {
			if (c.c == ConnectionParam.EXCEPTION) {
				handleException(c.e);
			}
		}
	}
}