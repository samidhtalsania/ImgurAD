package com.blueleaf.imguralbumdownloader;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;

public class settingsActivity extends PreferenceActivity{
	
	String path, downloadLocation ;	
	
	private static final String DEFAULT_LOCATION = Environment.DIRECTORY_DOWNLOADS;

	private static final String KEY = "dloc";
	
	@SuppressLint("NewApi")
	@SuppressWarnings("deprecation")
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);
        // show the current value in the settings screen
        
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
            getActionBar().setDisplayHomeAsUpEnabled(true);
        
        final Context mContext = this ;
        
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
		downloadLocation = sharedPref.getString(KEY, DEFAULT_LOCATION);
        
        Preference locPref = (Preference) findPreference(KEY);
        locPref.setSummary(downloadLocation);
        locPref.setOnPreferenceClickListener(new OnPreferenceClickListener(){

			@Override
			public boolean onPreferenceClick(Preference pref) {
				Intent i = new Intent(mContext, DirectoryPicker.class);
				startActivityForResult(i, DirectoryPicker.PICK_DIRECTORY) ;
				return false;
			}});
        
        
    }
	
	@SuppressWarnings("deprecation")
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode == RESULT_OK && requestCode == DirectoryPicker.PICK_DIRECTORY) {
			Bundle extras = data.getExtras();
			path = (String) extras.get(DirectoryPicker.CHOSEN_DIRECTORY);
			SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
		    SharedPreferences.Editor editor = preferences.edit();
		    editor.putString(KEY, path);
		    editor.commit();
		    Preference locPref = (Preference) findPreference(KEY);
		    locPref.setSummary(path);
		}
		
	}
	  
}
