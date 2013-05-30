package de.da_sense.moses.client.userstudy;

import java.util.Date;

import de.da_sense.moses.client.abstraction.apks.ExternalApplication;

/**
 * represents a notification about a user study
 * 
 * @author Simon L
 * 
 */
public class UserStudyNotification {

	/** separator for the user study string */
	private final static String SEPARATOR = "#USN#";
	/** the application for this user study notification */
	private ExternalApplication application;
	/** the date for this user study notification */
	private Date date;
	/** saves the status of this user study notification */
	private Status status;

	/**
	 * Enums for the Status of the user study.
	 */
	public static enum Status {
		ACCEPTED, DENIED, UNDECIDED
	}

	/**
	 * the external application reference which this user study is about
	 * 
	 * @param application
	 */
	public UserStudyNotification(ExternalApplication application) {
		this(application, Status.UNDECIDED, new Date());
	}

	/**
	 * Constructor for a UserStudyNotification.
	 * @param application the app for this notification
	 * @param status the status of this notification
	 * @param date the date of this notification
	 */
	private UserStudyNotification(ExternalApplication application, Status status, Date date) {
		this.application = application;
		this.status = Status.UNDECIDED;
		this.date = date;
	}

	/**
	 * @return the external application this user study is about
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
	 * @param status
	 *            the new status
	 */
	public void setStatus(Status status) {
		this.status = status;
		// TODO: Meldung an Server
	}

	/**
	 * @return the date
	 */
	public Date getDate() {
		return date;
	}

	
	/**
	 * saves this user study as an one line string
	 */
	public String asOnelineString() {
		return application.asOnelineString() + SEPARATOR + this.status.toString() + SEPARATOR + this.date.getTime();
	}

	/**
	 * decode this user study from an one line string
	 * 
	 * @param s
	 *            the encoded user study
	 * @return the decoded object
	 */
	public static UserStudyNotification fromOnelineString(String s) {
		String[] split = s.split(UserStudyNotification.SEPARATOR);
		return new UserStudyNotification(new ExternalApplication(split[0]), Status.valueOf(split[1]),
				new Date(Long.parseLong(split[2])));
	}

	/*
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return asOnelineString();
	}

	/**
	 * Check if the data for the user study notification is complete.
	 * @return true if the data is complete
	 */
	public boolean isDataComplete() {
		return application.isDataComplete();
	}

}
