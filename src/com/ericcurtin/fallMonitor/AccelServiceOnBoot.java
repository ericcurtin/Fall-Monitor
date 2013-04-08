package com.ericcurtin.fallMonitor;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Starts fall monitor service on boot if it was running when the phone was
 * switched off.
 * 
 * @author Eric Curtin
 */
public class AccelServiceOnBoot extends BroadcastReceiver {

	@Override
	public void onReceive(final Context context, Intent intent) {
		// SettingsData settingsData = new SettingsData(context);

		if ("android.intent.action.BOOT_COMPLETED".equals(intent.getAction())
		/* && settingsData.isAccelServiceOn() == true */) {
			Intent serviceIntent = new Intent(context,
					AccelerometerService.class);
			// serviceIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			context.startService(serviceIntent);
		}
	}
}