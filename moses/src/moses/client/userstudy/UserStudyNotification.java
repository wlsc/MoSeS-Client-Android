package moses.client.userstudy;

import moses.client.abstraction.apks.ExternalApplication;

public class UserStudyNotification {
	
	private final static String SEPARATOR = "###"; 
	
	public static enum Status {
		ACCEPTED, DENIED, UNDECIDED
	}
	
	private ExternalApplication application;
	private Status status;
	
	public UserStudyNotification(ExternalApplication application) {
		this.application = application;
		this.status = Status.UNDECIDED;
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

	public static UserStudyNotification fromOnelineString(String line) {
		String externalApplicationOnelineString = line;
		int lastSeparator = -2;
		return null; //TODO
//		while((lastSeparator = line.indexOf(SEPARATOR)))
//		statusString = line.sub
//		
//		return ExternalApplication.fromOnelineString(externalApplicationOnelineString);
	}

}
