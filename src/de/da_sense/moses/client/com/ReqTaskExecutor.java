package de.da_sense.moses.client.com;
/**
 * This interface is used for NetworkJSON calls.
 * 
 * @author Jaco Hofmann
 */
public interface ReqTaskExecutor {

	/**
	 * Specifies how to handle an exception.
	 * 
	 * @param e
	 *            The Exception to handle
	 */
	public void handleException(Exception e);

	/**
	 * Specifies what to do after the execution.
	 * 
	 * @param s
	 *            The Resultstring from the Execution
	 */
	public void postExecution(String s);

	/**
	 * Update execution.
	 * 
	 * @param c
	 *            The NetworkJSON.BackgroundException
	 */
	public void updateExecution(NetworkJSON.BackgroundException c);
}
