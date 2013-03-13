package com.ericcurtin.fallMonitor;

import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.util.Arrays;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.widget.Toast;
import android.hardware.SensorManager;

public class AccelerometerService extends Service implements
		SensorEventListener {

	private SensorManager sensorManager;
	private Sensor accelerometer;
	private static long[] timer;
	private CalibrationData calibrationData;
	private static boolean alertOn;

	// private String[] contactNo;
	// private String contactSt, soundFile;

	@Override
	public IBinder onBind(Intent arg0) {
		return null;
	}

	@Override
	public void onCreate() {
		Notification notification = new Notification(R.drawable.fallmonitor,
				getText(R.string.ticker_text), System.currentTimeMillis());
		Intent notificationIntent = new Intent(this, FallMonitorActivity.class);
		PendingIntent pendingIntent = PendingIntent.getActivity(this, 0,
				notificationIntent, 0);
		notification.setLatestEventInfo(this,
				getText(R.string.notification_title),
				getText(R.string.notification_message), pendingIntent);
		startForeground(8256, notification);

		alertOn = false;
		timer = new long[3];
		Arrays.fill(timer, 0);

		super.onCreate();

		try {
			FileInputStream fileInputStream = openFileInput(getString(R.string.calibFileName));
			ObjectInputStream objectInputStream = new ObjectInputStream(
					fileInputStream);
			calibrationData = (CalibrationData) objectInputStream.readObject();
			objectInputStream.close();
			fileInputStream.close();
			// System.out.println("jonny " + calibrationData);
		} catch (Exception e) {
			calibrationData = new CalibrationData();
		}

		sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
		accelerometer = sensorManager
				.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
		sensorManager.registerListener(this, accelerometer,
				SensorManager.SENSOR_DELAY_FASTEST);
	}

	// @Override
	// @Deprecated
	// public void onStart(Intent intent, int startId) {
	// super.onStart(intent, startId);

	// contactNo = //intent.getExtras().getStringArray("settings");
	// soundFile = //intent.getExtras().getString("soundFile");
	// contactSt = "";
	// for (String arg : contactNo) {
	// contactSt += arg + " ";
	// }
	// Toast.makeText(this, contactSt, Toast.LENGTH_LONG).show();
	// }

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		final SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(this);
		if (!prefs.getBoolean("isAccelOn", false))
			stopSelf();
		return START_STICKY;
	}

	@Override
	public void onDestroy() {
		sensorManager.unregisterListener(this);
		super.onDestroy();
		Toast.makeText(this, "Fall Monitor service destroyed",
				Toast.LENGTH_LONG).show();
	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
	}

	@Override
	public void onSensorChanged(SensorEvent event) {
		if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
			getAccelerometer(event);
		}
	}

	private void getAccelerometer(SensorEvent event) {

		double g = java.lang.Math.sqrt(event.values[0] * event.values[0]
				+ event.values[1] * event.values[1] + event.values[2]
				* event.values[2]);// / 9.80665;

		// System.out.println("Accer: " + event.values[0] + " " +
		// event.values[1]
		// + " " + event.values[2] + " " + g);

		if (!alertOn)
			if (g > calibrationData.getMaxG() && timer[0] == 0) {
				timer[0] = System.currentTimeMillis();
				// System.out.println("StepOne");
			} else if (System.currentTimeMillis() - timer[0] < calibrationData
					.getTimeDifference() && g < calibrationData.getMinG()) {
				timer[1] = System.currentTimeMillis();
				// System.out.println("StepTwo");
			} else if (System.currentTimeMillis() - timer[0] > calibrationData
					.getTimeDifference() && timer[0] > 0 && timer[1] == 0) {
				Arrays.fill(timer, 0);
				// System.out.println("ResetTwo");
			} else if (System.currentTimeMillis() - timer[1] < 10000
					&& g > calibrationData.getMinG()
					&& g < calibrationData.getMaxG() && timer[2] == 0) {
				timer[2] = System.currentTimeMillis();
				// System.out.println("StepThree");
			} else if (System.currentTimeMillis() - timer[2] < 10000 && System.currentTimeMillis() - timer[2] > 1000
					&& (g < SensorManager.GRAVITY_EARTH - 1.5/*
															 * calibrationData.
															 * getMinG()
															 */&& g > SensorManager.GRAVITY_EARTH + 1.5/*
																										 * calibrationData
																										 * .
																										 * getMaxG
																										 * (
																										 * )
																										 */)) {
				Arrays.fill(timer, 0);
				// System.out.println("ResetThree");
			} else if (System.currentTimeMillis() - timer[2] > 10000
					&& timer[2] > 0) {
				// System.out.println("StepThree");
				alertOn = true;
				Intent popup = new Intent(this.getApplicationContext(),
						AlertActivity.class);
				popup.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				// popup.putExtra("settings", contactNo);
				// popup.putExtra("soundFile", soundFile);
				startActivity(popup);
			}

	}

	public static void setAlertOn(boolean alertOn) {
		AccelerometerService.alertOn = alertOn;
		Arrays.fill(timer, 0);
	}
}