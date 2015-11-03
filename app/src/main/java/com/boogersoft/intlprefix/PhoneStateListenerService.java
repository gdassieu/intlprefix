package com.boogersoft.intlprefix;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.telephony.PhoneStateListener;
import android.telephony.ServiceState;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;

/* This service serves two purposes:
 * 1. catch new outgoing call broadcast and modify the outgoing call number.
 * 2. listen for phone service state change (ex: new country, new operator) and
 *    send notification so that user adjusts IntlPrefix settings as needed.
 * */

public class PhoneStateListenerService extends Service
{
	public static void restart(Context context)
	{
		Intent intent = new Intent(context, PhoneStateListenerService.class);
		context.stopService(intent);
		context.startService(intent);
	}

	private ServiceStateChangeListener listener;
	private OutgoingCallReceiver receiver;

	@Override
	public void onCreate()
	{
		Log.d(getClass().getName(), "onCreate");
		//sendDebugNotification("AutoConfig created");

		// register receiver and listener
		receiver = new OutgoingCallReceiver(this);
		receiver.register();
		listener = new ServiceStateChangeListener(this);
		listener.register();

		super.onCreate();
	}

	@Override
	public void onDestroy()
	{
		Log.d(getClass().getName(), "onDestroy");
		//sendDebugNotification("AutoConfig ended");

		// unregister listener and receiver
		if(listener != null) listener.unregister();
		if(receiver != null) receiver.unregister();

		super.onDestroy();
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
	public IBinder onBind(Intent intent)
	{
		return null;
	}

	/* new outgoing call broadcast receiver */

	private class OutgoingCallReceiver extends BroadcastReceiver
	{
		Context context;

		public OutgoingCallReceiver(Context context)
		{
			super();
			this.context = context;
		}

		public void register()
		{
			int priority = Preferences.getCallReceiverPriority(context);
			Log.d(getClass().getName(), "Registering, priority=" + priority);
			IntentFilter filter = new IntentFilter();
			filter.addAction("android.intent.action.NEW_OUTGOING_CALL");
			filter.setPriority(priority);
			registerReceiver(this, filter);
		}

		public void unregister()
		{
			Log.d(getClass().getName(), "Unregistering");
			unregisterReceiver(this);
		}

		@Override
		public void onReceive(Context context, Intent intent)
		{
			String dialedNumber = getResultData();
			if(dialedNumber == null)
				return;

			Log.d(getClass().getName(), "Dialed number: " + dialedNumber);

			String correctedNumber = NumberConverter.convert(context,
				dialedNumber);

			if(correctedNumber != null)
			{
				Log.d(getClass().getName(), "Corrected number: "
					+ correctedNumber);

				if(Preferences.getAlternateConversionMethod(context))
				{
					// Alternate method for buggy phones that do not respect
					// the API: cancel current call and make a new one on
					Log.d(getClass().getName(), "Workaround activated");
					setResultData(null);
					Intent callIntent = new Intent(Intent.ACTION_CALL,
						Uri.parse("tel:" + Uri.encode(correctedNumber)));
					callIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
					context.startActivity(callIntent);
				}
				else
				{
					// Standard method: just setResultData to the new number
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

	/* phone service state change listener */

	private class ServiceStateChangeListener extends PhoneStateListener
	{
		private TelephonyManager telephonyManager;
		private Context context;

		public ServiceStateChangeListener(Context context)
		{
			super();
			this.context = context;
			telephonyManager = (TelephonyManager)getSystemService(
				Context.TELEPHONY_SERVICE);
		}

		public void register()
		{
			Log.d(getClass().getName(), "Registering");
			telephonyManager.listen(
				this, PhoneStateListener.LISTEN_SERVICE_STATE);
		}

		public void unregister()
		{
			Log.d(getClass().getName(), "Unregistering");
			telephonyManager.listen(
				this, PhoneStateListener.LISTEN_NONE);
		}

		@Override
		public void onServiceStateChanged(ServiceState serviceState)
		{
			if(Preferences.getNotifyOnNetworkCountryChange(context)
					|| Preferences.getNotifyOnNetworkOperatorChange(context))
			{
				checkServiceState();
			}
		}

		private void checkServiceState()
		{
			Log.d(getClass().getName(), "checkServiceState");

			String lastNetworkCountryIso =
				Preferences.getlastNetworkCountryIso(context);
			String lastNetworkOperatorName =
				Preferences.getlastNetworkOperatorName(context);

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

			Log.d(getClass().getName(), "lastNetworkCountryIso="
					+ lastNetworkCountryIso);
			Log.d(getClass().getName(), "newNetworkCountryIso="
					+ newNetworkCountryIso);
			Log.d(getClass().getName(), "lastNetworkOperatorName="
					+ lastNetworkOperatorName);
			Log.d(getClass().getName(), "newNetworkOperatorName="
					+ newNetworkOperatorName);

			if(newNetworkCountryIso.length() > 0
				&& !newNetworkCountryIso.equals(lastNetworkCountryIso))
			{
				networkCountryChanged = true;
				Preferences.setlastNetworkCountryIso(
					context, newNetworkCountryIso);
			}

			if(newNetworkOperatorName.length() > 0
				&& !newNetworkOperatorName.equals(lastNetworkOperatorName))
			{
				networkOperatorChanged = true;
				Preferences.setlastNetworkOperatorName(
					context, newNetworkOperatorName);
			}

			if(networkCountryChanged
				&& networkOperatorChanged
				&& Preferences.getNotifyOnNetworkCountryChange(context)
				&& Preferences.getNotifyOnNetworkOperatorChange(context))
			{
				sendNotification(1, getString(
					R.string.text_newNetworkCountryAndOperator,
					newNetworkCountryIso, newNetworkOperatorName));
			}
			else if(networkCountryChanged
				&& Preferences.getNotifyOnNetworkCountryChange(context))
			{
				sendNotification(2, getString(
					R.string.text_newNetworkCountry, newNetworkCountryIso));
			}
			else if(networkOperatorChanged
				&& Preferences.getNotifyOnNetworkOperatorChange(context))
			{
				sendNotification(3, getString(
					R.string.text_newNetworkOperator, newNetworkOperatorName));
			}
		}

//		int lastDebugNotificationAutoId = 1000;
//		private void sendDebugNotification(String title)
//		{
//			sendNotification(lastDebugNotificationAutoId++,
//				String.format("%1$tT %2$s", new Date(), title));
//		}

		private void sendNotification(int id, String title)
		{
			int icon =
				android.os.Build.VERSION.SDK_INT >= 11?
					R.drawable.icon_notification_11:
					R.drawable.icon_notification_9;
			NotificationCompat.Builder builder =
				new NotificationCompat.Builder(context)
					.setSmallIcon(icon)
					.setContentTitle(title)
					.setContentText(getString(R.string.text_tapToAdjustSettings))
					.setAutoCancel(true);
			Intent resultIntent = new Intent(
				context, PreferencesActivity.class);
			builder.setContentIntent(PendingIntent.getActivity(
				context, 0, resultIntent, PendingIntent.FLAG_UPDATE_CURRENT));
			NotificationManager notificationManager = (NotificationManager)
				context.getSystemService(Context.NOTIFICATION_SERVICE);
			notificationManager.notify(id, builder.build());
		}
	}
}
