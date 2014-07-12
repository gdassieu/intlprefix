package com.boogersoft.intlprefix;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

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

	private static void putStringPreference(Context context, int keyResId,
		String value)
	{
		SharedPreferences.Editor editor = getPrefs(context).edit();
		editor.putString(context.getString(keyResId), value);
		editor.commit();
	}

	private static boolean getBooleanPreference(Context context,
		int keyResId, boolean defValue)
	{
		return getPrefs(context).getBoolean(
				context.getString(keyResId), defValue);
	}

	private static boolean getBooleanPreference(Context context,
		int keyResId, int defValueResId)
	{
		boolean defValue =
			Boolean.parseBoolean(context.getString(defValueResId));
		return getBooleanPreference(context, keyResId, defValue);
	}

	private static void putBooleanPreference(Context context, int keyResId,
		boolean value)
	{
		SharedPreferences.Editor editor = getPrefs(context).edit();
		editor.putBoolean(context.getString(keyResId), value);
		editor.commit();
	}

	private static void deletePreference(Context context, int keyResId)
	{
		SharedPreferences.Editor editor = getPrefs(context).edit();
		editor.remove(context.getString(keyResId));
		editor.commit();
	}

	public static boolean getFirstRun(Context context)
	{
		return getBooleanPreference(context,
			R.string.pref_firstrun_key,
			R.string.pref_firstrun_default);
	}

	public static void setFirstRun(Context context, boolean value)
	{
		putBooleanPreference(context, R.string.pref_firstrun_key, value);
	}

	public static String getProfileName(Context context)
	{
		return getStringPreference(context,
			R.string.pref_profileName_key,
			R.string.pref_profileName_default);
	}

	public static void setProfileName(Context context, String value)
	{
		if(value == null)
			deletePreference(context, R.string.pref_profileName_key);
		else
			putStringPreference(context, R.string.pref_profileName_key, value);
	}

	public static String getCurrentCountryCode(Context context)
	{
		return getStringPreference(context,
			R.string.pref_currentCountryCode_key,
			R.string.pref_intlPrefix_default);
	}

	public static void setCurrentCountryCode(Context context, String value)
	{
		putStringPreference(
			context, R.string.pref_currentCountryCode_key, value);
	}

	// TODO: rename to getAddDomesticPrefix for consistency with UI
	public static boolean getConvertToLocal(Context context)
	{
		return getBooleanPreference(context,
			R.string.pref_convertToLocal_key,
			R.string.pref_convertToLocal_default);
	}

	// TODO: rename to setAddDomesticPrefix for consistency with UI
	public static void setConvertToLocal(Context context, boolean value)
	{
		putBooleanPreference(context, R.string.pref_convertToLocal_key, value);
	}

	// TODO: rename to getDomesticPrefix for consistency with UI
	public static String getLocalPrefix(Context context)
	{
		return getStringPreference(context,
			R.string.pref_localPrefix_key,
			R.string.pref_localPrefix_default);
	}

	// TODO: rename to setDomesticPrefix for consistency with UI
	public static void setLocalPrefix(Context context, String value)
	{
		putStringPreference(context, R.string.pref_localPrefix_key, value);
	}

	public static boolean getAddIntlPrefix(Context context)
	{
		return getBooleanPreference(context,
			R.string.pref_addIntlPrefix_key,
			R.string.pref_addIntlPrefix_default);
	}

	public static void setAddIntlPrefix(Context context, boolean value)
	{
		putBooleanPreference(context, R.string.pref_addIntlPrefix_key, value);
	}

	public static String getIntlPrefix(Context context)
	{
		return getStringPreference(context,
			R.string.pref_intlPrefix_key,
			R.string.pref_intlPrefix_default);
	}

	public static void setIntlPrefix(Context context, String value)
	{
		putStringPreference(context, R.string.pref_intlPrefix_key, value);
	}

	public static String getlastNetworkCountryIso(Context context)
	{
		return getStringPreference(context,
			R.string.pref_lastNetworkCountryIso_key,
			R.string.pref_lastNetworkCountryIso_default);
	}

	public static void setlastNetworkCountryIso(Context context, String value)
	{
		Log.d(Preferences.class.getName(),
			"setLastNetworkCountryIso(" + value + ")");
		putStringPreference(context,
			R.string.pref_lastNetworkCountryIso_key,
			value);
	}

	public static String getlastNetworkOperatorName(Context context)
	{
		return getStringPreference(context,
			R.string.pref_lastNetworkOperatorName_key,
			R.string.pref_lastNetworkOperatorName_default);
	}

	public static void setlastNetworkOperatorName(Context context, String value)
	{
		Log.d(Preferences.class.getName(),
			"setLastNetworkOperatorName(" + value + ")");
		putStringPreference(context,
			R.string.pref_lastNetworkOperatorName_key,
			value);
	}

	public static boolean getNotifyOnNetworkCountryChange(Context context)
	{
		return getBooleanPreference(context,
			R.string.pref_notifyOnNetworkCountryChange_key,
			R.string.pref_notifyOnNetworkCountryChange_default);
	}

	public static boolean getNotifyOnNetworkOperatorChange(Context context)
	{
		return getBooleanPreference(context,
			R.string.pref_notifyOnNetworkOperatorChange_key,
			R.string.pref_notifyOnNetworkOperatorChange_default);
	}

	public static boolean getToastOnDialedNumberChange(Context context)
	{
		return getBooleanPreference(context,
			R.string.pref_toastOnDialedNumberChange_key,
			R.string.pref_toastOnDialedNumberChange_default);
	}

	public static void setAlternateConversionMethod(
		Context context, boolean value)
	{
		putBooleanPreference(context,
			R.string.pref_alternateConversionMethod_key,
			value);
	}

	public static boolean getProfileDirty(Context context)
	{
		return getBooleanPreference(context,
			R.string.pref_profileDirty_key,
			R.string.pref_profileDirty_default);
	}

	public static void setProfileDirty(Context context, boolean value)
	{
		putBooleanPreference(context,
			R.string.pref_profileDirty_key,
			value);
	}

	public static boolean getAlternateConversionMethod(Context context)
	{
		String product = android.os.Build.PRODUCT;
		int sdk_int = android.os.Build.VERSION.SDK_INT;
		boolean knownBuggyPhone = 
			// Many (Most/All?) HTC phones on Android > 2.2
			(product.startsWith("htc_") && sdk_int >= 8)
			// SHARP AQUOS 102SHII
			|| product.equals("SBM102SH2");

		return getBooleanPreference(context,
			R.string.pref_alternateConversionMethod_key,
			knownBuggyPhone);
	}

	public static void initializeDynamicDefaultValues(Context context)
	{
		Preferences.setAlternateConversionMethod(
			context, Preferences.getAlternateConversionMethod(context));
	}
}
