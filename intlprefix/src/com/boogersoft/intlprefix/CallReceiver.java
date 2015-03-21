package com.boogersoft.intlprefix;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.widget.Toast;

public class CallReceiver extends BroadcastReceiver
{
	@Override
	public void onReceive(Context context, Intent intent)
	{
		String dialedNumber = getResultData();
		if(dialedNumber == null)
			dialedNumber = intent.getStringExtra(Intent.EXTRA_PHONE_NUMBER);

		Log.d(getClass().getName(), "Dialed number: " + dialedNumber);

		String correctedNumber = NumberConverter.convert(context, dialedNumber);

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
					Uri.parse("tel:" + Uri.encode(correctedNumber)));
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
