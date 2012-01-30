package moses.client.com;

// TODO: Auto-generated Javadoc
/**
 * This interface is used for NetworkJSON calls.
 * 
 * @author Jaco Hofmann
 */
public interface ReqTaskExecutor {

	/**
	 * Handle exception.
	 * 
	 * @param e
	 *            the e
	 */
	public void handleException(Exception e);

	/**
	 * Post execution.
	 * 
	 * @param s
	 *            the s
	 */
	public void postExecution(String s);

	/**
	 * Update execution.
	 * 
	 * @param c
	 *            the c
	 */
	public void updateExecution(NetworkJSON.BackgroundException c);
}
