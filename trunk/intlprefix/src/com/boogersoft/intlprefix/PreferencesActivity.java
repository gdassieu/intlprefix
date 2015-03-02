package com.boogersoft.intlprefix;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.widget.BaseAdapter;

public class PreferencesActivity extends android.preference.PreferenceActivity
	implements OnSharedPreferenceChangeListener
{
	private static final int DIALOG_ABOUT = 0;

	private static final int ACTIVITY_PROFILE_MANAGER = 1;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		// initialize preferences that have dynamic default values before
		// loading the preferences screen
		Preferences.initializeDynamicDefaultValues(this);

		addPreferencesFromResource(R.xml.preferences);

		getPreferenceScreen().getSharedPreferences()
			.registerOnSharedPreferenceChangeListener(this);
		getPreferenceScreen().findPreference(getString(R.string.cat_load_key))
			.setOnPreferenceClickListener(new OnPreferenceClickListener()
			{
				@Override
				public boolean onPreferenceClick(Preference preference)
				{
					Intent intent = new Intent(PreferencesActivity.this,
						ProfileManagerActivity.class);
					intent.putExtra("SAVE", false);
					startActivityForResult(intent, ACTIVITY_PROFILE_MANAGER);
					//overridePendingTransition(0, 0);
					return true;
				}
			});
		getPreferenceScreen().findPreference(getString(R.string.cat_save_key))
			.setOnPreferenceClickListener(new OnPreferenceClickListener()
			{
				@Override
				public boolean onPreferenceClick(Preference preference)
				{
					Intent intent = new Intent(PreferencesActivity.this,
						ProfileManagerActivity.class);
					intent.putExtra("SAVE", true);
					startActivityForResult(intent, ACTIVITY_PROFILE_MANAGER);
					//overridePendingTransition(0, 0);
					return true;
				}
			});
		getPreferenceScreen().findPreference(getString(R.string.pref_about_key))
			.setOnPreferenceClickListener(new OnPreferenceClickListener()
			{	
				@Override
				public boolean onPreferenceClick(Preference preference)
				{
					showDialog(DIALOG_ABOUT);
					return true;
				}
			});

		updateScreen();
	}

	@Override
	protected void onResume()
	{
		super.onResume();

		if(Preferences.getFirstRun(this) == false)
		{
			showDialog(DIALOG_ABOUT);
			Preferences.setFirstRun(this, true);
		}

		// android does not automatically start services when the app is
		// installed, so we must start it from the PreferencesActivity and
		// from the BootCompletedReceiver
		PhoneStateListenerService.startOrStop(this);
	}

	@Override
	public void onSharedPreferenceChanged(SharedPreferences prefs, String key)
	{
		if(key.equals(getString(R.string.pref_currentCountryCode_key))
			|| key.equals(getString(R.string.pref_addDomesticPrefix_key))
			|| key.equals(getString(R.string.pref_domesticPrefix_key))
			|| key.equals(getString(R.string.pref_addIntlPrefix_key))
			|| key.equals(getString(R.string.pref_intlPrefix_key)))
		{
			Preferences.setProfileDirty(this, true);
			updateScreen();
		}
		else if(key.equals(getString(R.string.pref_toastDuration_key)))
		{
			updateScreen();
		}
		else if(key.equals(getString(
				R.string.pref_notifyOnNetworkCountryChange_key))
			|| key.equals(getString(
				R.string.pref_notifyOnNetworkOperatorChange_key)))
		{
			// start the service if needed
			PhoneStateListenerService.startOrStop(this);
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode,
		Intent data)
	{
		if(requestCode == ACTIVITY_PROFILE_MANAGER)
		{
			recreateCompat();
		}
	}

	private String stringOrBlank(String s)
	{
		return "".equals(s)? getString(
			R.string.pref_profileName_summary_prefixAddBlank): s;
	}

	// TODO: after upgrading to API level 11, remove this method (use API 11
	// recreate method instead)
	private void recreateCompat()
	{
		Intent intent = getIntent();
		//overridePendingTransition(0, 0);
		//intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
		finish();
		//overridePendingTransition(0, 0);
		startActivity(intent);
	}

	private void updateScreen()
	{
		updateTitle(
			R.string.pref_profileName_key,
			R.string.pref_profileName_title,
			Preferences.getProfileName(this),
			Preferences.getProfileDirty(this)? "*": "");
		updateSummary(
			R.string.pref_profileName_key,
			R.string.pref_profileName_summary,
			stringOrBlank(Preferences.getCurrentCountryCode(this)),
			Preferences.getAddDomesticPrefix(this)?
				stringOrBlank(Preferences.getDomesticPrefix(this)):
				getString(R.string.pref_profileName_summary_prefixDontAdd),
			Preferences.getAddIntlPrefix(this)?
				stringOrBlank(Preferences.getIntlPrefix(this)):
				getString(R.string.pref_profileName_summary_prefixDontAdd));
		updateSummary(
			R.string.pref_currentCountryCode_key,
			R.string.pref_currentCountryCode_summary,
			stringOrBlank(Preferences.getCurrentCountryCode(this)));
		updateSummary(
			R.string.pref_domesticPrefix_key,
			R.string.pref_domesticPrefix_summary,
			stringOrBlank(Preferences.getDomesticPrefix(this)));
		updateSummary(
			R.string.pref_intlPrefix_key,
			R.string.pref_intlPrefix_summary,
			stringOrBlank(Preferences.getIntlPrefix(this)));
		updateSummary(
			R.string.pref_toastDuration_key,
			R.string.pref_toastDuration_summary,
			((ListPreference)getPreferenceScreen().findPreference(
					getString(R.string.pref_toastDuration_key))).getEntry());

		// needed to force refresh profile summary in the main screen after
		// profile settings are changed in the profile edit screen
		((BaseAdapter)getPreferenceScreen().getRootAdapter())
			.notifyDataSetChanged();
	}

	private void updateTitle(int keyResId, int titleResId, Object... values)
	{
		String key = getString(keyResId);
		Preference pref = getPreferenceScreen().findPreference(key);
		pref.setTitle(getString(titleResId, values));
	}

	private void updateSummary(int keyResId, int summaryResId, Object... values)
	{
		String key = getString(keyResId);
		Preference pref = getPreferenceScreen().findPreference(key);
		pref.setSummary(getString(summaryResId, values));
	}

	private String getVersionName()
	{
		try
		{
			return "v" + getPackageManager().getPackageInfo(
				getPackageName(), 0).versionName;
		}
		catch(PackageManager.NameNotFoundException e)
		{
			return "v{unknown}";
		}
	}

	@Override
	protected Dialog onCreateDialog(int id)
	{
		Dialog dialog = null;
		switch(id)
		{
		case DIALOG_ABOUT:
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setMessage(getString(R.string.text_about, getVersionName(),
				getString(R.string.pref_alternateConversionMethod_title),
				android.os.Build.PRODUCT, android.os.Build.VERSION.SDK_INT));
			builder.setPositiveButton(R.string.button_ok,
				new DialogInterface.OnClickListener()
				{
					public void onClick(DialogInterface dialog, int which)
					{
					}
				});
			dialog = builder.create();
		}
		return dialog;
	}
}