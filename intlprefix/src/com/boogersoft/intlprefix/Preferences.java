package com.boogersoft.intlprefix;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class Preferences
{
	private static SharedPreferences getPrefs(Context context)
	{
		return PreferenceManager.getDefaultSharedPreferences(context);
	}

	private static String getStringPreference(Context context, int keyResId,
		int defValueResId)
	{
		String defValue = context.getString(defValueResId);
		return getPrefs(context).getString(
			context.getString(keyResId), defValue);
	}

	private static boolean getBooleanPreference(Context context,
		int keyResId, int defValueResId)
	{
		boolean defValue =
			Boolean.parseBoolean(context.getString(defValueResId));
		return getPrefs(context).getBoolean(
			context.getString(keyResId), defValue);
	}

	public static String getCurrentCountryCode(Context context)
	{
		return getStringPreference(context,
			R.string.pref_currentCountryCode_key,
			R.string.pref_intlPrefix_default);
	}

	public static boolean getConvertToLocal(Context context)
	{
		return getBooleanPreference(context,
			R.string.pref_convertToLocal_key,
			R.string.pref_convertToLocal_default);
	}

	public static String getLocalPrefix(Context context)
	{
		return getStringPreference(context,
			R.string.pref_localPrefix_key,
			R.string.pref_localPrefix_default);
	}

	public static boolean getAddIntlPrefix(Context context)
	{
		return getBooleanPreference(context,
			R.string.pref_addIntlPrefix_key,
			R.string.pref_addIntlPrefix_default);
	}

	public static String getIntlPrefix(Context context)
	{
		return getStringPreference(context,
			R.string.pref_intlPrefix_key,
			R.string.pref_intlPrefix_default);
	}
}
