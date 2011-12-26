package com.boogersoft.intlprefix;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;

public class CallReceiver extends BroadcastReceiver
{
	public static final String plusSign="+";

	/***
	 * Some HTC phones are buggy and ignore setResultData call. The workaround
	 * is to cancel the call and create a new one. This method returns true
	 * on phone models where this workaround is needed.
	 * @return true if the workaround is needed on this phone, false otherwise
	 */
	private boolean useAlternateMethod()
	{
		String product = android.os.Build.PRODUCT;
		int sdk_int = android.os.Build.VERSION.SDK_INT;
		return
			// HTC Desire on Android 2.2
			(product.equals("htc_bravo") && sdk_int == 8)
			// HTC Desire S on Android 2.3.3
			|| (product.equals("htc_saga") && sdk_int == 10)
			// HTC Wildfire S on Android 2.3.3
			|| (product.equals("htc_marvel") && sdk_int == 10)
			// HTC Sensation on Android 2.3.3
			|| (product.equals("htc_pyramid") && sdk_int == 10)
			;
	}

	@Override
	public void onReceive(Context context, Intent intent)
	{
		String currentCountryCode = Preferences.getCurrentCountryCode(context);
		boolean convertToLocal = Preferences.getConvertToLocal(context);
		String localPrefix = Preferences.getLocalPrefix(context);
		boolean addIntlPrefix = Preferences.getAddIntlPrefix(context);
		String intlPrefix = Preferences.getIntlPrefix(context);
//		SharedPreferences prefs =
//			PreferenceManager.getDefaultSharedPreferences(context);
//		String currentCountryCode = prefs.getString(
//				context.getString(R.string.pref_currentCountryCode_key), "");
//		boolean convertToLocal = prefs.getBoolean(
//				context.getString(R.string.pref_convertToLocal_key), false);
//		boolean addIntlPrefix = prefs.getBoolean(
//				context.getString(R.string.pref_addIntlPrefix_key), false);
//		String intlPrefix = prefs.getString(
//				context.getString(R.string.pref_intlPrefix_key), "00");
		Log.d(getClass().getName(), "currentCountryCode=" + currentCountryCode);
		Log.d(getClass().getName(), "convertToLocal=" + convertToLocal);
		Log.d(getClass().getName(), "localPrefix=" + localPrefix);    	
		Log.d(getClass().getName(), "addIntlPrefix=" + addIntlPrefix);    	
		Log.d(getClass().getName(), "intlPrefix=" + intlPrefix);    	

		String dialedNumber = intent.getStringExtra(Intent.EXTRA_PHONE_NUMBER);
		Log.d(getClass().getName(), "Dialed number: " + dialedNumber);

		String correctedNumber = null;

		if(dialedNumber.startsWith(plusSign + currentCountryCode))
		{
			if(convertToLocal)
			{
				Log.d(getClass().getName(), "Number starts with " + plusSign
					+ currentCountryCode + ", converting to local");
				correctedNumber = localPrefix + dialedNumber.substring(
					plusSign.length() + currentCountryCode.length());
			}
		}
		else if(dialedNumber.startsWith(plusSign))
		{
			if(addIntlPrefix)
			{
				Log.d(getClass().getName(), "Number starts with " + plusSign
					+ ", adding international prefix");
				correctedNumber = intlPrefix + dialedNumber.substring(
					plusSign.length());
			}
		}

		if(correctedNumber != null)
		{
			Log.d(getClass().getName(), "Corrected number: " + correctedNumber);

			if(useAlternateMethod())
			{
				// Cancel current call and make a new one on (buggy) phones that
				// ignore call to setResultData
				Log.d(getClass().getName(), "Workaround activated");
				setResultData(null);
				Intent callIntent = new Intent(Intent.ACTION_CALL,
					Uri.parse("tel:" + correctedNumber));
				callIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				context.startActivity(callIntent);
			}
			else
			{
				// All other models: just setResultData to the new phone number
				setResultData(correctedNumber);
			}
		}
		else
		{
			Log.d(getClass().getName(), "No conversion");
		}
	}
}
