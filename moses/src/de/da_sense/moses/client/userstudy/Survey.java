
package de.da_sense.moses.client.userstudy;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.os.Bundle;
import android.util.Log;

/**
 * This class represents a survey. A survey consists of one ore more {@link Form} instances.
 * 
 * @author Zijad Maksuti
 * @author Wladimir Schmidt
 *
 */
public class Survey extends HasID implements IHasTitle{
	
	private List<Form> mForms;
	
	private String mTitle;
	
	private static final String LOG_TAG = Survey.class.getName();
	
	/**
	 * A constant to be used when passing an id of a {@link Survey} through
	 * a communication channel. For example, through a {@link Bundle}.
	 */
	public static final String KEY_SURVEY_ID = "key_survey_id";
	
	/**
	 * This method creates a survey with all its underlying children from the consumed {@link JSONObject}.
	 * This constructor assumes that the consumed object has the same structure as provided by the server.
	 * @see <a href="https://github.com/ischweizer/MoSeS/wiki/Communication-API-between-MoSeS-Server-and-MoSeS-Clients"> protocol specifications</a>
	 * @param j json object provided from the server
	 */
	public Survey(JSONObject jsonObject) {
		try {
			JSONObject surveyObject = jsonObject.getJSONObject("SURVEY");
			JSONArray names = surveyObject.names();
			mForms = new ArrayList<Form>();
			for(int i=0; i<names.length(); i++){
				String something = names.getString(i);
				if(something.equals("SURVEY_ID"))
					setId(surveyObject.getInt(something));
				else
					if(something.equals("TITLE"))
						setTitle(surveyObject.getString(something));
					else{
						// no SURVEY_ID, no TITLE, it must be an id of a Form
						Form aForm = new Form();
						aForm.setId(Integer.parseInt(something));
						JSONObject formObject = surveyObject.getJSONObject(something);
						JSONArray namesInForm = formObject.names();
						List<Question> questions = new ArrayList<Question>();
						for(int j=0; j<namesInForm.length(); j++){
							String somethingInForm = namesInForm.getString(j);
							if(somethingInForm.equals("FORM_TITLE"))
								aForm.setTitle(formObject.getString(somethingInForm));
							else{
								// it is not FORM_TITLE, it must be id of a question
								Question aQuestion = new Question();
								aQuestion.setId(Integer.parseInt(somethingInForm));
								JSONObject questionObject = formObject.getJSONObject(somethingInForm);
								JSONArray namesInQuestion = questionObject.names();
								List<PossibleAnswer> possibleAnswers = new ArrayList<PossibleAnswer>();
								for(int k=0; k<namesInQuestion.length(); k++){
									String somethingInQuestion = namesInQuestion.getString(k);
									if(somethingInQuestion.equals("QUESTION_TYPE"))
										aQuestion.setType(questionObject.getInt(somethingInQuestion));
									else
										if(somethingInQuestion.equals("QUESTION_TITLE"))
											aQuestion.setTitle(questionObject.getString(somethingInQuestion));
										else{
											// it is not QUESTION_TYPE nor QUESTION_TITLE, it must be an id of a possible answer
											PossibleAnswer aPossibleAnswer = new PossibleAnswer();
											aPossibleAnswer.setId(Integer.parseInt(somethingInQuestion));
											JSONObject possibleAnswerObject = questionObject.getJSONObject(somethingInQuestion);
											aPossibleAnswer.setTitle(possibleAnswerObject.getString("POSSIBLE_ANSWER_TITLE"));
											possibleAnswers.add(aPossibleAnswer);
										}
								}
								aQuestion.setPossibleAnswers(possibleAnswers);
								questions.add(aQuestion);
							}
						}
						aForm.setQuestions(questions);
						mForms.add(aForm);
					}
			}
			Log.i(LOG_TAG, "Survey() successfully parsed JSONObject");
			}
		catch (JSONException e) {
			Log.e(LOG_TAG, e.getMessage());
		}
	}

	/**
	 * @return the mForms
	 */
	public List<Form> getForms() {
		return mForms;
	}

	/**
	 * @param mForms the mForms to set
	 */
	public void setForms(List<Form> mForms) {
		this.mForms = mForms;
	}

	@Override
	public String setTitle(String title) {
		String oldTitle = this.mTitle;
		this.mTitle = title;
		return oldTitle;
	}

	@Override
	public String getTitle() {
		return this.mTitle;
	}

	/**
	 * Returns true if and only if the results of this survey have be sent back to server.
	 * @return true if the results of this survey have been sent back to server, false otherwise
	 */
	public boolean hasBeenSent() {
		// TODO IMPLEMENT ME!
		return false;
	}
	
	/**
	 * Returns {@link Form} instance attached to this {@link Survey}.
	 * @param formID the ID of the form to be returned
	 * @return the form with the specified formID or null if this survey does not
	 * contain a form with the specified formID
	 */
	public Form getForm(int formID){
		for(Form form : mForms)
			if(form.getId() == formID)
				return form;
		return null;
	}
}
