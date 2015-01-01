package edu.gonzaga.textsecretary;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

public class Help_Adapter extends FragmentPagerAdapter{

	public Help_Adapter(FragmentManager fm) {
		super(fm);
	}

	@Override
	public Fragment getItem(int position) {
        switch (position) {
            case 0:
                return new HelpCalendarFragment();
            case 1:
                return new HelpDrivingFragment();
            case 2:
                return new HelpSleepFragment();
            case 3:
                return new HelpWidgetFragment();
            case 4:
                return new HelpSilencerFragment();
            case 5:
                return new HelpAboutFragment();
        }
        return null;
	}

    @Override
    public CharSequence getPageTitle(int position) {
        switch (position) {
            case 0:
                return "Calendar";
            case 1:
                return "Driving";
            case 2:
                return "Single Response";
            case 3:
                return "Widget";
            case 4:
                return "Do Not Disturb";
            case 5:
                return "About";
        }
        return null;
    }

	@Override
	public int getCount() {
		return 6;
	}

}
