package moses.client.abstraction.apks;

import java.io.File;
import java.io.IOException;
import java.util.Observable;

import moses.client.abstraction.ApkMethods;
import moses.client.service.MosesService;
import android.util.Log;
import android.widget.Toast;

/**
 * Abstraction for installing a single application package file.
 * 
 * Supports the {@link Observable} scheme for updates in the install process. See {@link State}.
 * 
 * @author Simon L
 * 
 */
public class ApkInstallManager extends Observable implements ApkInstallObserver {

	public static enum State {
		JUST_INITIALIZED, INSTALLATION_REQUESTED, INSTALLATION_CANCELLED, INSTALLATION_COMPLETED, ERROR;

		public boolean isEndState() {
			return this == State.ERROR || this == State.INSTALLATION_COMPLETED || this == State.INSTALLATION_CANCELLED;
		}
	}
	
	private String errorMsg;
	private State state;
	private MosesService mosesService;
	private File file;
	private ExternalApplication appRef;
	
	public ApkInstallManager(File apkFile, ExternalApplication appRef) {
		super();
		this.file = apkFile;
		this.appRef = appRef;
		this.mosesService = MosesService.getInstance();
		if(this.mosesService == null) {
			setErrorState("service is not started yet, thus the installation could not be started");
			return;
		}
		
		setState(State.JUST_INITIALIZED);
	}
	
	/**
	 * start the installation process
	 */
	public void start() {
		requestInstallation();
	}
	
	private void requestInstallation() {
		try {
			setState(State.INSTALLATION_REQUESTED);
			ApkMethods.installApk(file, appRef, this);
		} catch (IOException e) {
			setErrorState("Apk install failed", e);
			e.printStackTrace();
		}
	}

	private void setState(State state) {
		this.state = state;
		notifyObservers(state);
	}
	
	private void setErrorState(String errorMsg) {
		setErrorState(errorMsg, null);
	}
	
	private void setErrorState(String errorMsg, Throwable e) {
		this.errorMsg = errorMsg;
		if(e != null) {
			Log.e("MoSeS.InstallApk", errorMsg, e);
		} else {
			Log.e("MoSeS.InstallApk", errorMsg);
		}
		
		Toast.makeText(mosesService, errorMsg, Toast.LENGTH_LONG).show();
		setState(State.ERROR);
	}
	
	public State getState() {
		return state;
	}
	
	public String getErrorMsg() {
		return errorMsg;
	}


	@Override
	public void apkInstallSuccessful(File apk, ExternalApplication appRef) {
		//gather informations about the app
		//TODO: should this installer meddle with registering objects into the manager? I think not.
		if (InstalledExternalApplicationsManager.getDefault() == null) {
			InstalledExternalApplicationsManager
					.init(mosesService);
		}
		String packageName;
		try {
			packageName = ApkMethods.getPackageNameFromApk(file,
					mosesService);
			InstalledExternalApplication installedExternalApp = new InstalledExternalApplication(
				packageName, appRef);
			InstalledExternalApplicationsManager.getDefault()
					.addExternalApplication(installedExternalApp);
			InstalledExternalApplicationsManager.getDefault().saveToDisk(
					mosesService);
		} catch (IOException e) {
			Log.e("MoSeS.Install", "Problems with the InstalledExternalApplicationsManager after installing an app", e);
		}
		
		setState(State.INSTALLATION_COMPLETED);
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
