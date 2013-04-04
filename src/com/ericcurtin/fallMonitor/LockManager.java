package com.ericcurtin.fallMonitor;

import android.app.KeyguardManager;
import android.content.Context;
import android.net.wifi.WifiManager;
import android.os.PowerManager;
import android.provider.Settings;

//import android.util.Log;

/**
 * Maintains wake lock state.
 * 
 * @author Stuart O. Anderson
 */
public class LockManager {
	private final PowerManager.WakeLock fullLock;
	private final PowerManager.WakeLock partialLock;
	private final KeyguardManager.KeyguardLock keyGuardLock;
	private final KeyguardManager km;
	private final WifiManager.WifiLock wifiLock;

	private boolean keyguardDisabled;

	public enum PhoneState {
		IDLE, PROCESSING, // used when the phone is active but before the user
							// should be alerted.
		INTERACTIVE,
	}

	private enum LockState {
		FULL, PARTIAL, SLEEP,
	}

	public LockManager(Context context) {
		PowerManager pm = (PowerManager) context
				.getSystemService(Context.POWER_SERVICE);
		fullLock = pm.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK
				| PowerManager.ACQUIRE_CAUSES_WAKEUP, "RedPhone Full");
		partialLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
				"Fall Monitor Partial");

		km = (KeyguardManager) context
				.getSystemService(Context.KEYGUARD_SERVICE);
		keyGuardLock = km.newKeyguardLock("Fall Monitor KeyGuard");

		WifiManager wm = (WifiManager) context
				.getSystemService(Context.WIFI_SERVICE);
		wifiLock = wm.createWifiLock(WifiManager.WIFI_MODE_FULL_HIGH_PERF,
				"RedPhone Wifi");

		fullLock.setReferenceCounted(false);
		partialLock.setReferenceCounted(false);
		wifiLock.setReferenceCounted(false);
	}

	private boolean isWifiPowerActiveModeEnabled(Context context) {
		int wifi_pwr_active_mode = Settings.Secure.getInt(
				context.getContentResolver(), "wifi_pwr_active_mode", -1);
		// Log.d("LockManager", "Wifi Activity Policy: " +
		// wifi_pwr_active_mode);

		if (wifi_pwr_active_mode == 0) {
			return false;
		}

		return true;
	}

	public void updatePhoneState(PhoneState state) {
		switch (state) {
		case IDLE:
			setLockState(LockState.SLEEP);
			maybeEnableKeyguard();
			break;
		case PROCESSING:
			setLockState(LockState.PARTIAL);
			maybeEnableKeyguard();
			break;
		case INTERACTIVE:
			setLockState(LockState.FULL);
			disableKeyguard();
			break;
		}
	}

	private synchronized void setLockState(LockState newState) {
		switch (newState) {
		case FULL:
			fullLock.acquire();
			partialLock.acquire();
			wifiLock.acquire();
			break;
		case PARTIAL:
			partialLock.acquire();
			wifiLock.release();
			fullLock.release();
			break;
		case SLEEP:
			fullLock.release();
			partialLock.release();
			wifiLock.release();
			break;
		default:
			throw new IllegalArgumentException("Unhandled Mode: " + newState);
		}
		// Log.d("LockManager", "Entered Lock State: " + newState);
	}

	private void disableKeyguard() {
		if (keyguardLocked()) {
			keyGuardLock.disableKeyguard();
			keyguardDisabled = true;
		}
	}

	private void maybeEnableKeyguard() {
		if (keyguardDisabled) {
			keyGuardLock.reenableKeyguard();
			keyguardDisabled = false;
		}
	}

	private boolean keyguardLocked() {
		return true;
	}
}