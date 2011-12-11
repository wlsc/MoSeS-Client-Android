package moses.client.com;

public interface ReqTaskExecutor {
	public void postExecution(String s);
	
	public void updateExecution(ConnectionParam c);
	
	public void handleException(Exception e);
}
