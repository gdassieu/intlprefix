package com.boogersoft.intlprefix;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.widget.Toast;

public class CallReceiver extends BroadcastReceiver
{
	public static final String plusSign="+";

	@Override
	public void onReceive(Context context, Intent intent)
	{
		String currentCountryCode = Preferences.getCurrentCountryCode(context);
		boolean addDomesticPrefix = Preferences.getAddDomesticPrefix(context);
		String domesticPrefix = Preferences.getDomesticPrefix(context);
		String domesticSuffix = Preferences.getDomesticSuffix(context);
		boolean addIntlPrefix = Preferences.getAddIntlPrefix(context);
		String intlPrefix = Preferences.getIntlPrefix(context);
		String intlSuffix = Preferences.getIntlSuffix(context);
//		Log.d(getClass().getName(), "currentCountryCode=" + currentCountryCode);
//		Log.d(getClass().getName(), "addDomesticPrefix=" + addDomesticPrefix);
//		Log.d(getClass().getName(), "domesticPrefix=" + domesticPrefix);    	
//		Log.d(getClass().getName(), "addIntlPrefix=" + addIntlPrefix);    	
//		Log.d(getClass().getName(), "intlPrefix=" + intlPrefix);    	

		String dialedNumber = getResultData();
		if(dialedNumber == null)
			dialedNumber = intent.getStringExtra(Intent.EXTRA_PHONE_NUMBER);

		Log.d(getClass().getName(), "Dialed number: " + dialedNumber);

		String correctedNumber = null;

		if(dialedNumber == null)
		{
			// received some crash reports due to null dialedNumber (not sure
			// whether this is normal operation or a whether a 3rd party app is
			// tampering with the broadcast) so let's be defensive
			Log.d(getClass().getName(), "Number is null");
		}
		else if(dialedNumber.startsWith(plusSign + currentCountryCode))
		{
			// '+' followed by current country code -> domestic call
			if(addDomesticPrefix)
			{
				Log.d(getClass().getName(), "Number starts with " + plusSign
					+ currentCountryCode + ", adding domestic prefix");
				correctedNumber = domesticPrefix + dialedNumber.substring(
					plusSign.length() + currentCountryCode.length());
			}
			if(domesticSuffix.length() > 0)
			{
				correctedNumber = (correctedNumber == null?
					dialedNumber: correctedNumber) + domesticSuffix;
			}
		}
		else if(dialedNumber.startsWith(plusSign))
		{
			// '+' followed by something else -> international call
			if(addIntlPrefix)
			{
				Log.d(getClass().getName(), "Number starts with " + plusSign
					+ ", adding international prefix");
				correctedNumber = intlPrefix + dialedNumber.substring(
					plusSign.length());
			}
			if(intlSuffix.length() > 0)
			{
				correctedNumber = (correctedNumber == null?
					dialedNumber: correctedNumber) + intlSuffix;
			}
		}

		if(correctedNumber != null)
		{
			Log.d(getClass().getName(), "Corrected number: " + correctedNumber);

			if(Preferences.getAlternateConversionMethod(context))
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

			if(Preferences.getToastOnDialedNumberChange(context))
			{
				int toastLength = Preferences.getToastDuration(context);
				Toast.makeText(context,
					context.getString(R.string.text_dialedNumberChanged,
						dialedNumber, correctedNumber),
					toastLength).show();
			}
		}
		else
		{
			Log.d(getClass().getName(), "No conversion");
		}
	}
}
