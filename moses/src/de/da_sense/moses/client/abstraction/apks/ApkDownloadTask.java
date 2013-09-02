/*******************************************************************************
 * Copyright 2013
 * Telecooperation (TK) Lab
 * Technische Universität Darmstadt
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package de.da_sense.moses.client.abstraction.apks;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import de.da_sense.moses.client.service.helpers.ExecutableForObject;
import de.da_sense.moses.client.util.FileLocationUtil;

/**
 * asynchronous downloading task for an apk file
 * 
 * @author Simon L
 * 
 */
public class ApkDownloadTask extends AsyncTask<Void, Integer, File> {

	private URL url;
	private File apkFile;
	private boolean downloadInterrupted;
	private IOException downloadException;
	private ApkDownloadObserver observer;
	private ExternalApplication externalAppRef;
	private ExecutableForObject progressListener;
	/** a log tag for this class */
    private final static String TAG = "ApkDownloadTask";

	/**
	 * creates the downloading task with the required parameters
	 * 
	 * @param observer
	 *            observer interface - implementing class for notifications
	 *            about progress etc.
	 * @param url
	 *            the url of the file to download
	 * @param appContext
	 *            context
	 * @param apkFileName
	 *            the file name of the apk file in the default location
	 */
	public ApkDownloadTask(ApkDownloadObserver observer, URL url, Context appContext, String apkFileName,
			ExecutableForObject progressListener) {
	    Log.d(TAG,"ApkDownloadTask( " + observer + " , " + url + " , " + appContext + " , " + apkFileName + " , " + progressListener + " )");
		this.observer = observer;
		this.url = url;
		File downloadDir = FileLocationUtil.getApkDownloadFolder(appContext);
		apkFile = new File(downloadDir, apkFileName);
		downloadInterrupted = false;
		this.progressListener = progressListener;
		Log.d(TAG,"downloadDir = " + downloadDir + " apkFile = " + apkFile);
	}

	@Override
	protected File doInBackground(Void... params) {
		return downloadFile(url);
	}

	@Override
	protected void onProgressUpdate(Integer... integers) {
		if (progressListener != null) {
			progressListener.execute(integers[0]);
		}
	}

	@Override
	protected void onPostExecute(File result) {
		if (result != null) {
			observer.apkDownloadFinished(this, result, externalAppRef);
		} else {
			observer.apkDownloadFailed(this);
		}
	}

	/**
	 * starts the download
	 * 
	 * @param url
	 *            the url
	 * @return
	 */
	private File downloadFile(URL url) {
        Log.d(TAG,"downloadFile( " + url + " )");
        Log.d(TAG,"apkFile = " + apkFile);
		FileOutputStream fileOutput = null;

		try {
			HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
			urlConnection.setRequestMethod("GET");
			urlConnection.setDoOutput(true);

			urlConnection.connect();

			fileOutput = new FileOutputStream(apkFile);
			InputStream inputStream = urlConnection.getInputStream();

			int totalSize = urlConnection.getContentLength();
			int downloadedSize = 0;

			// create a buffer...
			byte[] buffer = new byte[1024];
			int bufferLength = 0; // used to store a temporary size of the
									// buffer
			publishProgress(totalSize);
			while ((bufferLength = inputStream.read(buffer)) > 0) {
				// add the data in the buffer to the file in the file output
				// stream (the file on the sd card
				fileOutput.write(buffer, 0, bufferLength);
				// add up the size so we know how much is downloaded
				downloadedSize += bufferLength;
				if (totalSize > 0) {
					publishProgress(downloadedSize);
				} else {
					publishProgress(0);
				}
			}
			publishProgress(totalSize);
		} catch (IOException e) {
			e.printStackTrace();
			this.downloadInterrupted = true;
			this.downloadException = e;
		} finally {
			if (fileOutput != null) {
				try {
					fileOutput.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if (apkFile.exists() && this.downloadInterrupted) {
				apkFile.delete();
				apkFile = null;
			} else if (!apkFile.exists()) {
				return null;
			}
		}

		return apkFile;
	}

	public IOException getDownloadException() {
		return downloadException;
	}

	public void setExternalApplicationReference(ExternalApplication app) {
		this.externalAppRef = app;
	}

}
