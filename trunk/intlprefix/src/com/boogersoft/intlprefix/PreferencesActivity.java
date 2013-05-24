package com.boogersoft.intlprefix;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.preference.Preference;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

public class PreferencesActivity extends android.preference.PreferenceActivity
	implements OnSharedPreferenceChangeListener
{
	private static final int DIALOG_ABOUT = 0;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.preferences);
		getPreferenceScreen().getSharedPreferences()
			.registerOnSharedPreferenceChangeListener(this);
		updateSummaries();
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
			|| key.equals(getString(R.string.pref_localPrefix_key))
			|| key.equals(getString(R.string.pref_intlPrefix_key)))
		{
			updateSummaries();
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

	private String stringOrNone(String s)
	{
		return "".equals(s)? "(none)": s;
	}

	private void updateSummaries()
	{
		updateSummary(
			R.string.pref_currentCountryCode_key,
			R.string.pref_currentCountryCode_summary,
			stringOrNone(Preferences.getCurrentCountryCode(this)));
		updateSummary(
			R.string.pref_localPrefix_key,
			R.string.pref_localPrefix_summary,
			stringOrNone(Preferences.getLocalPrefix(this)));
		updateSummary(
			R.string.pref_intlPrefix_key,
			R.string.pref_intlPrefix_summary,
			stringOrNone(Preferences.getIntlPrefix(this)));
	}

	private void updateSummary(int keyResId, int summaryResId,
		Object value)
	{
		String key = getString(keyResId);
		Preference pref = getPreferenceScreen().findPreference(key);
		pref.setSummary(getString(summaryResId, value));
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		if(item.getItemId() == R.id.menu_about)
		{
			showDialog(DIALOG_ABOUT);
			return true;
		}
		return false;
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
