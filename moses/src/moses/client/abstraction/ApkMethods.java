package moses.client.abstraction;

import java.io.File;
import java.io.IOException;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;

public class ApkMethods {

	/**
	 * Installs a given apk file.
	 * 
	 * @param apk
	 * @param baseActivity
	 */
	public static void installApk(File apk, Activity baseActivity) {
		Intent promptInstall = new Intent(Intent.ACTION_VIEW);
		if(apk.exists()) {
			promptInstall.setDataAndType(Uri.fromFile(apk), "application/vnd.android.package-archive");
			try {
				baseActivity.startActivity(promptInstall);
			} catch (ActivityNotFoundException e) {
				//should not occur
				e.printStackTrace();
			}
		} else {
			throw new IllegalArgumentException("apk file does not exist");
		}
	}
	
	public static void startApplication(String packageName, Activity baseActivity) throws NameNotFoundException {
		Intent intent = baseActivity.getApplicationContext().getPackageManager().getLaunchIntentForPackage(packageName);
		baseActivity.startActivity(intent);
	}
	
	/**
	 * Retrieves the package name from an apk file
	 * 
	 * @param apk the file
	 * @param appContext the application context
	 * @return the package name
	 * @throws IOException if the apk file could not be parsed correctly
	 */
	public static String getPackageNameFromApk(File apk, Context appContext) throws IOException {
		PackageInfo appInfo = appContext.getPackageManager().getPackageArchiveInfo(apk.getAbsolutePath(), 0);
		if(appInfo != null) {
			return appInfo.packageName;
		} else {
			throw new IOException("the given package could not be parsed correctly");
		}
	}
	
	/**
	 * checks whether an application is installed or not
	 * 
	 * @param packageName the package name for the application
	 * @param context
	 * @return
	 */
	public static boolean isApplicationInstalled(String packageName, Context context) {
		try {
			context.getPackageManager().getApplicationInfo(packageName, 0);
		} catch (NameNotFoundException e) {
			return false;
		}
		return true;
	}
	
}

