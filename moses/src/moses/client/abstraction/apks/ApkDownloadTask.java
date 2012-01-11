package moses.client.abstraction.apks;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import moses.client.util.FileLocationUtil;
import android.content.Context;
import android.os.AsyncTask;

public class ApkDownloadTask extends AsyncTask<Void, Double, File> {

	private URL url;
	private File apkFile;
	private boolean downloadInterrupted;
	private IOException downloadException;
	private ApkDownloadObserver observer;
	private ExternalApplication externalAppRef;

	public ApkDownloadTask(ApkDownloadObserver observer, URL url, Context appContext, String apkFileName) {
		this.observer = observer;
		this.url = url;
		File downloadDir = FileLocationUtil.getApkDownloadFolder(appContext);
		apkFile = new File(downloadDir, apkFileName);
		downloadInterrupted = false;
	}

	@Override
	protected File doInBackground(Void... params) {
		return downloadFile(url);
	}

	@Override
	protected void onPostExecute(File result) {
		if (result != null) {
			observer.apkDownloadFinished(this, result, externalAppRef);
		} else {
			observer.apkDownloadFailed(this);
		}
	}

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

			while ((bufferLength = inputStream.read(buffer)) > 0) {
				// add the data in the buffer to the file in the file output
				// stream (the file on the sd card
				fileOutput.write(buffer, 0, bufferLength);
				// add up the size so we know how much is downloaded
				downloadedSize += bufferLength;
				if (totalSize > 0) {
					publishProgress(Double.valueOf(downloadedSize / totalSize));
				} else {
					publishProgress(Double.valueOf(0));
				}
			}
			publishProgress(Double.valueOf(1));
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
			} else if (!apkFile.exists()) { return null; }
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
