package edu.mit.armadillo.networktest;

import android.util.Log;
import edu.mit.armadillo.networktest.download.DownloadProgress;
import edu.mit.armadillo.networktest.download.DownloadTask.DownloadProgressListener;

public class DebugProgressLogger implements DownloadProgressListener {
	String LOGTAG = DebugProgressLogger.class.getCanonicalName();

	@Override
	public void onDownloadProgress(DownloadProgress dl) {
		Log.d(LOGTAG, dl.toString());
	}

}
