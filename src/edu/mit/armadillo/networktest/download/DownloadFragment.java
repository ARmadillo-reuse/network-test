package edu.mit.armadillo.networktest.download;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import android.app.Activity;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import edu.mit.armadillo.networktest.DebugProgressLogger;
import edu.mit.armadillo.networktest.MainActivity;
import edu.mit.armadillo.networktest.R;
import edu.mit.armadillo.networktest.download.DownloadProgress.ErrorMessage;
import edu.mit.armadillo.networktest.download.DownloadProgress.FinishMessage;
import edu.mit.armadillo.networktest.download.DownloadProgress.ProgressMessage;
import edu.mit.armadillo.networktest.download.DownloadProgress.StartMessage;
import edu.mit.armadillo.networktest.download.DownloadTask.DownloadProgressListener;

public class DownloadFragment extends Fragment implements
		DownloadProgressListener, OnClickListener {

	private static final String LOGTAG = DownloadFragment.class
			.getCanonicalName();
	private DownloadTask downloadWorker;
	private String url;
	private long startTime;
	private DownloadDataLogger dataLogger;

	private ProgressBar progressBar;
	private TextView status;
	private Button send_button;

	public DownloadFragment() {
		this.url = "http://web.mit.edu/6.115/www/datasheets/8051.pdf";
	}

	@Override
	public void setArguments(Bundle args) {
		if (args.containsKey("URL")) {
			this.url = args.getString("URL");
		}
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.downloadWorker = new DownloadTask();
		try {
			this.dataLogger = new DownloadDataLogger(getActivity());
		} catch (FileNotFoundException e) {
			throw new RuntimeException(e);
		}
		this.downloadWorker.registerProgressListener(dataLogger);
		this.downloadWorker.registerProgressListener(this);
		this.downloadWorker.registerProgressListener(new DebugProgressLogger());
	}

	public android.view.View onCreateView(LayoutInflater inflater,
			ViewGroup container, Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.download_fragment, container, false);
		this.progressBar = (ProgressBar) v.findViewById(android.R.id.progress);
		this.status = (TextView) v.findViewById(android.R.id.text1);
		this.send_button = (Button) v.findViewById(android.R.id.button1);
		this.send_button.setOnClickListener(this);
		return v;
	}

	@Override
	public void onResume() {
		super.onResume();
		Log.d(LOGTAG, "Resuming");
		if (!this.downloadWorker.hasStarted()) {
			Log.d(LOGTAG, "Starting download");
			this.downloadWorker.execute(this.url);
		}
	}

	@Override
	public void onPause() {
		super.onPause();
		if (this.downloadWorker.hasStarted()
				&& !this.downloadWorker.hasFinished())
			this.downloadWorker.cancel(false);
	}

	@Override
	public void onDownloadProgress(DownloadProgress dl) {
		switch (dl.getType()) {
		case Initiate:
			this.startTime = dl.timestamp;
			break;
		case Start:
			if (((StartMessage) dl).isSizeAvailable()) {
				long size = ((StartMessage) dl).getDownloadSize();
				this.progressBar.setIndeterminate(false);
				this.progressBar.setMax((int) size);
				this.progressBar.setProgress(0);
			} else {
				this.progressBar.setIndeterminate(true);
			}
			break;
		case Progress:
			if (!this.progressBar.isIndeterminate()) {
				this.progressBar.setProgress((int) ((ProgressMessage) dl)
						.getDownloadByteCount());
			}
			break;
		case Finish:
			onDownloadFinished((FinishMessage) dl);
			break;
		case Error:
			Toast.makeText(getActivity(),
					"Error while downloading: " + ((ErrorMessage) dl).message,
					Toast.LENGTH_LONG).show();
			((MainActivity) getActivity()).onDownloadError();
		}
	}

	private void onDownloadFinished(FinishMessage dl) {
		if (this.progressBar.isIndeterminate()) {
			this.progressBar.setIndeterminate(false);
			this.progressBar.setMax(100);
		}

		this.progressBar.setProgress(this.progressBar.getMax());

		this.status.setText(getActivity().getString(
				R.string.download_finish_message, dl.getFinalSize(),
				(dl.timestamp - this.startTime) / 1000f, this.url));
		this.send_button.setVisibility(View.VISIBLE);
	}

	@Override
	public void onClick(View v) {
		if (v == this.send_button) {
			try {
				Intent emailIntent = new Intent(Intent.ACTION_SEND);
				ConnectivityManager cm = (ConnectivityManager) getActivity()
						.getSystemService(Activity.CONNECTIVITY_SERVICE);
				NetworkInfo currentNetwork = cm.getActiveNetworkInfo();

				emailIntent.putExtra(
						Intent.EXTRA_SUBJECT,
						"ARmadillo network test on "
								+ currentNetwork.getTypeName() + " "
								+ currentNetwork.getSubtypeName());
				char[] buffer = new char[4096];
				FileReader fr = new FileReader(dataLogger.getLogFile());
				StringBuilder sb = new StringBuilder(
						"From the network test app:\n\n");

				int count;
				while ((count = fr.read(buffer)) != -1) {
					sb.append(buffer, 0, count);
				}
				fr.close();
				emailIntent.putExtra(Intent.EXTRA_TEXT, sb.toString());
				emailIntent.setType("text/plain");
				getActivity().startActivity(emailIntent);
			} catch (IOException io) {
				throw new RuntimeException(io);
			}
		}
	}
}
