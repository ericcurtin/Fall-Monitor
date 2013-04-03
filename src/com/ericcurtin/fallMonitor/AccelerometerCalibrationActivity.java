// Eric Curtin 1990 312

package com.ericcurtin.fallMonitor;

import java.util.Arrays;

import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class AccelerometerCalibrationActivity extends FallMonitorAbstActivity implements
		SensorEventListener {

	private SensorManager sensorManager;
	private Sensor accelerometer;
	private double[] minG, maxG;
	private long[] minTime, maxTime, timeDifference;
	private static int i;

	@Override
	protected void initializeUI() {
		setContentView(R.layout.calibration);
	}
	
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		i = 0;
		sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
		accelerometer = sensorManager
				.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
		sensorManager.registerListener(this, accelerometer,
				SensorManager.SENSOR_DELAY_GAME);
		minG = new double[5];
		maxG = new double[5];
		minTime = new long[5];
		maxTime = new long[5];
		Arrays.fill(minG, Float.MAX_VALUE);
		Arrays.fill(maxG, Float.MIN_VALUE);// setting up arrays for storage of
											// calibration values
		final Button button = (Button) findViewById(R.id.button1);
		final TextView textView = (TextView) findViewById(R.id.textView);
		textView.setTextColor(Color.GREEN);
		textView.setText("Simulate Fall " + (i + 1));
		button.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				if (i == 0) {
					textView.setTextColor(Color.CYAN);
					textView.setText("Simulate Fall " + (i + 2));
				} else if (i == 1) {
					textView.setTextColor(Color.BLUE);
					textView.setText("Simulate Fall " + (i + 2));
				} else if (i == 2) {
					textView.setTextColor(Color.MAGENTA);
					textView.setText("Simulate Fall " + (i + 2));
				} else if (i == 3) {
					textView.setTextColor(Color.RED);
					button.setText("Calibration Complete");
					textView.setText("Simulate Fall " + (i + 2));
				} else
					calibrationComplete();
				i++;
			}
		});
	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		finish();
	}

	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		super.onStop();
		finish();
	}

	protected void calibrationComplete() {
		// finish();
		calculateTimeDiff();
		Arrays.sort(this.minG);
		Arrays.sort(this.maxG);

		// CalibrationData calibrationData =
		new CalibrationData(this, minG[4], maxG[0], timeDifference[4]);
		finish();
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
				* event.values[2]);// s / 9.80665;
		if (i < 5)
			if (g > maxG[i]) {
				maxG[i] = g;
				maxTime[i] = System.currentTimeMillis();
				// System.out.println(g);
				minG[i] = Float.MAX_VALUE;// to ensure minG is recorded after
											// maxG
			} else if (g < minG[i]) {
				minG[i] = g;
				minTime[i] = System.currentTimeMillis();
				// System.out.println(g + " " + (minTime[i] - maxTime[i]));
			}
	}

	@Override
	public void onAccuracyChanged(Sensor arg0, int arg1) {
		// TODO Auto-generated method stub

	}

	private void calculateTimeDiff() {
		timeDifference = new long[5];
		for (int i = 0; i < 5; i++)
			timeDifference[i] = minTime[i] - maxTime[i];
		Arrays.sort(timeDifference);
	}
}