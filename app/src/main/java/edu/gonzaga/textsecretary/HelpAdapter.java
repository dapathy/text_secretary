package edu.gonzaga.textsecretary;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

public class HelpAdapter extends FragmentPagerAdapter {

	public HelpAdapter(FragmentManager fm) {
		super(fm);
	}

	@Override
	public Fragment getItem(int tab) {
		Fragment fragment = null;
		if (tab == 0) {
			fragment = new HelpCalendarFragment();
		} else if (tab == 1) {
			fragment = new HelpDrivingFragment();
		} else if (tab == 2) {
			fragment = new HelpSleepFragment();
		} else if (tab == 3) {
			fragment = new HelpWidgetFragment();
		} else if (tab == 4) {
			fragment = new HelpSilencerFragment();
		} else if (tab == 5) {
			fragment = new HelpAboutFragment();
		}
		return fragment;
	}

	@Override
	public int getCount() {
		return 6;
	}

}
