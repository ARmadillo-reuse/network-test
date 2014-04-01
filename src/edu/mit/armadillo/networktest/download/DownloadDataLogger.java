package edu.mit.armadillo.networktest.download;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.LinkedList;
import java.util.List;

import android.content.Context;
import android.os.Environment;
import edu.mit.armadillo.networktest.download.DownloadProgress.ErrorMessage;
import edu.mit.armadillo.networktest.download.DownloadProgress.FinishMessage;
import edu.mit.armadillo.networktest.download.DownloadProgress.ProgressMessage;
import edu.mit.armadillo.networktest.download.DownloadTask.DownloadProgressListener;

public class DownloadDataLogger implements DownloadProgressListener {

	private final Context c;
	private final PrintWriter outputWriter;
	private final List<Double> bytesPerSecFiveSecInterval;
	private final File logFile;

	private long initTime;
	private long startTime;
	private long lastFiveSecondIntervalCount;
	private long lastDownloadedByteCount;
	private long lastProgressTime;

	public DownloadDataLogger(Context context) throws FileNotFoundException {
		this.c = context;
		this.logFile = new File(context.getCacheDir(), "nework-test-log-"
				+ System.currentTimeMillis() + ".log");
		this.outputWriter = new PrintWriter(logFile);
		bytesPerSecFiveSecInterval = new LinkedList<Double>();

		lastDownloadedByteCount = 0;
		lastProgressTime = 0;
		lastFiveSecondIntervalCount = 0;
	}

	@Override
	public void onDownloadProgress(DownloadProgress dl) {
		switch (dl.getType()) {
		case Initiate:
			this.initTime = dl.timestamp;
			this.outputWriter.print("Initiating download at ");
			this.outputWriter.println(this.initTime);
			break;
		case Start:
			this.startTime = dl.timestamp;
			this.outputWriter.print("Starting download @ ");
			this.outputWriter.print(this.startTime);
			this.outputWriter.print("; latency = ");
			this.outputWriter.print(this.startTime - this.initTime);
			this.outputWriter.println(" milliseconds");
			this.outputWriter.println();
			this.outputWriter
					.println("The following is a 2-column table with timestamp on the left (milliseconds");
			this.outputWriter
					.println("elapsed since intialization) and the number of bytes downloaded on the right.");
			this.outputWriter
					.println("================BEGIN CSV OUTPUT================");
			break;
		case Progress:
			long bytesDownloaded = ((ProgressMessage) dl)
					.getDownloadByteCount();
			this.outputWriter.print(dl.timestamp - this.initTime);
			this.outputWriter.print(", ");
			this.outputWriter.println(bytesDownloaded);
			if (dl.timestamp - this.initTime > (1 + bytesPerSecFiveSecInterval
					.size()) * 5000) {
				long bytesSinceLastUpdate = bytesDownloaded
						- lastDownloadedByteCount;
				long mSecondsSinceLastUpdate = dl.timestamp - lastProgressTime;
				double bytesPerMSecond = bytesSinceLastUpdate
						/ (double) mSecondsSinceLastUpdate;
				double estimatedBytesAtInterval = bytesPerMSecond
						* (5000 * (bytesPerSecFiveSecInterval.size() + 1) - (lastProgressTime - startTime))
						+ lastDownloadedByteCount;
				bytesPerSecFiveSecInterval
						.add((estimatedBytesAtInterval - lastFiveSecondIntervalCount) / 5);

				lastFiveSecondIntervalCount = bytesDownloaded;
			}
			lastProgressTime = dl.timestamp;
			lastDownloadedByteCount = bytesDownloaded;
			break;
		case Error:
			this.outputWriter.print("*ERROR* ");
			this.outputWriter.println(((ErrorMessage) dl).message);
			break;
		case Finish:
			this.outputWriter.println();
			this.outputWriter
					.println("=================END CSV OUTPUT=================");
			this.outputWriter.println();
			this.outputWriter.println("Download speed every five seconds:");
			{
				long bytesSinceInterval = ((FinishMessage) dl).getFinalSize()
						- lastFiveSecondIntervalCount;
				long mSecondsSinceInterval = lastProgressTime - startTime
						- 5000 * bytesPerSecFiveSecInterval.size();
				double bytesPerSec = bytesSinceInterval
						/ ((double) mSecondsSinceInterval / 1000);
				this.bytesPerSecFiveSecInterval.add(bytesPerSec);
			}
			this.outputWriter.println();
			this.outputWriter
					.println("The following is a 2-column table with the number of elapsed seconds");
			this.outputWriter
					.println("since intialization on the left and the number of bytes downloaded per");
			this.outputWriter.println("second on the right.");
			this.outputWriter
					.println("================BEGIN CSV OUTPUT================");
			int i = 1;
			for (double dataPoint : bytesPerSecFiveSecInterval) {
				this.outputWriter.print(i);
				this.outputWriter.print(", ");
				this.outputWriter.println(dataPoint);
				i++;
			}
			this.outputWriter.println();
			this.outputWriter
					.println("=================END CSV OUTPUT=================");
			this.outputWriter.flush();
			this.outputWriter.close();
		}
	}

	public File getLogFile() {
		return logFile;
	}
}
