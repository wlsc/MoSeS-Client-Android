package de.da_sense.moses.client.abstraction.apks;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import de.da_sense.moses.client.abstraction.ApkMethods;
import de.da_sense.moses.client.util.Log;

/**
 * Activity for installing an application. call
 * {@link #setAppToInstall(File, ExternalApplication, ApkInstallObserver)} just
 * before starting the activity (only preliminary) TODO:refactor
 * 
 * @author Simon L
 * 
 */
public class InstallApkActivity extends Activity {
	private static File apkToInstall;
	private static ExternalApplication externalAppToInstall;
	private static ApkInstallObserver o;
	// TODO Change to Sparse Array ?
	private static HashMap<Integer, File> callArgMapFile = new HashMap<Integer, File>();
	private static HashMap<Integer, ExternalApplication> callArgMapAppRef = new HashMap<Integer, ExternalApplication>();
	private static HashMap<Integer, ApkInstallObserver> callArgMapObserver = new HashMap<Integer, ApkInstallObserver>();

	/**
	 * TODO needed? 2 Methoden die jeweils alle 3 Infos �bergeben bekommen. Hier wird gesetzt
	 * aber in der install m�ssen wieder alle 3 �bergeben werden?!?
	 * Sets the APK to install
	 * @param apkFile The File
	 * @param externalApplication The corresponding ExternalApplication
	 * @param o Instance of ApkInstallObserver
	 */
	public static void setAppToInstall(File apkFile, ExternalApplication externalApplication, ApkInstallObserver o) {
		InstallApkActivity.apkToInstall = apkFile;
		InstallApkActivity.externalAppToInstall = externalApplication;
		InstallApkActivity.o = o;
	}

	/**
	 * TODO needed?
	 * Clears the apk to install.
	 */
	private static void clearAppToInstall() {
		InstallApkActivity.apkToInstall = null;
		InstallApkActivity.externalAppToInstall = null;
		InstallApkActivity.o = null;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if (apkToInstall != null && externalAppToInstall != null) {
			installApk(apkToInstall, externalAppToInstall, o);
		} else {
			Log.e("MoSeS.Install", "could not install apk because it was not set in the activity");
		}

		clearAppToInstall();
	}

	/**
	 * Installs the APK
	 * @param apkToInstall The File to install
	 * @param externalAppToInstall The corresponding ExternalApplication
	 * @param o An instance of ApkInstallObserver to monitor the progress
	 */
	private void installApk(File apkToInstall, ExternalApplication externalAppToInstall, ApkInstallObserver o) {
		Intent promptInstall = new Intent(Intent.ACTION_VIEW);
		promptInstall.setDataAndType(Uri.fromFile(apkToInstall), "application/vnd.android.package-archive");
		callArgMapFile.put(Math.abs(apkToInstall.hashCode()), apkToInstall);
		callArgMapAppRef.put(Math.abs(apkToInstall.hashCode()), externalAppToInstall);
		callArgMapObserver.put(Math.abs(apkToInstall.hashCode()), o);
		this.startActivityForResult(promptInstall, Math.abs(apkToInstall.hashCode()));
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		File apkFile = callArgMapFile.get(requestCode);
		ExternalApplication appRef = callArgMapAppRef.get(requestCode);
		ApkInstallObserver o = callArgMapObserver.get(requestCode);
		if (apkFile == null) {
			Log.w("MoSeS.Install", "could not handle installation - no corresponding file/external app found");
			return;
		}
		try {
			boolean installed = ApkMethods.isApplicationInstalled(
					ApkMethods.getPackageNameFromApk(apkFile, this.getApplicationContext()),
					this.getApplicationContext());
			if (installed) {
				o.apkInstallSuccessful(apkFile, appRef);
			} else {
				// TODO: apparently there is no way to determine whether the
				// installation
				
				// crashed, or whether it was explicitly cancelled.
				// Maybe put some more thinking into it, but for now, always
				// signalize
				// "clean user cancel"
				if (resultCode == RESULT_CANCELED) {
					o.apkInstallCleanAbort(apkFile, appRef);
				} else {
					o.apkInstallCleanAbort(apkFile, appRef);
				}
			}
		} catch (IOException e) {
			o.apkInstallError(apkFile, appRef, e);
		}

		finish();
	}
}
