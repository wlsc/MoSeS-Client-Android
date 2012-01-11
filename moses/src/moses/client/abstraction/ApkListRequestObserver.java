package moses.client.abstraction;

import java.util.List;

import moses.client.abstraction.apks.ExternalApplication;

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
	 */
	public void apkListRequestFailed(Exception e);
}
