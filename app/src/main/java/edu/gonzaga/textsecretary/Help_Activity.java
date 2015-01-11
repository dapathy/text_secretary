package edu.gonzaga.textsecretary;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;

import com.astuetz.PagerSlidingTabStrip;

public class Help_Activity extends FragmentActivity {

	private ViewPager viewPager;
    PagerSlidingTabStrip tabs;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help_);

        //set View Pager (ability to swipe the view from left to right)
        //and set its adapter
        viewPager = (ViewPager) findViewById(R.id.pager);
        viewPager.setAdapter(new Help_Adapter(getSupportFragmentManager()));

        // Give the SlidingTabLayout the ViewPager, this must be done AFTER the ViewPager has had
        // it's PagerAdapter set.
        tabs = (PagerSlidingTabStrip) findViewById(R.id.sliding_tabs);
        tabs.setViewPager(viewPager);
    }
}
