package edu.gonzaga.textsecretary;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

public class Help_Adapter extends FragmentPagerAdapter{

	public Help_Adapter(FragmentManager fm) {
		super(fm);
		// TODO Auto-generated constructor stub
	}

	@Override
	public Fragment getItem(int tab) {
		// TODO Auto-generated method stub
		Fragment fragment = null;
		if(tab == 0){
			fragment = new HelpCalendarFragment();
		}
		else if(tab == 1){
			fragment = new HelpSleepFragment();
		}
		else if(tab == 2){
			fragment = new HelpAboutFragment();
		}
		return fragment;
	}

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return 3;
	}

}
