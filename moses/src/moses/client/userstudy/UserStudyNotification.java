package moses.client.userstudy;

import java.util.Date;

import moses.client.abstraction.apks.ExternalApplication;

public class UserStudyNotification {
	
	private final static String SEPARATOR = "#USN#"; 
	
	public static enum Status {
		ACCEPTED, DENIED, UNDECIDED
	}
	
	private ExternalApplication application;
	private Date date;
	private Status status;
	
	public UserStudyNotification(ExternalApplication application) {
		this(application, Status.UNDECIDED, new Date());
	}

	private UserStudyNotification(ExternalApplication application, Status status, Date date) {
		this.application = application;
		this.status = Status.UNDECIDED;
		this.date = date;
	}
	
	public ExternalApplication getApplication() {
		return application;
	}

	public Status getStatus() {
		return status;
	}
	
	public void setStatus(Status status) {
		this.status = status;
		//TODO: Meldung an Server
	}

	public String asOnelineString() {
		return application.asOnelineString()+SEPARATOR+this.status.toString()+this.date.getTime();
	}
	
	public static UserStudyNotification fromOnelineString(String s) {
		String[] split = s.split(UserStudyNotification.SEPARATOR);
		return new UserStudyNotification(ExternalApplication.fromOnelineString(split[0]), Status.valueOf(split[1]), new Date(Long.parseLong(split[2])));
	}

}
