package de.da_sense.moses.client.abstraction.apks;

import java.util.Date;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import de.da_sense.moses.client.userstudy.Form;
import de.da_sense.moses.client.userstudy.Question;
import de.da_sense.moses.client.userstudy.Survey;
import de.da_sense.moses.client.util.Log;

/**
 * References a past user study application (additional to ExternalApplication, the
 * package name of the installed application must be specified)
 * 
 * @author Sandra Amend, Simon L, Wladimir Schmidt
 * 
 */
public class HistoryExternalApplication extends ExternalApplication {

	private static final String SEPARATOR = "#HEA#";
	private String packageName;
	private boolean questionnaireSent;
	private boolean hasEnded;
	private static final String LOG_TAG = HistoryExternalApplication.class.getName();

	/**
	 * Creates the reference to the external application by specifying the
	 * package name and version number
	 * 
	 * @param packageName
	 *            the name of the package of the application
	 * @param ID
	 *            the moses id of the application
	 */
	private HistoryExternalApplication(String packageName, String ID) {
		super(Integer.valueOf(ID));
		this.packageName = packageName;
	}

	/**
	 * Creates the instance by adapting an already existing {@link ExternalApplication}
	 * reference. This has the advantage of copying already retrieved name and
	 * description over.
	 * 
	 * @param packageName
	 *            the package name of the installed app
	 * @param externalApp
	 *            the preexisting reference that will be adapted
	 * @param questionnaireSent true if the questionnaire was sent
	 * @param hasEnded true if it has ended
	 */
	private HistoryExternalApplication(String packageName,
			ExternalApplication externalApp, boolean questionnaireSent, boolean hasEnded) {

		this(packageName, externalApp.getID());

		if (externalApp.isDescriptionSet()) {
			setDescription(externalApp.getDescription());
		}
		if (externalApp.isNameSet()) {
			setName(externalApp.getName());
		}

		if (externalApp.isNewestVersionSet()) {
			super.setNewestVersion(externalApp.getNewestVersion());
		}
		
		if(externalApp.isStartDateSet()){
            setStartDate(externalApp.getStartDate());
        }
		if(externalApp.isEndDateSet()){
		    setEndDate(externalApp.getEndDate());
		}
		if(externalApp.isApkVersionSet()){
		    setApkVersion(externalApp.getApkVersion());
		}
		
		setQuestionnaireSent(questionnaireSent);
		setHasEnded(hasEnded);
		

		setSurvey(externalApp.getSurvey());
		
	}
	
	@Deprecated
	public HistoryExternalApplication(String packageName,
			ExternalApplication externalApp) {
		this(packageName, externalApp, false,
				(externalApp.getEndDate().compareTo(new Date()) > 0));
	}

	/**
	 * Creates the instance by adapting an already existing {@link InstalledExternalApplication}
	 * reference. This has the advantage of copying already retrieved name and
	 * description over.
	 * 
	 * @param externalApp
	 *            the preexisting reference that will be adapted
	 * @param questionnaireSent true if the questionnaire was sent
	 * @param hasEnded true if it has ended
	 */
	public HistoryExternalApplication(InstalledExternalApplication externalApp, 
			boolean questionnaireSent, boolean hasEnded) {
		this(externalApp.getPackageName(), externalApp, questionnaireSent,
				hasEnded);
	}
	

	/**
	 * General method for showing an alert dialog to the user.
	 * @param baseActivity the base activity for the dialog
	 * @param title the title for the alert dialog
	 * @param msg the message to show in the dialog
	 * @param onClickListener the onClickListener for the button
	 */
	@Deprecated
	protected void showMessageBoxError(Activity baseActivity, String title,
			String msg, DialogInterface.OnClickListener onClickListener) {
		new AlertDialog.Builder(baseActivity).setMessage(msg).setTitle(title)
				.setCancelable(true).setNeutralButton("OK", onClickListener)
				.show();
	}

	/**
	 * @see de.da_sense.moses.client.abstraction.apks.ExternalApplication#toString()
	 */
	@Override
	public String toString() {
		return packageName;
	}

