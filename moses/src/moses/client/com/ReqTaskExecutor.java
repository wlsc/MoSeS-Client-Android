package moses.client.com;
/**
 * This interface is used for NetworkJSON calls
 * @author Jaco
 *
 */
public interface ReqTaskExecutor {
	public void handleException(Exception e);
	
	public void postExecution(String s);
	
	public void updateExecution(NetworkJSON.BackgroundException c);
}
