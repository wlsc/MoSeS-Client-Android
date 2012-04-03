package de.da_sense.moses.client.abstraction;

import java.util.List;

import de.da_sense.moses.client.abstraction.apks.ExternalApplication;

/**
 * Interface for getting notifications about an apk list request
 * 
 * @author Simon L
 * 
 */
public interface ApkListRequestObserver {
	/**
	 * Notifies the observer, that the request was finished. if the reference to
	 * the external application was set, it will be passed on as parameter,
	 * otherwise this parameter will be null.
	 * 
	 * @param result
	 *            the application list
	 */
	public void apkListRequestFinished(List<ExternalApplication> result);

	/**
	 * Notifies the observer that the request failed
	 * 
	 * @param e
	 *            an exception object; may be null
	 */
	public void apkListRequestFailed(Exception e);
}
