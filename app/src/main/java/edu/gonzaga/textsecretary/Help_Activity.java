package edu.gonzaga.textsecretary;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;

public class Help_Activity extends FragmentActivity {

	private ViewPager view;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help_);

        //set View Pager (ability to swipe the view from left to right)
        //and set its adapter
        view = (ViewPager) findViewById(R.id.pager);
        view.setAdapter(new Help_Adapter(getSupportFragmentManager()));
    }

}
