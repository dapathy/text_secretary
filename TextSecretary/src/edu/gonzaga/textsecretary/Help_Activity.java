package edu.gonzaga.textsecretary;

import android.app.ActionBar;
import android.app.ActionBar.Tab;
import android.app.ActionBar.TabListener;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.util.Log;

public class Help_Activity extends FragmentActivity implements TabListener {

	ActionBar actionBar;
	ViewPager view;
	ActionBar.Tab calendarTab;
	ActionBar.Tab sleepTab;
	ActionBar.Tab aboutTab;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_help_);

		view = (ViewPager) findViewById(R.id.pager);
		view.setAdapter(new Help_Adapter(getSupportFragmentManager()));
		view.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {		
			@Override
			public void onPageSelected(int arg0) {
				// TODO Auto-generated method stub
				actionBar.setSelectedNavigationItem(arg0);
			}
			
			@Override
			public void onPageScrolled(int arg0, float arg1, int arg2) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void onPageScrollStateChanged(int arg0) {
				// TODO Auto-generated method stub
				
			}
		});
		
		actionBar = getActionBar();
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
		actionBar.setDisplayShowTitleEnabled(true);
		actionBar.setTitle("Help and Information");
		actionBar.setDisplayUseLogoEnabled(false);
					
		createTabs();
		addTabs();
		
	}

	@Override
	public void onTabReselected(Tab tab, FragmentTransaction arg1) {
		// TODO Auto-generated method stub
	}

	@Override
	public void onTabSelected(Tab tab, FragmentTransaction arg1) {
		view.setCurrentItem(tab.getPosition());
	}

	@Override
	public void onTabUnselected(Tab tab, FragmentTransaction arg1) {
	}


	private void createTabs(){
		calendarTab = actionBar.newTab();
		calendarTab.setText("Calendar");
		calendarTab.setTabListener(this);
		
		sleepTab = actionBar.newTab();
		sleepTab.setText("Sleep");
		sleepTab.setTabListener(this);
		
		aboutTab = actionBar.newTab();
		aboutTab.setText("About");
		aboutTab.setTabListener(this);
	}
	
	private void addTabs(){
		actionBar.addTab(calendarTab);
		actionBar.addTab(sleepTab);
		actionBar.addTab(aboutTab);
	}
	

}
