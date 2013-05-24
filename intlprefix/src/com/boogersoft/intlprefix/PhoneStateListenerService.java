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

public class PhoneStateListenerService extends Service
{
	public static void startOrStop(Context context)
	{
		Intent intent = new Intent(context, PhoneStateListenerService.class);
		// start the service only if needed
		if(Preferences.getNotifyOnNetworkCountryChange(context)
			|| Preferences.getNotifyOnNetworkOperatorChange(context))
		{
			context.startService(intent);
		}
		else
		{
			context.stopService(intent);
		}
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

//		Uncomment to test notifications
//		sendNotification(1, getString(
//			R.string.text_newNetworkCountryAndOperator, "JP", "Softbank"));
//		sendNotification(2, getString(
//			R.string.text_newNetworkCountry, "JP"));
//		sendNotification(3, getString(
//			R.string.text_newNetworkOperator, "SoftBank"));

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

		boolean networkCountryChanged = false;
		boolean networkOperatorChanged = false;

		if(newNetworkCountryIso.length() > 0
			&& !newNetworkCountryIso.equals(lastNetworkCountryIso))
		{
			networkCountryChanged = true;
			Preferences.setlastNetworkCountryIso(
				this, newNetworkCountryIso);
		}

		if(newNetworkOperatorName.length() > 0
			&& !newNetworkOperatorName.equals(lastNetworkOperatorName))
		{
			networkOperatorChanged = true;
			Preferences.setlastNetworkOperatorName(
				this, newNetworkOperatorName);
		}

		if(networkCountryChanged
			&& networkOperatorChanged
			&& Preferences.getNotifyOnNetworkCountryChange(this)
			&& Preferences.getNotifyOnNetworkOperatorChange(this))
		{
			sendNotification(1, getString(
				R.string.text_newNetworkCountryAndOperator,
				newNetworkCountryIso + "/" + newNetworkOperatorName));
		}
		else if(networkCountryChanged
			&& Preferences.getNotifyOnNetworkCountryChange(this))
		{
			sendNotification(2, getString(
				R.string.text_newNetworkCountry, newNetworkCountryIso));
		}
		else if(networkOperatorChanged
			&& Preferences.getNotifyOnNetworkOperatorChange(this))
		{
			sendNotification(3, getString(
				R.string.text_newNetworkOperator, newNetworkOperatorName));
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
				.setContentText(getString(R.string.text_tapToAdjustSettings))
				.setAutoCancel(true);
		Intent resultIntent = new Intent(this, PreferencesActivity.class);
		builder.setContentIntent(PendingIntent.getActivity(
				this, 0, resultIntent, PendingIntent.FLAG_UPDATE_CURRENT));
		NotificationManager notificationManager = (NotificationManager)
			this.getSystemService(Context.NOTIFICATION_SERVICE);
		notificationManager.notify(id, builder.build());
	}
}
