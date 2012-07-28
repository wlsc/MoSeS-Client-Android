package de.da_sense.moses.client.abstraction.apks;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import de.da_sense.moses.client.service.helpers.ExecutableForObject;
import de.da_sense.moses.client.util.FileLocationUtil;

import android.content.Context;
import android.os.AsyncTask;

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
		this.observer = observer;
		this.url = url;
		File downloadDir = FileLocationUtil.getApkDownloadFolder(appContext);
		apkFile = new File(downloadDir, apkFileName);
		downloadInterrupted = false;
		this.progressListener = progressListener;
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
