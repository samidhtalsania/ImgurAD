package com.blueleaf.imguralbumdownloader;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;

public class settingsActivity extends PreferenceActivity{
	
	String path, downloadLocation ;	
	boolean isOnlyWifiChecked ;
	private static final String DEFAULT_LOCATION = Environment.DIRECTORY_DOWNLOADS;

	private static final String DLOC = "dloc";
	private static final String NETWORK_PREF = "network_pref";
	
	@SuppressLint("NewApi")
	@SuppressWarnings("deprecation")
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);
        
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
            getActionBar().setDisplayHomeAsUpEnabled(true);
        
        final Context mContext = this ;
        
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
		downloadLocation = sharedPref.getString(DLOC, DEFAULT_LOCATION);
		isOnlyWifiChecked = sharedPref.getBoolean(NETWORK_PREF, false);
        
        Preference locPref = (Preference) findPreference(DLOC);
        locPref.setSummary(downloadLocation);
        locPref.setOnPreferenceClickListener(new OnPreferenceClickListener(){

			@Override
			public boolean onPreferenceClick(Preference pref) {
				Intent i = new Intent(mContext, DirectoryPicker.class);
				startActivityForResult(i, DirectoryPicker.PICK_DIRECTORY) ;
				return false;
			}
		});
        
        final CheckBoxPreference networkPref = (CheckBoxPreference)findPreference(NETWORK_PREF);
        networkPref.setOnPreferenceClickListener(new OnPreferenceClickListener(){

			@Override
			public boolean onPreferenceClick(Preference preference) {
				
				if( networkPref.isChecked() )
				{
					SetSharedPreference(NETWORK_PREF, true);
				}
				else
				{
					SetSharedPreference(NETWORK_PREF, false);
				}
				return false;
			}
		});
    }
	
	@SuppressWarnings("deprecation")
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode == RESULT_OK && requestCode == DirectoryPicker.PICK_DIRECTORY) {
			Bundle extras = data.getExtras();
			path = (String) extras.get(DirectoryPicker.CHOSEN_DIRECTORY);
			SetSharedPreference(DLOC,path);
		    Preference locPref = (Preference) findPreference(DLOC);
		    locPref.setSummary(path);
		}
		
	}
	
	private void SetSharedPreference(String KEY, String VALUE)
	{
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
	    SharedPreferences.Editor editor = preferences.edit();
	    editor.putString(KEY, VALUE);
	    editor.commit();
	}
	
	private void SetSharedPreference(String KEY, boolean VALUE)
	{
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
	    SharedPreferences.Editor editor = preferences.edit();
	    editor.putBoolean(KEY, VALUE);
	    editor.commit();
	}
	  
}
