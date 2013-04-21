package com.boogersoft.intlprefix;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.telephony.PhoneStateListener;
import android.telephony.ServiceState;
import android.telephony.TelephonyManager;
import android.util.Log;

public class AutoConfigService extends Service
{
	public static void start(Context context)
	{
		context.startService(
			new Intent(context, AutoConfigService.class));
	}

	private PhoneStateListener listener;
	private TelephonyManager telephonyManager;

	@Override
	public void onCreate()
	{
		Log.d(getClass().getName(), "onCreate");
		//sendDebugNotification("AutoConfig created");

		telephonyManager = (TelephonyManager)getSystemService(
			Context.TELEPHONY_SERVICE);

		// Setup listener
		listener = new PhoneStateListener()
		{
			@Override
			public void onServiceStateChanged(ServiceState serviceState)
			{
				checkServiceState();
			}
		};
		((TelephonyManager)this.getSystemService(TELEPHONY_SERVICE)).listen(
			listener, PhoneStateListener.LISTEN_SERVICE_STATE);

		super.onCreate();
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId)
	{
		super.onStartCommand(intent, flags, startId);
		Log.d(getClass().getName(), "onStartCommand");
		//sendDebugNotification("AutoConfig started");

		// instruct the system to recreate the service if it gets killed
		return START_STICKY;
	}

	@Override
	public void onDestroy()
	{
		Log.d(getClass().getName(), "onDestroy");
		//sendDebugNotification("AutoConfig ended");

		// cleanup listener
		((TelephonyManager)this.getSystemService(TELEPHONY_SERVICE)).listen(
			listener, PhoneStateListener.LISTEN_NONE);

		super.onDestroy();
	}

	@Override
	public IBinder onBind(Intent intent)
	{
		return null;
	}

	private void checkServiceState()
	{
		Log.d(getClass().getName(), "checkServiceState");

		String lastNetworkCountryIso =
			Preferences.getlastNetworkCountryIso(this);
		String lastNetworkOperatorName =
			Preferences.getlastNetworkOperatorName(this);

		String newNetworkCountryIso =
			telephonyManager.getNetworkCountryIso();
		String newNetworkOperatorName =
			telephonyManager.getNetworkOperatorName();

		// a bit of defensive programming
		if(newNetworkCountryIso == null) newNetworkCountryIso = "";
		if(newNetworkOperatorName == null) newNetworkOperatorName = "";

		//sendDebugNotification("SvcStCghd: "
		//	+ newNetworkCountryIso + "/" + newNetworkOperatorName);

		if(newNetworkCountryIso.length() > 0
			&& !newNetworkCountryIso.equals(lastNetworkCountryIso))
		{
			sendNotification(1, "New country: " + newNetworkCountryIso);
			Preferences.setlastNetworkCountryIso(
				this, newNetworkCountryIso);
		}

		if(newNetworkOperatorName.length() > 0
			&& !newNetworkOperatorName.equals(lastNetworkOperatorName))
		{
			sendNotification(2, "New operator: " + newNetworkOperatorName);
			Preferences.setlastNetworkOperatorName(
				this, newNetworkOperatorName);
		}
	}

//	int lastDebugNotificationAutoId = 1000;
//	private void sendDebugNotification(String title)
//	{
//		sendNotification(lastDebugNotificationAutoId++,
//			String.format("%1$tT %2$s", new Date(), title));
//	}

	private void sendNotification(int id, String title)
	{
		NotificationCompat.Builder builder =
			new NotificationCompat.Builder(this)
				.setSmallIcon(R.drawable.icon_intlprefix)
				.setContentTitle(title)
				.setContentText("Tap to adjust IntlPrefix settings")
				.setAutoCancel(true);
		Intent resultIntent = new Intent(this, PreferencesActivity.class);
		builder.setContentIntent(PendingIntent.getActivity(
				this, 0, resultIntent, PendingIntent.FLAG_UPDATE_CURRENT));
		NotificationManager notificationManager = (NotificationManager)
			this.getSystemService(Context.NOTIFICATION_SERVICE);
		notificationManager.notify(id, builder.build());
	}
}
