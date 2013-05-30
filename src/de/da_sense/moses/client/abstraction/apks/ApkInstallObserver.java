package de.da_sense.moses.client.abstraction.apks;

import java.io.File;

/**
 * Observer interface for apk installation processes
 * 
 * @author Simon L
 * 
 */
public interface ApkInstallObserver {
	/**
	 * is called when the installation process was cleanly finished
	 * 
	 * @param apk
	 *            the apk file that was installed
	 */
	public void apkInstallSuccessful(File apk, ExternalApplication appRef);

	/**
	 * called if the installation encountered no errors per se, but the user
	 * chose to cancel the installation.
	 * 
	 * @param apk
	 *            the apk file that was meant to be installed
	 */
	public void apkInstallCleanAbort(File apk, ExternalApplication appRef);

	/**
	 * Called if the installation could not be completed successfully.
	 * 
	 * @param apk
	 *            the apk file
	 * @param appRef
	 * @param e
	 *            exception that was thrown somewhere in the process, or null if
	 *            there is no such exception.
	 */
	public void apkInstallError(File apk, ExternalApplication appRef, Throwable e);
}
