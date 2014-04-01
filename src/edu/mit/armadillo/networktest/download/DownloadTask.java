package edu.mit.armadillo.networktest.download;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;

import android.os.AsyncTask;

public class DownloadTask extends AsyncTask<String, DownloadProgress, Void> {

	public interface DownloadProgressListener {
		void onDownloadProgress(DownloadProgress dl);
	}

	private static final String LOGTAG = DownloadTask.class.getCanonicalName();

	private Set<DownloadProgressListener> listeners;
	private volatile boolean started, finished;

	public DownloadTask() {
		listeners = new HashSet<DownloadTask.DownloadProgressListener>();
		finished = false;
	}

	public void registerProgressListener(DownloadProgressListener listener) {
		this.listeners.add(listener);
	}

	@Override
	protected void onPreExecute() {
		this.started = true;
	}

	@Override
	protected Void doInBackground(String... sUrl) {
		InputStream input = null;
		HttpURLConnection connection = null;
		try {
			URL url = new URL(sUrl[0]);
			publishProgress(new DownloadProgress.InitiateMessage(
					System.currentTimeMillis()));

			connection = (HttpURLConnection) url.openConnection();
			connection.connect();

			// expect HTTP 200 OK, so we don't mistakenly save error report
			// instead of the file
			if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
				publishProgress(new DownloadProgress.ErrorMessage(System.currentTimeMillis(),
						"Received HTTP " + connection.getResponseCode()
								+ " instead of 200"));
				return null;
			}

			// this will be useful to display download percentage
			// might be -1: server did not report the length
			int fileLength = connection.getContentLength();
			publishProgress(new DownloadProgress.StartMessage(
					System.currentTimeMillis(), fileLength));

			// download the file
			input = connection.getInputStream();

			byte data[] = new byte[4096];
			long total = 0;
			int count;
			while ((count = input.read(data)) != -1) {
				// allow canceling with back button
				if (isCancelled()) {
					input.close();
					return null;
				}
				total += count;
				// publishing the progress....
				publishProgress(new DownloadProgress.ProgressMessage(
						System.currentTimeMillis(), total));
			}

			publishProgress(new DownloadProgress.FinishMessage(
					System.currentTimeMillis(), total));
		} catch (Exception e) {
			publishProgress(new DownloadProgress.ErrorMessage(
					System.currentTimeMillis(), e.getMessage()));
			return null;
		} finally {
			try {
				if (input != null)
					input.close();
			} catch (IOException ignored) {
			}

			if (connection != null)
				connection.disconnect();
		}
		return null;
	}

	@Override
	protected void onPostExecute(Void result) {
		this.finished = true;
	}

	@Override
	protected void onCancelled() {
		this.finished = true;
	}

	@Override
	protected void onProgressUpdate(DownloadProgress... values) {
		for (DownloadProgress dp : values) {
			for (DownloadProgressListener dl : this.listeners) {
				dl.onDownloadProgress(dp);
			}
		}
	}

	public boolean hasFinished() {
		return this.finished;
	}

	public boolean hasStarted() {
		return this.started;
	}
}
