package de.da_sense.moses.client.abstraction.apks;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Pattern;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import de.da_sense.moses.client.R;
import de.da_sense.moses.client.com.ConnectionParam;
import de.da_sense.moses.client.com.NetworkJSON.BackgroundException;
import de.da_sense.moses.client.com.ReqTaskExecutor;
import de.da_sense.moses.client.com.requests.RequestMulti_Questionnaire;
import de.da_sense.moses.client.service.MosesService;
import de.da_sense.moses.client.service.Multi_Questionnaire;
import de.da_sense.moses.client.util.Log;

/**
 * Reference to an application on the server, referenced by it's MoSeS id
 * 
 * @author Simon L, Wladimir Schmidt
 * 
 */
public class ExternalApplication {
	/** tag for the version */
	private static final String TAG_NEWESTVERSION = "[newestversion]";
	/** tag for the description */
	private static final String TAG_DESCRIPTION = "[description]";
	/** tag for the name */
	private static final String TAG_NAME = "[name]";
	/** tag for the sensors */
	private static final String TAG_SENSORS = "[sensors]";
	/** tag for start date */
	private static final String TAG_STARTDATE = "[startdate]";
	/** tag for end date */
	private static final String TAG_ENDDATE = "[enddate]";
	/** tag for apk version */
	private static final String TAG_APKVERSION = "[apkversion]";
	/** tag for questionnaire */
	private static final String TAG_QUESTIONNAIRE = "[questionnaire]";
	/** tag for the couldHaveQuest */
	private static final String TAG_HASQUEST = "[hasQuest]";
	/** tag for the separator */
	private static final String SEPARATOR = "#EA#";
	
	/**
	 * Boolean if the enddate is reached
	 */
	private Boolean endDateReached = false;
	
	public Boolean getEndDateReached() {
		return endDateReached;
	}

	public void setEndDateReached(Boolean endDateReached) {
		this.endDateReached = endDateReached;
	}

	/**
	 * Boolean if this External Application can contain a Questionnaire
	 */
	private Boolean apkHasQuestOnServer = false; 

	/**
	 * Boolean if this External Application currently contains a Questionnaire
	 */
	private Boolean apkHasQuestLocal = false;
	
	/** String containing the id */
	private String ID;

	// lazy loading variables for non-defining attributes
	/** the user study name */
	private String name;
	/** the user study description */
	private String description;
	/** the newest version of the user study / apk */
	private String newestVersion = "0";
	/** list for the sensor ids */
	private ArrayList<Integer> sensors = null;
	/** the start date of this user study */
	private Date startDate;
	/** the end date of this user study */
	private Date endDate;
	/** the apk version of this user study */
	private String apkVersion;
	/** the questionnaire of this user study */
	private Multi_Questionnaire questionnaire;

	/**
	 * Gets the ID
	 * 
	 * @return ID
	 */
	public String getID() {
		return ID;
	}
	
	public void setID(String id){
		this.ID = id;
	}

	/**
	 * Creates a reference to an external application, specifying its ID
	 * 
	 * @param ID
	 *            the id in the MoSeS database
	 */
	public ExternalApplication(int ID) {
		Log.d("ExternalApplication", "from ID");
		this.ID = String.valueOf(ID);
	}
	
	public ExternalApplication(){
	}

	/**
	 * sets the name of the application
	 * 
	 * @param name
	 *            the name
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * sets the description of the application
	 * 
	 * @param description
	 *            the description
	 */
	public void setDescription(String description) {
		this.description = description;
	}

	/**
	 * Returns the name of this external application. If the name was not set,
	 * or retrieved yet, this will be done now. In this case, this method should
	 * not be called from the UI thread (method blocks).
	 * 
	 * @return
	 */
	public String getName() {
		if (name != null) {
			return name;
		} else {
			return getGenericName();
		}
	}

	/**
	 * retrieves the name of this application from the server in case it was not
	 * set yet.
	 * 
	 * @return the name of the application
	 */
	private String getGenericName() {
		return MosesService.getInstance().getApplicationContext().getString(R.string.loadingName, getID());
	}

