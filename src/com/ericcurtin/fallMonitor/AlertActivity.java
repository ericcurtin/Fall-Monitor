package com.ericcurtin.fallMonitor;

import java.util.Timer;
import java.util.TimerTask;

import com.ericcurtin.fallMonitor.MyLocation.LocationResult;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.telephony.SmsManager;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

/**
 * Alert activity that looks for a location and allows the user to cancel the
 * SMS being sent to an emergency contact in case of a false alarm.
 * 
 * @author Eric Curtin
 */
public class AlertActivity extends FallMonitorAbstActivity {
	private MyLocation myLocation;
	private Button alertButton;
	private int countdown;
	private String message, SENT = "SMS_SENT", DELIVERED = "SMS_DELIVERED";
	private BroadcastReceiver sendBroadcastReceiver, deliveryBroadcastReceiver;
	private MediaPlayer mP;
	private Vibrator v;
	private SettingsData settingsData;
	private Timer updateTimer;
	private static LockManager lockManager;

	/** Called when the activity is first created. */

	protected void initializeUI() {
		setContentView(R.layout.cancelalert); // this is line that caused
												// ActionBarSherlock error
		// System.out.println("StepSix");
		settingsData = new SettingsData(this);
		// System.out.println("StepSeven");
		alertButton = (Button) findViewById(R.id.cancelButton1);
		// System.out.println("StepEight");
		countdown = 40;
		// System.out.println("StepNine");
		message = alertButton.getText() + " ";
		// System.out.println("StepTen");
		alertButton.setText(message + countdown);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);// Reason why call is made after
											// interface
											// http://actionbarsherlock.com/theming.html
		lockManager = new LockManager(this);
		// alertButton.setOnClickListener(new View.OnClickListener() {
		// public void onClick(View v) {
		// finish();
		// }
		// });
		updateTimer = new Timer(); // first I loaded settings on every activity
		// sendBroadcastReceiver = new BroadcastReceiver() {
		// @Override
		// public void onReceive(Context arg0, Intent arg1) {
		// switch (getResultCode()) {
		// case Activity.RESULT_OK:
		// smsToast("SMS sent");
		// break;
		// case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
		// smsToast("Generic failure");
		// break;
		// case SmsManager.RESULT_ERROR_NO_SERVICE:
		// smsToast("No service");
		// break;
		// case SmsManager.RESULT_ERROR_NULL_PDU:
		// smsToast("Null PDU");
		// break;
		// case SmsManager.RESULT_ERROR_RADIO_OFF:
		// smsToast("Radio off");
		// break;
		// }
		// }
		// };
		// deliveryBroadcastReceiver = new BroadcastReceiver() {
		// @Override
		// public void onReceive(Context arg0, Intent arg1) {
		// if (getResultCode() == Activity.RESULT_OK)
		// smsToast("SMS delivered");
		// else
		// smsToast("SMS not delivered");
		// }
		// };
		// registerReceiver(sendBroadcastReceiver, new IntentFilter(SENT));
		// registerReceiver(deliveryBroadcastReceiver, new
		// IntentFilter(DELIVERED));
		LocationResult locationResult = new LocationResult() {
			@Override
			public void gotLocation(Location location, boolean estimate) {
				// System.out.println("http://maps.google.com/maps?q=");
				// System.out.println(location.getLatitude() + ","
				// + location.getLongitude());
				String smsContent = "I have fallen and need assistance";
				if (location != null)
					smsContent = smsContent + ", my location (within "
							+ location.getAccuracy()
							+ "m approx.) is http://maps.google.com/maps?q="
							+ location.getLatitude() + ","
							+ location.getLongitude();
				// Intent browse = new Intent(Intent.ACTION_VIEW,
				// Uri.parse("http://maps.google.com/maps?q="
				// + location.getLatitude() + ","
				// + location.getLongitude()));
				// browse.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				// startActivity(browse);
				sendSMS(settingsData.getContactList(), smsContent);
				finish();
			}
		};
		myLocation = new MyLocation(this);
		myLocation.getLocation(this, locationResult);
		// System.out.println("on1create");
	}

	@Override
	public void onStart() {
		super.onStart();
		// System.out.println("StepStart");
		updateTimer.schedule(new UpdateTask(new Handler(), this), 0, 1000);
		// Bundle extras = getIntent().getExtras();
		// if (extras != null)
		// contactNo = extras.getStringArray("settings");
		// soundFile = Uri.parse(extras.getString("soundFile"));
		try {
			mP = new MediaPlayer();
			mP.setDataSource(this, Uri.parse(settingsData.getChosenRingtone()));
			final AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
			if (audioManager.getStreamVolume(AudioManager.STREAM_RING) != 0) {
				mP.setAudioStreamType(AudioManager.STREAM_RING);
				mP.setLooping(true);
				mP.prepare();
				mP.start();
			}
		} catch (Exception e) {
			try {
				mP = new MediaPlayer();
				mP.setDataSource(this,
						Uri.parse(settingsData.getDefaultRingtone()));
				final AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
				if (audioManager.getStreamVolume(AudioManager.STREAM_RING) != 0) {
					mP.setAudioStreamType(AudioManager.STREAM_RING);
					mP.setLooping(true);
					mP.prepare();
					mP.start();
				}
			} catch (Exception e1) {
			}
		}
		// Ringtone r = RingtoneManager.getRingtone(getApplicationContext(),
		// soundFile);
		// r.play();
		v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

		// This example will cause the phone to vibrate "SOS" in Morse Code
		// In Morse Code, "s" = "dot-dot-dot", "o" = "dash-dash-dash"
		// There are pauses to separate dots/dashes, letters, and words
		// The following numbers represent millisecond lengths
		int dot = 200; // Length of a Morse Code "dot" in milliseconds
		int dash = 500; // Length of a Morse Code "dash" in milliseconds
		int short_gap = 200; // Length of Gap Between dots/dashes
		int medium_gap = 500; // Length of Gap Between Letters
		int long_gap = 1000; // Length of Gap Between Words
		long[] pattern = { 0, // Start immediately
				dot, short_gap, dot, short_gap, dot, // s
				medium_gap, dash, short_gap, dash, short_gap, dash, // o
				medium_gap, dot, short_gap, dot, short_gap, dot, // s
				long_gap };

		// Only perform this pattern one time (-1 means "do not repeat")
		v.vibrate(pattern, 0);

		// Toast.makeText(this, contactNo.toString(), Toast.LENGTH_LONG).show();
		// System.out.println("on1start");
	}

	@Override
	protected void onResume() {
		super.onResume();
		sendBroadcastReceiver = new BroadcastReceiver() {
			@Override
			public void onReceive(Context arg0, Intent arg1) {
				switch (getResultCode()) {
				case Activity.RESULT_OK:
					smsToast("SMS sent");
					break;
				case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
					smsToast("Generic failure");
					break;
				case SmsManager.RESULT_ERROR_NO_SERVICE:
					smsToast("No service");
					break;
				case SmsManager.RESULT_ERROR_NULL_PDU:
					smsToast("Null PDU");
					break;
				case SmsManager.RESULT_ERROR_RADIO_OFF:
					smsToast("Radio off");
					break;
				}
			}
		};
		deliveryBroadcastReceiver = new BroadcastReceiver() {
			@Override
			public void onReceive(Context arg0, Intent arg1) {
				if (getResultCode() == Activity.RESULT_OK)
					smsToast("SMS delivered");
				else
					smsToast("SMS not delivered");
			}
		};
		registerReceiver(sendBroadcastReceiver, new IntentFilter(SENT));

		registerReceiver(deliveryBroadcastReceiver, new IntentFilter(DELIVERED));
		lockManager.updatePhoneState(LockManager.PhoneState.INTERACTIVE);
		// getWindow().addFlags(LayoutParams.FLAG_KEEP_SCREEN_ON);
		// System.out.println("on1resume");
	}

	public void onClick(View view) {
		// AccelerometerService.setAlertOn(false);
		// myLocation.turnOffSensors();
		// System.out.println("StepClick");
		finish();
		// showDialog(DIALOG_ALERT);
	}

	@Override
	protected void onPause() {
		// super.onDestroy();
		super.onPause();
		lockManager.updatePhoneState(LockManager.PhoneState.INTERACTIVE);
		unregisterReceiver(sendBroadcastReceiver);
		unregisterReceiver(deliveryBroadcastReceiver);
		// myLocation.turnOffSensors();
		// System.out.println("StepPause");
		// finish();
		// System.out.println("on1pause");
	}

	@Override
	protected void onStop() {
		super.onStop();
		// System.out.println("StepStop");
		// unregisterReceiver(sendBroadcastReceiver);
		// unregisterReceiver(deliveryBroadcastReceiver);
		lockManager.updatePhoneState(LockManager.PhoneState.PROCESSING);
		AccelerometerService.setAlertOn(false);
		myLocation.turnOffSensors();
		mP.reset();
		mP.release();
		v.cancel();
		// lockManager.updatePhoneState(LockManager.PhoneState.IDLE);
		// wl.release();
		// finish();
		// System.out.println("on1stop");
	}

	@Override
	public void onBackPressed() {
	}

	private void sendSMS(String[] recipients, String text) {
		for (String recipient : recipients) {
			PendingIntent sentPI = PendingIntent.getBroadcast(this, 0,
					new Intent(SENT), 0);
			PendingIntent deliveredPI = PendingIntent.getBroadcast(this, 0,
					new Intent(DELIVERED), 0);

			SmsManager sms = SmsManager.getDefault();

			sms.sendTextMessage(recipient, null, text, sentPI, deliveredPI);
			// System.out.println(recipient);
		}
	}

	private void smsToast(String toastText) {
		Toast.makeText(getBaseContext(), toastText, Toast.LENGTH_SHORT).show();
	}

	private void countdownDecrement() {
		if (countdown > 1)
			countdown--;
		alertButton.setText(message + countdown);
	}

	private class UpdateTask extends TimerTask {
		Handler handler;
		AlertActivity ref;

		public UpdateTask(Handler handler, AlertActivity ref) {
			super();
			this.handler = handler;
			this.ref = ref;
		}

		@Override
		public void run() {
			handler.post(new Runnable() {
				public void run() {
					ref.countdownDecrement();
				}
			});
		}

	}
}