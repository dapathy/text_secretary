package edu.gonzaga.textsecretary;

import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

/**
 * A simple {@link android.support.v4.app.Fragment} subclass.
 * 
 */
public class HelpAboutFragment extends Fragment {

	public HelpAboutFragment() {
		// Required empty public constructor
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// Inflate the layout for this fragment
	    View myInflatedView =  inflater.inflate(R.layout.fragment_help_about, container, false);
	    TextView t = (TextView) myInflatedView.findViewById(R.id.aboutVersion);
	    String versionName = null;
		try {
			versionName = getActivity().getPackageManager().getPackageInfo(getActivity().getPackageName(), 0).versionName;
		} catch (NameNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	    t.setText("Version: "+ versionName);
		return myInflatedView;
	}
	
    public void setText(String text){
        TextView textView = (TextView) getView().findViewById(R.id.aboutVersion);
        textView.setText(text);
    }

    public void onActivityCreated(){
    	super.onActivityCreated(getArguments());
    	setText("TEST");
    }
}