	/**
	 * Returns the description of this external application. If the description
	 * was not set, or retrieved yet, this will be done now. In this case, this
	 * method should not be called from the UI thread (method blocks).
	 * 
	 * @return
	 */
	public String getDescription() {
		if (description != null) {
			return description;
		} else {
			return MosesService.getInstance().getApplicationContext().getString(R.string.loadingDescription);
		}
	}

	/**
	 * Returns the String newestVersion
	 * 
	 * @return newestVersion
	 */
	public String getNewestVersion() {
		return newestVersion;
	}

	/**
	 * Returns the ArrayList<Integer> of Sensors
	 * 
	 * @return the sensors
	 */
	public ArrayList<Integer> getSensors() {
		return sensors;
	}

	/**
	 * Sets the sensors
	 * 
	 * @param sensors
	 */
	public void setSensors(Collection<Integer> sensors) {
		if (sensors == null) {
			this.sensors = null;
		} else {
			this.sensors = new ArrayList<Integer>(sensors);
		}
	}

	/**
	 * Returns if sensors are not null
	 * 
	 * @return boolean - sensors != null
	 */
	public boolean isSensorsSet() {
		return sensors != null;
	}

	/**
	 * Sets the newestVersion
	 * 
	 * @param newestVersion
	 *            The version to set
	 */
	public void setNewestVersion(String newestVersion) {
		this.newestVersion = newestVersion;
	}

	/**
	 * Returns if newestVersion is not null
	 * 
	 * @return boolean - newestVersion != null
	 */
	public boolean isNewestVersionSet() {
		return newestVersion != null;
	}

	/**
	 * @return whether the description of the application was already
	 *         retrieved/set
	 */
	public boolean isDescriptionSet() {
		return description != null;
	}

	/**
	 * @return whether the name of the application was already retrieved/set
	 */
	public boolean isNameSet() {
		return name != null;
	}

	/**
	 * To set the start date of this user study
	 * 
	 * @param startDate
	 *            the date to set
	 */
	public void setStartDate(String startDate) {
		this.startDate = convertStringDate(startDate);
	}

	/**
	 * To set the start date of this user study
	 * 
	 * @param startDate
	 *            the date to set
	 */
	public void setStartDate(Date startDate) {
		this.startDate = startDate;
	}

	/**
	 * @return the start date of this user study
	 */

	public Date getStartDate() {
		return startDate;
	}

	/**
	 * @return the date as a localized String
	 */
	public String getStartDateAsString() {
		String date = null;
		if (startDate != null) {
			DateFormat format = SimpleDateFormat.getDateInstance();
			date = format.format(startDate);
		} else {
			date = "not set";
		}
		return date;
	}

	/**
	 * @return the date as a standard String
	 */
	public String getStartDateAsStandardString() {
		String date = null;
		if (startDate != null) {
			DateFormat format = new SimpleDateFormat("yyyy-MM-dd");
			date = format.format(startDate);
		} else {
			date = MosesService.getInstance().getApplicationContext().getString(R.string.not_set);
		}
		return date;
	}

	/**
	 * whether the start date of this user study is set
	 */
	public boolean isStartDateSet() {
		return startDate != null;
	}

	/**
	 * To set the end date of this user study
	 * 
	 * @param endDate
	 *            the date to set
	 */
	public void setEndDate(String endDate) {
		this.endDate = convertStringDate(endDate);
	}

	/**
	 * To set the end date of this user study
	 * 
	 * @param endDate
	 *            the date to set
	 */
	public void setEndDate(Date endDate) {
		this.endDate = endDate;
	}

	/**
	 * @return the end date of this user study
	 */
	public Date getEndDate() {
		return endDate;
	}

	/**
	 * @return the date as a localized String
	 */
	public String getEndDateAsString() {
		String date = null;
		if (startDate != null) {
			DateFormat format = SimpleDateFormat.getDateInstance();
			date = format.format(endDate);
		} else {
			date = MosesService.getInstance().getApplicationContext().getString(R.string.not_set);
		}
		return date;
	}

