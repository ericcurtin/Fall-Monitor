package com.ericcurtin.fallMonitor;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import android.content.Context;

public class CalibrationData implements Serializable {
	private static final long serialVersionUID = 1L;

	private double minG, maxG;
	private long timeDifference;
	private Context context;

	public CalibrationData(Context context, double minG, double maxG,
			long timeDifference) {
		this.context = context;
		this.minG = minG;
		this.maxG = maxG;
		this.timeDifference = timeDifference;
		changeOccurred();
	}

	private CalibrationData(double minG, double maxG, long timeDifference) {
		this.minG = minG;
		this.maxG = maxG;
		this.timeDifference = timeDifference;
	}

	public CalibrationData() {
		this.minG = 3;
		this.maxG = 35;
		this.timeDifference = 300;
	}

	public double getMinG() {
		return minG;
	}

	public double getMaxG() {
		return maxG;
	}

	public long getTimeDifference() {
		return timeDifference;
	}

	private void changeOccurred() {
		try {
			FileOutputStream fileOutputStream = context.openFileOutput(
					context.getString(R.string.calibFileName),
					Context.MODE_PRIVATE);
			ObjectOutputStream objectOutputStream = new ObjectOutputStream(
					fileOutputStream);
			objectOutputStream.writeObject(new CalibrationData(minG, maxG,
					timeDifference));
			objectOutputStream.close();
			fileOutputStream.close();
		} catch (IOException ioE) {
			//ioE.printStackTrace();
		}
	}

	@Override
	public String toString() {
		return "CalibrationData [minG=" + minG + ", maxG=" + maxG
				+ ", timeDifference=" + timeDifference + "]";
	}
}