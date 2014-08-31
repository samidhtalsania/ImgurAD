package com.example.imguralbumdownloader;

import android.app.Activity;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;

public class SettingsActivity extends Activity implements  OnPreferenceClickListener{
	
	 @Override
	 protected void onCreate(Bundle savedInstanceState) {
	        super.onCreate(savedInstanceState);
	 
	 
		     if (savedInstanceState == null) {
		         // During initial setup, plug in the details fragment.
//		         AppPreferences details = new AppPreferences();
		         //details.setArguments(getIntent().getExtras());
//		         getFragmentManager().beginTransaction().add(android.R.id.content, details).commit();
		     	}
	 }

	@Override
	public boolean onPreferenceClick(Preference preference) {
		if(preference.getKey().equalsIgnoreCase("dloc"))
		{
			//TODO add code for file browser
		}
		
		return true;
	}



}
