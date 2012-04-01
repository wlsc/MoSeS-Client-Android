package moses.client.abstraction.apks;

import moses.client.R;
import moses.client.abstraction.ApkMethods;
import moses.client.abstraction.ESensor;
import android.app.Activity;
import android.app.Dialog;
import android.content.pm.PackageManager.NameNotFoundException;
import android.widget.Gallery;
import android.widget.TextView;

/**
 * References an installed application (additional to ExternalApplication, the
 * package name of the installed application must be specified)
 * 
 * @author Simon L
 * 
 */
public class InstalledExternalApplication extends ExternalApplication {

	private static final String SEPARATOR = "#IEA#";
	private String packageName;
	private boolean wasInstalledAsUserStudy;
	private boolean updateAvailable;
	private String installedVersion;

	/**
	 * Creates the reference to the external application by specifying the
	 * package name and version number
	 * 
	 * @param packageName
	 *            the name of the package of the application
	 * @param ID
	 *            the moses id of the application
	 * @param wasInstalledAsUserStudy
	 * @param version the version of the app which is installed 
	 */
	public InstalledExternalApplication(String packageName, String ID, boolean wasInstalledAsUserStudy, String version) {
		super(ID);
		
		//assume this version as the newest version
		String newestVersion = version;
		super.setNewestVersion(newestVersion);
		this.installedVersion = version;
		
		this.wasInstalledAsUserStudy = wasInstalledAsUserStudy;
		this.packageName = packageName;
	}
	
	/**
	 * Creates the instance by adapting an already existing ExternalApplication
	 * reference. This has the advantage of copying already retrieved name and
	 * description over.
	 * 
	 * @param packageName
	 *            the package name of the installed app
	 * @param externalApp
	 *            the preexisting reference that will be adapted
	 * @param wasInstalledAsUserStudy
	 * @param version the version of the app which is installed 
	 */
	public InstalledExternalApplication(String packageName, ExternalApplication externalApp,
		boolean wasInstalledAsUserStudy, String version) {
		
		this(packageName, externalApp.getID(), wasInstalledAsUserStudy, version);
		
		if (externalApp.isDescriptionSet()) {
			setDescription(externalApp.getDescription());
		}
		if (externalApp.isNameSet()) {
			setName(externalApp.getName());
		}
		if(externalApp.isNewestVersionSet()) {
			super.setNewestVersion(externalApp.getNewestVersion());
		} else {
			//assume this version as the newest version
			String newestVersion = version;
			super.setNewestVersion(newestVersion);
		}
	}
	
	/**
	 * Creates the instance by adapting an already existing ExternalApplication
	 * reference. This has the advantage of copying already retrieved name and
	 * description over. The external applicatiopn object must have its installed version set!
	 * 
	 * @param packageName
	 *            the package name of the installed app
	 * @param externalApp
	 *            the preexisting reference that will be adapted
	 * @param wasInstalledAsUserStudy
	 */
	public InstalledExternalApplication(String packageName, ExternalApplication externalApp,
		boolean wasInstalledAsUserStudy) {
		
		this(packageName, externalApp, wasInstalledAsUserStudy, externalApp.getNewestVersion());
	}

	/**
	 * starts the application this object is referencing
	 * 
	 * @param baseActivity
	 * @throws NameNotFoundException
	 *             should only occur if the application was uninstalled after
	 *             the creation of this InstalledExternalApplication instance.
	 */
	public void startApplication(Activity baseActivity) throws NameNotFoundException {
		Dialog d = new Dialog(baseActivity);
		d.setContentView(R.layout.app_info_dialog);
		TextView t = (TextView)d.findViewById(R.id.appname);
		t.setText(getName());
		t = (TextView)d.findViewById(R.id.description);
		t.setText(getDescription());
		Gallery g = (Gallery)d.findViewById(R.id.sensors);
		Integer[] imageIds = new Integer[];
		for(int i = 0; i < getSensors().length; ++i) {
			imageIds[i] = getSensors()[i];
		}
		g.setAdapter(new ImageAdapter(this, imageIds));
		ApkMethods.startApplication(packageName, baseActivity);
	}

	/**
	 * @return the package name of the referenced application
	 */
	public String getPackageName() {
		return packageName;
	}
	
	public boolean wasInstalledAsUserStudy() {
		return wasInstalledAsUserStudy;
	}

	/**
	 * @return the version of the app which was installed
	 */
	public String getInstalledVersion() {
		return installedVersion;
	}

	/**
	 * @param installedVersion sets the installed version
	 */
	public void setInstalledVersion(String installedVersion) {
		this.installedVersion = installedVersion;
	}

	@Override
	public String toString() {
		return packageName;
	}

	@Override
	public boolean equals(Object o) {
		if (o instanceof InstalledExternalApplication) {
			if (this.getPackageName() == null) return false;
			return this.getPackageName().equals(((InstalledExternalApplication) o).getPackageName());
		} else {
			return false;
		}
	}

	@Override
	public String asOnelineString() {
		return super.asOnelineString() + SEPARATOR + this.getPackageName() + SEPARATOR
			+ Boolean.valueOf(wasInstalledAsUserStudy).toString() + SEPARATOR + installedVersion;
	}

	/**
	 * creates an installed external application from a string (@see {@link #asOnelineString()})
	 * 
	 * @param s the string-exncoded installed external application
	 * @return the decoded installed external application
	 */
	public static InstalledExternalApplication fromOnelineString(String s) {
		String[] split = s.split(SEPARATOR);
		ExternalApplication exApp = ExternalApplication.fromOnelineString(split[0]);
		return new InstalledExternalApplication(split[1], exApp, 
			Boolean.parseBoolean(split[2]), split[3]);
	}

	public boolean isUpdateAvailable() {
		return updateAvailable;
	}

	public void setUpdateAvailable(boolean updateAvailable) {
		this.updateAvailable = updateAvailable;
	}
	
	@Override
	public boolean isDataComplete() {
		return super.isDataComplete();
	}

}