	/**
	 * @return the date as a standard String
	 */
	public String getEndDateAsStandardString() {
		String date = null;
		if (startDate != null) {
			DateFormat format = new SimpleDateFormat("yyyy-MM-dd");
			date = format.format(endDate);
		} else {
			date = MosesService.getInstance().getApplicationContext().getString(R.string.not_set);
		}
		return date;
	}

	/**
	 * whether the end date of this user study is set
	 */
	public boolean isEndDateSet() {
		return endDate != null;
	}

	/**
	 * @return the apk version of this user study
	 */
	public String getApkVersion() {
		return apkVersion;
	}

	/**
	 * To set the apk version of this user study
	 * 
	 * @param apkVersion
	 *            the apk version
	 */
	public void setApkVersion(String apkVersion) {
		this.apkVersion = apkVersion;
	}

	/**
	 * whether the apk version is set
	 */
	public boolean isApkVersionSet() {
		return apkVersion != null;
	}

	/**
	 * setter for questionnaire
	 * 
	 * @param questionnaire
	 *            the quesitonnaire to set
	 */
	private void setQuestionnaire(Multi_Questionnaire questionnaire) {
		if (apkHasQuestOnServer && !apkHasQuestLocal)
		this.questionnaire = questionnaire;
	}

	/**
	 * Set the questionnaire from a string.
	 * 
	 * @param questAsString
	 */
	public void setQuestionnaire(String questAsString) {
		if (apkHasQuestOnServer && !apkHasQuestLocal)
		setQuestionnaire(new Multi_Questionnaire(questAsString));
		apkHasQuestLocal = true;
	}

	/**
	 * Getting questionnaire with string format
	 * 
	 * @return string that represents the questionnaire of this application
	 *         (user study)
	 */
	private String getStringQuestionnaire() {
		return questionnaire.toString();
	}

	/**
	 * Getter for questionnaire
	 * 
	 * @return the questionnaire of this application (user study)
	 */
	public Multi_Questionnaire getMultiQuestionnaire() {
		return questionnaire;
	}

	/**
	 * Checks if the external application has a questionnaire.
	 * 
	 * @return true if the external app contains a questionnaire
	 */
	public boolean hasQuestionnaire() {
		return (apkHasQuestLocal);
	}
	
	/**
	 * 
	 * @return
	 */
	public Boolean getApkHasQuestOnServer() {
		return apkHasQuestOnServer;
	}

	/**
	 * 
	 * @param apkHasQuestOnServer
	 */
	public void setApkHasQuestOnServer(Boolean apkHasQuestOnServer) {
		Log.d("ExternalApplication", "apkHasQuestOnServer set to " + apkHasQuestOnServer);
		this.apkHasQuestOnServer = apkHasQuestOnServer;
	}

	/**
	 * Returns if this external application could have a questionnaire
	 * @return
	 */
	public boolean couldHaveQuestionnaire(){
		return apkHasQuestOnServer;
	}

	private static String LINEBREAK_SUBST = "#LINEBREAK";

	/**
	 * Replaces all "\r\n" and "\n" in the String with the LINEBREAK_SUBST
	 * 
	 * @param s
	 *            The original String
	 * @return The String with the replacements
	 */
	private static String toLinebreakSubst(String s) {
		return s.replaceAll(Pattern.quote("\r\n"), LINEBREAK_SUBST).replaceAll(
				Pattern.quote("\n"), LINEBREAK_SUBST);
	}

	/**
	 * Replaces all LINEBREAK_SUBST with "\n"
	 * 
	 * @param s
	 *            The original String
	 * @return The String with replacements
	 */
	private static String fromLinebreakSubst(String s) {
		return s.replaceAll(Pattern.quote(LINEBREAK_SUBST), "\n");
	}

