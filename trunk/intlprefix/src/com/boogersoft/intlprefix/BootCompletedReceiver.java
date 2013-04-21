package com.boogersoft.intlprefix;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class BootCompletedReceiver extends BroadcastReceiver
{
	@Override
	public void onReceive(Context context, Intent intent)
	{
		if(Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction()))
		{
			Log.d(BootCompletedReceiver.class.getName(), "starting service");

			// TODO: only start the service if configured to do so
			AutoConfigService.start(context);
		}
	}
}