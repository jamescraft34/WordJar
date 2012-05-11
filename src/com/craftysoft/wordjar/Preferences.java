package com.craftysoft.wordjar;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.preference.Preference.OnPreferenceClickListener;

import com.craftysoft.wordjar.db.DBTasker;

public class Preferences extends PreferenceActivity  {

	private enum PreferenceType
	{		
		RESET("Reset all WordJar statistics?"),
		DELETE("Delete all WordJar words?");
	
		public String message = "";
		
		PreferenceType(String msg)
		{
			message = msg;
		}
	}
	
	private ProgressDialog _progressDialog = null;
	
	private SharedPreferences _sp = null;
		
	private TaskListener _deleteAllWordsTaskListener = new TaskListener()
	{
			@Override
			public void performAction(BaseWord word, Object obj) 
			{
				//call back to main activity...
				WordJarActivity.wordjarActivity.clearWordMap();
				WordJarActivity.wordjarActivity.refreshUI(false);
			
				try {
					Thread.sleep(300L);//just for the user to see something...
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				if(_progressDialog.isShowing())
					_progressDialog.dismiss();
		}
	};
	
	private TaskListener _resetAllWordsTaskListener = new TaskListener()
	{
			@Override
			public void performAction(BaseWord word, Object obj) 
			{
				//call back to main activity...
				WordJarActivity.wordjarActivity.initializeWordMap(false);
				WordJarActivity.wordjarActivity.refreshUI(false);
			
				try {
					Thread.sleep(300L);//just for the user to see something...
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				if(_progressDialog.isShowing())
					_progressDialog.dismiss();
		}
	};

	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);  
        
		_progressDialog = new ProgressDialog(this);
        
        //register listener for preference box entry handling
        _sp = PreferenceManager.getDefaultSharedPreferences(this);

        CheckBoxPreference alertCheckboxPref = (CheckBoxPreference)findPreference("checkbox_alerts");
    	alertCheckboxPref.setChecked(_sp.getBoolean("checkbox_alerts", true));
    	
    	CheckBoxPreference vibrationCheckboxPref = (CheckBoxPreference)findPreference("checkbox_vibration");
    	vibrationCheckboxPref.setChecked(_sp.getBoolean("checkbox_vibration", true));
        
    	Preference resetPref = findPreference("preference_reset");
    	resetPref.setOnPreferenceClickListener(resetPrefClickListener);
    	
    	Preference deletePref = findPreference("preference_delete");
    	deletePref.setOnPreferenceClickListener(deletePrefClickListener);
   }


    private OnPreferenceClickListener resetPrefClickListener = new OnPreferenceClickListener()
    {
		@Override
		public boolean onPreferenceClick(Preference preference) {

			displayAlertDialog(PreferenceType.RESET);
	        return false;
		}     
	};
	
    private OnPreferenceClickListener deletePrefClickListener = new OnPreferenceClickListener()
    {
		@Override
		public boolean onPreferenceClick(Preference preference) {

			displayAlertDialog(PreferenceType.DELETE);
	        return false;
		}     
	};
	
	private void displayAlertDialog(PreferenceType type)
	{
		final PreferenceType myType = type;
		
        new AlertDialog.Builder(Preferences.this) 
        .setTitle(type.message) 
        .setIcon(getResources().getDrawable(R.drawable.action_droid))
        .setPositiveButton("Ok", new DialogInterface.OnClickListener() 
        {
            @Override 
            public void onClick(DialogInterface dialog, int which) 
            { 
            	if(myType == PreferenceType.DELETE)
            	{            		
					new DBTasker(getApplicationContext()).new DeleteAllWordsTask(_progressDialog, _deleteAllWordsTaskListener).execute();
            	}
            	else if(myType == PreferenceType.RESET)
            	{
					new DBTasker(getApplicationContext()).new ResetAllWordsTask(_progressDialog, _resetAllWordsTaskListener).execute();
            	}
            } 
        }) 
        .setNegativeButton("Cancel", null)
        .show(); 
	}
}
