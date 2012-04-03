package de.da_sense.moses.client.abstraction.apks;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

import de.da_sense.moses.client.abstraction.ApkMethods;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

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
	private static HashMap<Integer, File> callArgMapFile = new HashMap<Integer, File>();
	private static HashMap<Integer, ExternalApplication> callArgMapAppRef = new HashMap<Integer, ExternalApplication>();
	private static HashMap<Integer, ApkInstallObserver> callArgMapObserver = new HashMap<Integer, ApkInstallObserver>();

	public static void setAppToInstall(File apkFile, ExternalApplication externalApplication, ApkInstallObserver o) {
		InstallApkActivity.apkToInstall = apkFile;
		InstallApkActivity.externalAppToInstall = externalApplication;
		InstallApkActivity.o = o;
	}

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
				// crashed, or whether it was explicitely cancelled.
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
