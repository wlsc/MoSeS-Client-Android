package de.da_sense.moses.client.abstraction;

import de.da_sense.moses.client.abstraction.apks.ExternalApplication;

/**
 * Interface for getting notifications about download link requests
 * 
 * @author Simon L
 * 
 */
public interface ApkDownloadLinkRequestObserver {
	/**
	 * Notifies the observer, that the request was finished. if the reference to
	 * the external application was set, it will be passed on as parameter,
	 * otherwise this parameter will be null.
	 * 
	 * @param app
	 * 
	 * @param url
	 *            the url that was requested
	 * @param the
	 *            external application object which belongs to the download link
	 */
	public void apkDownloadLinkRequestFinished(String url, ExternalApplication app);

	/**
	 * Notifies the observer that the request failed
	 * 
	 */
	public void apkDownloadLinkRequestFailed(Exception e);
}