	/**
	 * writes this object into an one-line string
	 * 
	 * @return the encoded object
	 */
	public String asOnelineString() { // ID-{name}-{description}
		String result = this.ID;
		if (isNameSet()) {
			result += SEPARATOR + TAG_NAME + toLinebreakSubst(getName());
		}
		if (isDescriptionSet()) {
			result += SEPARATOR + TAG_DESCRIPTION
					+ toLinebreakSubst(getDescription());
		}
		if (isNewestVersionSet()) {
			result += SEPARATOR + TAG_NEWESTVERSION
					+ getNewestVersion().toString();
		}
		if (isSensorsSet()) {
			result += SEPARATOR + TAG_SENSORS
					+ new JSONArray(getSensors()).toString();
		}
		if (isStartDateSet()) {
			result += SEPARATOR + TAG_STARTDATE
					+ getStartDateAsStandardString();
		}
		if (isEndDateSet()) {
			result += SEPARATOR + TAG_ENDDATE + getEndDateAsStandardString();
		}
		if (isApkVersionSet()) {
			result += SEPARATOR + TAG_APKVERSION + getApkVersion();
		}
		if (apkHasQuestOnServer){
			result += SEPARATOR + TAG_HASQUEST + "true";
		} else {
			result += SEPARATOR + TAG_HASQUEST + "false";
		}
		Log.d("ExternalApplication", "Local = " + apkHasQuestLocal + ", onServer = " + apkHasQuestOnServer);
		if (apkHasQuestLocal && apkHasQuestOnServer) {
			result += SEPARATOR + TAG_QUESTIONNAIRE + getStringQuestionnaire();
			
		} else {
			Log.i("ExternalApplication", "no questionnaire for "
					+ toLinebreakSubst(getName()) + " found");
		}
		Log.d("ExternalApplication", "asOnelineString = " + result);
		return result;
	}


	@Override
	public String toString() {
		return asOnelineString();
	}

	/**
	 * creates an external application from a string (@see
	 * {@link #asOnelineString()})
	 * 
	 * @param s
	 *            the string-encoded external application
	 * @return the decoded external application
	 */
	public ExternalApplication(String s) {
		Log.d("ExternalApplication", "fromOnelineString : " + s);
		String[] split = s.split(Pattern.quote(SEPARATOR));
		String ID = null;
		String name = null;
		String description = null;
		String newestVersion = null;
		List<Integer> sensors = null;
		String startDate = null;
		String endDate = null;
		String apkVersion = null;
		String questionnaireString = null;
		for (int i = 0; i < split.length; i++) {
			if (i == 0) {
				Log.d("ExternalApplication", "ID set to "+ split[i].toString());
				ID = split[i];
			} else {
				if (split[i].startsWith(TAG_DESCRIPTION)) {
					description = fromLinebreakSubst(split[i]
							.substring(TAG_DESCRIPTION.length()));
				}
				if (split[i].startsWith(TAG_NAME)) {
					name = fromLinebreakSubst(split[i].substring(TAG_NAME
							.length()));
				}
				if (split[i].startsWith(TAG_NEWESTVERSION)) {
					newestVersion = split[i].substring(TAG_NEWESTVERSION
							.length());
				}
				if (split[i].startsWith(TAG_SENSORS)) {
					sensors = new LinkedList<Integer>();
					JSONArray jsonarray = null;
					try {
						jsonarray = new JSONArray(
								split[i].substring(TAG_SENSORS.length()));
						for (int j = 0; j < jsonarray.length(); j++)
							sensors.add(jsonarray.getInt(j));
					} catch (JSONException e) {
						Log.e("MoSeS.APK",
								"error parsing external application from settings file",
								e);
					}
				}

				if (split[i].startsWith(TAG_STARTDATE)) {
					startDate = split[i].substring(TAG_STARTDATE.length());
				}
				if (split[i].startsWith(TAG_ENDDATE)) {
					endDate = split[i].substring(TAG_ENDDATE.length());
				}
				if (split[i].startsWith(TAG_APKVERSION)) {
					apkVersion = split[i].substring(TAG_APKVERSION.length());
				}
				if (split[i].startsWith(TAG_QUESTIONNAIRE)) {
					questionnaireString = split[i].substring(TAG_QUESTIONNAIRE.length());
				}
				if (split[i].startsWith(TAG_HASQUEST)){
					this.apkHasQuestOnServer = (split[i].substring(TAG_HASQUEST.length()) == "true");
				}
			}
		}

		
		this.setID(ID);
		this.setName(name);
		this.setDescription(description);
		this.setNewestVersion(newestVersion);
		this.setSensors(sensors);

		this.setStartDate(startDate);
		this.setEndDate(endDate);
		this.setApkVersion(apkVersion);
		Log.d("ExternalApplication", "questionnaire = " + questionnaireString);
		if (apkHasQuestOnServer && questionnaireString != null)
		if (questionnaireString.length() > 0) {
			Log.d("ExternalApplication", "has Lokal questionnaire " + questionnaireString);
			this.setQuestionnaire(questionnaireString);
			apkHasQuestLocal = true;			
		} else {
			apkHasQuestLocal = false;
		}
	}

