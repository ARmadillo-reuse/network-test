package edu.mit.armadillo.networktest;

import edu.mit.armadillo.networktest.download.DownloadFragment;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;

public class MainActivity extends FragmentActivity {

	private MainFragment mainFragment;

	@Override
	protected void onCreate(Bundle arg0) {
		super.onCreate(arg0);
		setContentView(R.layout.main_activity);
		this.mainFragment = new MainFragment();
		getSupportFragmentManager().beginTransaction()
				.add(R.id.main_fragment, mainFragment).commit();
	}

	public void startDownload() {
		String url = mainFragment.getDownloadURL();
		FragmentManager fm = getSupportFragmentManager();
		FragmentTransaction ft = fm.beginTransaction();
		Fragment downloadFragment = new DownloadFragment();
		Bundle arguments = new Bundle();
		arguments.putString("URL", url);
		downloadFragment.setArguments(arguments);
		ft.replace(R.id.main_fragment, downloadFragment);
		ft.addToBackStack("main-to-download");
		ft.commit();
	}

	public void onDownloadError() {
		getSupportFragmentManager().popBackStack();
	}
}
