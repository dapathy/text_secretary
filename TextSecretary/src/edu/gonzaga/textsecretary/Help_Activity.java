package edu.gonzaga.textsecretary;

import android.app.ActionBar;
import android.app.ActionBar.Tab;
import android.app.ActionBar.TabListener;
import android.app.FragmentTransaction;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.widget.TextView;

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

		//set View Pager (ability to swipe the view from left to right)
		//and set its adapter
		view = (ViewPager) findViewById(R.id.pager);
		view.setAdapter(new Help_Adapter(getSupportFragmentManager()));
		view.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {		
			@Override
			public void onPageSelected(int arg0) {
				//when a page is swiped to, change the tab to that
				//corresponding page
				actionBar.setSelectedNavigationItem(arg0);
			}
			
			@Override
			public void onPageScrolled(int arg0, float arg1, int arg2) {}
			
			@Override
			public void onPageScrollStateChanged(int arg0) {}
		});
		
		//Get action bar and set it to tab mode
		actionBar = getActionBar();
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
		
		//show the launcher icon in the action bar and change the title
		actionBar.setDisplayShowTitleEnabled(true);
		actionBar.setTitle("Help and Information");
		actionBar.setDisplayUseLogoEnabled(false);
					
		createTabs();
		addTabs();
		
	}

	@Override
	public void onTabReselected(Tab tab, FragmentTransaction arg1) {}

	@Override
	public void onTabSelected(Tab tab, FragmentTransaction arg1) {
		//when a tab is selected, move the view page to that tab
		view.setCurrentItem(tab.getPosition());
	}

	@Override
	public void onTabUnselected(Tab tab, FragmentTransaction arg1) {}


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
