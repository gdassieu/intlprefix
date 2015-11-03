package com.boogersoft.intlprefix;

import android.content.Context;
import android.util.Log;

public class NumberConverter
{
	public static final String plusSign="+";

	public static String convert(Context context, String dialedNumber)
	{
		String currentCountryCode = Preferences.getCurrentCountryCode(context);
		boolean addDomesticPrefix = Preferences.getAddDomesticPrefix(context);
		String domesticPrefix = Preferences.getDomesticPrefix(context);
		String domesticSuffix = Preferences.getDomesticSuffix(context);
		boolean addIntlPrefix = Preferences.getAddIntlPrefix(context);
		String intlPrefix = Preferences.getIntlPrefix(context);
		String intlSuffix = Preferences.getIntlSuffix(context);

		String correctedNumber = null;

		if(dialedNumber == null)
		{
			// received some crash reports due to null dialedNumber (not sure
			// whether this is normal operation or a whether a 3rd party app is
			// tampering with the broadcast) so let's be defensive
			Log.d(NumberConverter.class.getName(), "Number is null");
		}
		else if(dialedNumber.startsWith(plusSign + currentCountryCode))
		{
			// '+' followed by current country code -> domestic call
			if(addDomesticPrefix)
			{
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
				correctedNumber = intlPrefix + dialedNumber.substring(
					plusSign.length());
			}
			if(intlSuffix.length() > 0)
			{
				correctedNumber = (correctedNumber == null?
					dialedNumber: correctedNumber) + intlSuffix;
			}
		}

		return correctedNumber;
	}

	public static String getSummary(Context context)
	{
		String currentCountryCode = Preferences.getCurrentCountryCode(context);

		String dialedDomesticNumber = "+" + currentCountryCode + "nnnn";
		String correctedDomesticNumber = convert(context, dialedDomesticNumber);

		String dialedIntlNumber = "+ccnnnn";
		String correctedIntlNumber = convert(context, dialedIntlNumber);

		String noChange = context.getString(
			R.string.pref_profileName_summary_noChange);

		return context.getString(R.string.pref_profileName_summary,
			dialedDomesticNumber + " -> " + (correctedDomesticNumber == null?
				noChange: correctedDomesticNumber),
			dialedIntlNumber + " -> " + (correctedIntlNumber == null?
				noChange: correctedIntlNumber));
	}
}
