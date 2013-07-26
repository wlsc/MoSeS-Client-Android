package de.da_sense.moses.client.abstraction.apks;

import java.io.File;
import java.io.IOException;
import java.util.Observable;

import android.content.Context;
import de.da_sense.moses.client.R;
import de.da_sense.moses.client.abstraction.ApkMethods;
import de.da_sense.moses.client.service.MosesService;
import de.da_sense.moses.client.util.Log;

/**
 * Abstraction for installing a single application package file.
 * 
 * Supports the {@link Observable} scheme for updates in the install process.
 * See {@link State}.
 * 
 * @author Simon L, Wladimir Schmidt
 * 
 */
public class ApkInstallManager extends Observable implements ApkInstallObserver {

	/**
	 * States of progress that the ApkInstallManager can assume
	 * 
	 * @author Simon L
	 * 
	 */
	public static enum State {
		JUST_INITIALIZED, INSTALLATION_REQUESTED, INSTALLATION_CANCELLED, INSTALLATION_COMPLETED, ERROR;

		public boolean isEndState() {
			return this == State.ERROR || this == State.INSTALLATION_COMPLETED || this == State.INSTALLATION_CANCELLED;
		}
	}

	/**
	 * The current errorMsg
	 */
	private String errorMsg;
	/**
	 * The current State
	 */
	private State state;
	/**
	 * Reference to mosesService
	 */
	private MosesService mosesService;
	/**
	 * The file to install
	 */
	private File file;
	/**
	 * The corresponding ExternalApplication
	 */
	private ExternalApplication appRef;
	
	private Context context;

	/**
	 * creates the installation manager with the required parameters
	 * 
	 * @param apkFile
	 *            the file from which to install
	 * @param appRef
	 *            the reference to the external application that is being
	 *            installed
	 * @param context 
	 */
	public ApkInstallManager(File apkFile, ExternalApplication appRef, Context context) {
		super();
		this.file = apkFile;
		this.appRef = appRef;
		this.context = context;
		this.mosesService = MosesService.getInstance();
		if (this.mosesService == null) {
			setErrorState(context.getString(R.string.apkInstallManager_errorMessage));
			return;
		}

		setState(State.JUST_INITIALIZED);
	}

	/**
	 * start the installation process
	 */
	public void start() {
		startInstallation();
	}

	/**
	 * Try to install the APK
	 */
	private void startInstallation() {
		try {
			setState(State.INSTALLATION_REQUESTED);
			ApkMethods.installApk(file, appRef, this);
		} catch (IOException e) {
			setErrorState(context.getString(R.string.apkInstallManager_apkInstallFailed), e);
			Log.e("ApkInstallManager", e.getMessage());
		}
	}

	/**
	 * Sets the state
	 * @param state The state to set to
	 */
	private void setState(State state) {
		this.state = state;
		this.setChanged();
		notifyObservers(state);
	}

	/**
	 * sets the error state with a message; notifies observers
	 * 
	 * @param errorMsg
	 */
	private void setErrorState(String errorMsg) {
		setErrorState(errorMsg, null);
	}

	/**
	 * sets the error state with a message and a related throwable; notifies
	 * observers
	 * 
	 * @param errorMsg
	 *            the error message
	 * @param e
	 *            a throwable that was thrown when the error occured
	 */
	private void setErrorState(String errorMsg, Throwable e) {
		this.errorMsg = errorMsg;
		if (e != null) {
			Log.e("MoSeS.InstallApk", errorMsg, e);
		} else {
			Log.e("MoSeS.InstallApk", errorMsg);
		}

		setState(State.ERROR);
	}

	/**
	 * @return the state of progress of the installation manager
	 */
	public State getState() {
		return state;
	}

	/**
	 * Returns the errorMsg
	 * @return errorMsg - String
	 */
	public String getErrorMsg() {
		return errorMsg;
	}

	@Override
	public void apkInstallSuccessful(File apk, ExternalApplication appRef) {
		setState(State.INSTALLATION_COMPLETED);
	}

	/**
	 * registers an installed application in the
	 * {@link InstalledExternalApplicationsManager}. In the process, an external
	 * 
	 * @param apk
	 *            the original apk file for the already installed app
	 * @param externalAppRef
	 *            the external application reference
	 * @param context
	 *            context to perform operations
	 * @return the InstalledExternalApplication object that was
	 *         created&registered
	 * @throws IOException
	 *             when there were Problems with extracting package name from
	 *             apk, or problems with the
	 *             InstalledExternalApplicationsManager after installing an app
	 */
	public static InstalledExternalApplication registerInstalledApk(File apk, ExternalApplication externalAppRef,
			Context context, boolean isUserStudy) throws IOException {
		// gather informations about the app
		if (InstalledExternalApplicationsManager.getInstance() == null) {
			InstalledExternalApplicationsManager.init(context);
		}
		String packageName;

		packageName = ApkMethods.getPackageNameFromApk(apk, context);
		Log.d("TEST", externalAppRef.asOnelineString());
		InstalledExternalApplication installedExternalApp = new InstalledExternalApplication(packageName,
				externalAppRef, isUserStudy);
		InstalledExternalApplicationsManager.getInstance().addExternalApplication(installedExternalApp);
		InstalledExternalApplicationsManager.getInstance().saveToDisk(context);
		return installedExternalApp;
	}

	@Override
	public void apkInstallCleanAbort(File apk, ExternalApplication appRef) {
		setState(State.INSTALLATION_CANCELLED);
	}

	@Override
	public void apkInstallError(File apk, ExternalApplication appRef, Throwable e) {
		setErrorState("Could not install application", e);
	}

}
