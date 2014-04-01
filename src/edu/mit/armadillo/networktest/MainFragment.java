package edu.mit.armadillo.networktest;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

public class MainFragment extends Fragment implements OnClickListener {

	private Button startButton;
	private TextView urlField;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.main_fragment, container, false);
		startButton = (Button) v.findViewById(android.R.id.button1);
		startButton.setOnClickListener(this);
		
		urlField = (TextView) v.findViewById(android.R.id.input);
		urlField.setText("http://web.mit.edu/21w.789/www/papers/griswold2004.pdf");
		return v;
	}

	@Override
	public void onClick(View v) {
		if (v == startButton) {
			((MainActivity) getActivity()).startDownload();
		}
	}

	public String getDownloadURL() {
		return urlField.getText().toString();
	}

}
