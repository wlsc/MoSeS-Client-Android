package moses.client.userstudy;

import java.util.Date;

import moses.client.abstraction.apks.ExternalApplication;

/**
 * represents a notification about a user study
 * 
 * @author Simon L
 *
 */
public class UserStudyNotification {
	
	private final static String SEPARATOR = "#USN#"; 
	
	public static enum Status {
		ACCEPTED, DENIED, UNDECIDED
	}
	
	private ExternalApplication application;
	private Date date;
	private Status status;
	
	/**
	 * the external application reference which this user study is about
	 * 
	 * @param application
	 */
	public UserStudyNotification(ExternalApplication application) {
		this(application, Status.UNDECIDED, new Date());
	}

	private UserStudyNotification(ExternalApplication application, Status status, Date date) {
		this.application = application;
		this.status = Status.UNDECIDED;
		this.date = date;
	}
	
	/**
	 * @return the external applicatiuon this user study is about
	 */
	public ExternalApplication getApplication() {
		return application;
	}

	/**
	 * @return the confirmation/refusal/undecided status of this user study
	 */
	public Status getStatus() {
		return status;
	}
	
	/**
	 * set the confirmation/refusal/undecided status of this user study
	 * 
	 * @param status the new status
	 */
	public void setStatus(Status status) {
		this.status = status;
		//TODO: Meldung an Server
	}

	/**
	 * saves this user study as an one line string
	 */
	public String asOnelineString() {
		return application.asOnelineString()+SEPARATOR+this.status.toString()+SEPARATOR+this.date.getTime();
	}
	
	/**
	 * decode this user study from an one line string 
	 * 
	 * @param s the encoded user study
	 * @return the decoded object
	 */
	public static UserStudyNotification fromOnelineString(String s) {
		String[] split = s.split(UserStudyNotification.SEPARATOR);
		return new UserStudyNotification(ExternalApplication.fromOnelineString(split[0]), Status.valueOf(split[1]), new Date(Long.parseLong(split[2])));
	}
	
	@Override
	public String toString() {
		return asOnelineString();
	}

}
