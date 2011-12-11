package moses.client.com;

public interface ReqTaskExecutor {
	public void postExecution(String s);
	
	public void updateExecution(NetworkJSON.BackgroundException c);
	
	public void handleException(Exception e);
}
