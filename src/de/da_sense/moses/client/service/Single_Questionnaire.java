package de.da_sense.moses.client.service;

import org.json.JSONException;
import org.json.JSONObject;

import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import de.da_sense.moses.client.service.helpers.Question;
import de.da_sense.moses.client.util.Log;

public class Single_Questionnaire {

	/**
	 * Defining a log tag to this class
	 */
	private static final String TAG = "Single_Questionnaire";

	/**
	 * An array of all questions
	 */
	private Question[] questions;

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
	 * The id of the APK
	 */
	private int apkid;
	
	/**
	 * The id of the APK
	 */
	private int questid;
	
	/**
	 * Type of this Questionnaire
	 */
	private String type;
	
	/**
	 * The Name of the Questionnaire;
	 */
	private String questionnaireName;

	/**
	 * Constructor for a Single_Questionnaire with a JSONObject
	 * @param questJSON
	 */
	public Single_Questionnaire(JSONObject questJSON) {
		try {
			this.type = questJSON.getString("TYPE");
			this.apkid = questJSON.getInt("APKID");
			this.questid = questJSON.getInt("QUESTID");
			this.questionnaireName = questJSON.getString("NAME");
			String questionString[] = questJSON.getString("QUESTIONS").split(QUESTIONNAIRE_SEPARATOR);
			questions = new Question[questionString.length];
			for (int i = 0; i < questionString.length; i++) {
				questions[i] = new Question(questionString[i]);
			}
		} catch (JSONException e1) {
			Log.d(TAG,
					"Error while parsing JSONObject");
			e1.printStackTrace();
		}
	}

	/**
	 * Constructor for a Single_Questionnaire with the APKID and QUESTID
	 * @param apkid
	 * @param questid
	 */
	public Single_Questionnaire(int apkid, int questid) {
		this.type = "dynamic";
		this.questid = questid;
		this.apkid = apkid;
	}

	
	/**
	 * Retrieve the answers from the layout and directly set them in the
	 * questions.
	 * 
	 * @param layout
	 *            the layout where the answers should be found
	 */
	public void setAnswers(LinearLayout layout) {
		for (int currentID = 0; currentID < questions.length; currentID++) {
			String answer = "";
			String tag = "Questionnaire" + currentID;
			switch (questions[currentID].getType()) {
			case (SINGLE_QUESTION): {
				RadioGroup rg = ((RadioGroup) layout.findViewWithTag(tag));
				for (int i = 0; i < rg.getChildCount(); i++) {
					if (((RadioButton) rg.getChildAt(i)).isChecked()) {
						answer = (String) ((RadioButton) rg.getChildAt(i))
								.getText();
						i = rg.getChildCount();
					}
				}
				break;
			}
			case (MULTIPLE_QUESTION): {
				for (int i = 0; i < questions[currentID].getPossibleAnswers().length; i++) {
					CheckBox cb = ((CheckBox) layout.findViewWithTag(tag
							+ "Answer" + i));
					answer = cb.isChecked() ? answer.concat(ANSWER_SEPARATOR + cb.getText())
							: answer;
				}
				answer = (answer.length() == 0) ? answer : answer.substring(ANSWER_SEPARATOR.length());
				break;
			}
			case (OPEN_QUESTION): {
				EditText et = ((EditText) layout.findViewWithTag(tag));
				answer = et.getText().toString();
				break;
			}
			default:
				break;
			}

			questions[currentID].setAnswer(answer);
		}
	}

	

	/**
	 * Get the questions from this questionnaire.
	 * 
	 * @return this questionnaires questions
	 */
	public Question[] getQuestions() {
		return questions;
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
	 * Parses additional information into this Questionnaire. Additional
	 * information refers to the answers for a standard questionnaire or
	 * the whole content for a dynamic questionnaire
	 * @param content
	 */
	public void parseAdditional(JSONObject content) {
		if (type.equals("dynamic")) {
			setContent(content);
		} else {
			setStandardAnswers(content);
		}
	}

	/**
	 * Sets the content of a questionnaire (Used for dynamic questionnaires
	 * which get initialized only with their ID).
	 * @param content
	 */
	private void setContent(JSONObject content) {
	try {
		this.type = content.getString("TYPE");
		this.apkid = content.getInt("APKID");
		this.questionnaireName = content.getString("NAME");		
		String questionString[] = content.getString("QUESTIONS").split(QUESTIONNAIRE_SEPARATOR);
		questions = new Question[questionString.length];
		for (int i = 0; i < questionString.length; i++) {
			questions[i] = new Question(questionString[i]);
		}
	} catch (JSONException e1) {
		Log.d(TAG,
				"Error while parsing content for a dynamic Questionnaire");
		e1.printStackTrace();
	}
	}

	/**
	 * Sets the answers for a Questionnaire. Used for standard questionnaires
	 * which get initialized with empty answers.
	 * @param content
	 */
	private void setStandardAnswers(JSONObject content) {
	try {
		String questionString[] = content.getString("QUESTIONS").split(QUESTIONNAIRE_SEPARATOR);
		questions = new Question[questionString.length];
		for (int i = 0; i < questionString.length; i++) {
			questions[i].setAnswer(questionString[i]);
		}
	} catch (JSONException e1) {
		Log.d(TAG,
				"Error while parsing answers for a standard Questionnaire");
		e1.printStackTrace();
	}
	}

	/**
	 * Returns the QuestID
	 * @return questid
	 */
	public String getQuestID() {
		return String.valueOf(questid);
	}

	/**
	 * Returns if this is a dynamic Questionnaire
	 * @return isDynamic
	 */
	public String getType() {
		return type;
	}

	/**
	 * Returns a string of all answers in the order of the index in the
	 * questionnaire separated by QUESTION_SEPARATOR
	 * @return String s
	 */
	public String getAnswers() {
		StringBuffer s = new StringBuffer();
		s.append(questions[0].getAnswer());
		for (int i = 0; i < questions.length ; i++){
			s.append(QUESTION_SEPARATOR).append(questions[i].getAnswer());		
		}
		return s.toString();
	}

	/**
	 * Returns a string of all content for a questionnaire
	 * @return
	 */
	public String getContent() {
		JSONObject j = new JSONObject();
		StringBuffer questionString = new StringBuffer();
		questionString.append(questions[0].toString());
		for (int i = 1; i < questions.length ; i++){
			questionString.append(QUESTIONNAIRE_SEPARATOR).append(questions[i].toString());
		}
		try {
			j.put("TYPE", type);
			j.put("QUESTID", questid);
			j.put("NAME", questionnaireName);
			j.put("QUESTIONS", questionString);
		} catch (JSONException e) {
			Log.d(TAG, "Error while putting content in getContent()");
			e.printStackTrace();
		}
		return j.toString();
	}

	/**
	 * Returns a String of qid+QUESTION_SEPARATOR+answer for all Question in
	 * questions[] separated by QUESTIONNAIRE_SEPARATOR
	 * @return
	 */
	public String getAnswersToSend(){
		StringBuffer s = new StringBuffer();
		for (int i = 0; i < questions.length ; i++){
			s.append(questions[i].getQID()).append(QUESTION_SEPARATOR).append(questions[i].getAnswer()).append(QUESTIONNAIRE_SEPARATOR);
		}
		return s.toString();
	}
}