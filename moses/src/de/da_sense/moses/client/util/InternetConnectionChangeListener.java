/**
 * 
 */
package de.da_sense.moses.client.util;

/**
 * Fragments implementing this interface are interested in change
 * of the Internet connection: connection loss and connection establishment.
 * Fragments that implement this interface offer a possibility to their parent
 * activity to directly inform them about the changes regarding the Internet
 * connection. 
 * 
 * @author Zijad Maksuti
 *
 */
public interface InternetConnectionChangeListener {
	
	/**
	 * This method is called when Internet connection is lost.
	 */
	public void onConnectionLost();
	
	/**
	 * This method is called when Internet connection is established.
	 */
	public void onConnectionEstablished();
	
}