	/**
	 * Convert a String to a Date.
	 * 
	 * @param date
	 *            the date as String (yyyy-MM-dd)
	 * @return date
	 */
	private static Date convertStringDate(String date) {
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
		Date dateObject = null;
		try {
			if (date != null && date != "null") {
				dateObject = format.parse(date);
			}
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return dateObject;
	}

	/**
	 * @return true if all data that could be retrieved for this object (name,
	 *         description, sensors, ...) is held in this object
	 */
	public boolean isDataComplete() {
		return isDescriptionSet() && isNameSet() && isSensorsSet()
				&& isNewestVersionSet() && isStartDateSet() 
				&& isEndDateSet() && isApkVersionSet();
	}

	/**
	 * Gets the questionnaire for this app from the server
	 */
	public void getQuestionnaireFromServer(){
		Log.d("External Application", "Requested Questionnaire from the server");
		if (couldHaveQuestionnaire() && !hasQuestionnaire())
		new RequestMulti_Questionnaire(new GetQuestionnaireExecutor(), ID).send();
	}
	
	/**
	 * Sets that this apk has neither an Questionnaire on the server nor locally
	 */
	void hasNoQuestionnaire() {
		apkHasQuestLocal = false;
		apkHasQuestOnServer = false;
	}
	
	/**
	 * Implementation of ReqTaskExecutor to handle the return of the server
	 * for a SetQuestionnaireRequest
	 */
	private class GetQuestionnaireExecutor implements ReqTaskExecutor {
		@Override
		public void handleException(Exception e) {
			Log.d("SetQuestionnaireAnswers", "Failed because of an exception: " + e.getMessage());
		}

		@Override
		public void postExecution(String s) {
			try {
				JSONObject j = new JSONObject(s);
				Log.d("GetQuestionnaireExecutor", "postExecution return was: "+s);
				String APKID = j.getString("APKID");
				String Status = j.getString("STATUS");
				if (Status.equals("SUCCESS")){
					Log.d("GetQuestionnaireExecutor", "Successfully received the Multi_Questionnaire");
					InstalledExternalApplicationsManager.getInstance().getAppForId(APKID).setQuestionnaire(s);
				} else if (Status.equals("FAILURE_NO_QUESTIONNAIRES")){
					Log.d("GetQuestionnaireExecutor", "Failed to receive the Questionnare, because this ExternalApplication has no Questionnaire on the server");
					InstalledExternalApplicationsManager.getInstance().getAppForId(APKID).hasNoQuestionnaire();
				} else if (Status.equals("FAILURE_INVALID_APKID")){
					Log.d("GetQuestionnaireExecutor", "Failed to receive the Questionnare, because of invalid APK");
					// TODO Handle wrong APKID
				} else if (Status.equals("FAILURE_INVALID_SESSION")){
					Log.d("GetQuestionnaireExecutor", "Failed to receive the Questionnare, because of invalid Session ID. Trying again");
					MosesService.getInstance().login();
					InstalledExternalApplicationsManager.getInstance().getAppForId(APKID).getQuestionnaireFromServer();
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