	/**
	 * XXX: Just some quick edit
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object o) {
		if (o instanceof HistoryExternalApplication) {
			if (this.getID() == null)
				return false;
			return this.getID().equals(
					((HistoryExternalApplication) o).getID());
		} else {
			return false;
		}
	}
	
	/**
	 * @see de.da_sense.moses.client.abstraction.apks.ExternalApplication#asOnelineString()
	 */
	@Override
	public String asOnelineString() {
		String oneLine = super.asOnelineString() + SEPARATOR + packageName 
				+ SEPARATOR
				+ Boolean.valueOf(questionnaireSent).toString()
				+ SEPARATOR 
				+ Boolean.valueOf(hasEnded).toString();
		Survey survey = getSurvey();
		if(survey != null){
			JSONObject surveyAsJsonObject = survey.getSurveyAsJSON();
			if(surveyAsJsonObject != null){
				// there is a survey, add JSONObject as one line string
				String jsonAsString = surveyAsJsonObject.toString();
				jsonAsString = jsonAsString.replace("\n", " "); // paranoia
				oneLine = oneLine+SEPARATOR+jsonAsString;
			}
			// now save the answers
			JSONObject answersAsJson = new JSONObject();
			List<Form> forms = survey.getForms();
			for(Form form : forms)
				for(Question question : form.getQuestions()){
					try {
						answersAsJson.put(String.valueOf(question.getId()), question.getAnswer());
					} catch (JSONException e) {
						Log.e(LOG_TAG, e.getMessage());
					}
				}
			oneLine = oneLine + SEPARATOR + answersAsJson.toString().replace("\n", " "); // paranoia 2
		}
		
		Log.d("HEA", "returning one line String:\n" + oneLine);
		return oneLine;
	}

	/**
	 * creates an installed external application from a string (@see
	 * {@link #asOnelineString()})
	 * 
	 * @param s
	 *            the string-encoded installed external application
	 * @return the decoded installed external application
	 */	
	public HistoryExternalApplication(String s) {
		String[] split = s.split(SEPARATOR);
		initializeFromString(split[0]);
		this.packageName = split[1];
		this.questionnaireSent = Boolean.parseBoolean(split[2]);
		this.hasEnded = Boolean.parseBoolean(split[3]);
		// check if it has a Survey
		if(split.length > 4){
			String surveyAsJsonStrong = split[4];
			setQuestionnaire(surveyAsJsonStrong);
		}
		
		// set answers of the survey
		if(split.length > 5){
			String answersAsJsoString = split[5];
			try {
				JSONObject answersAsJsonObject = new JSONObject(answersAsJsoString);
				for(Form form : getSurvey().getForms())
					for(Question question : form.getQuestions()){
						if(answersAsJsonObject.has(String.valueOf(question.getId())))
							question.setAnswer(answersAsJsonObject.getString(String.valueOf(question.getId())));
					}
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
	}

	/**
	 * @see de.da_sense.moses.client.abstraction.apks.ExternalApplication#isDataComplete()
	 */
	@Override
	public boolean isDataComplete() {
		return super.isDataComplete();
	}

	/**
	 * @return true if the questionnaire was sent
	 */
	public boolean isQuestionnaireSent() {
		return questionnaireSent;
	}

	/**
	 * @param questionnaireSent the questionnaireSent to set
	 */
	public void setQuestionnaireSent(boolean questionnaireSent) {
		this.questionnaireSent = questionnaireSent;
	}
	
	/**
	 * Check if the current date is after the end date (user study has ended).
	 * @return true if user study is finished
	 */
	@Deprecated
	public boolean hasEnded() {
		Date today = new Date();
		if (this.getEndDate().compareTo(today) > 0) {
			setHasEnded(true);
			return true;
		}
		setHasEnded(false);
		return false;
	}

	/**
	 * @return the hasEnded
	 */
	public boolean getHasEnded() {
		return hasEnded;
	}

	/**
	 * @param hasEnded the hasEnded to set
	 */
	public void setHasEnded(boolean hasEnded) {
		this.hasEnded = hasEnded;
	}

}
