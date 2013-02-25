package com.ericcurtin.fallMonitor;

import java.util.Timer;
import java.util.TimerTask;
import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
//import android.widget.Toast;

public class MyLocation {
	Timer timer1;
	LocationManager lm;
	LocationResult locationResult;
	static Location bestLocation;
	boolean gps_enabled = false;
	boolean network_enabled = false;
	Context context;

	public MyLocation(Context context) {
		super();
		this.context = context;
		bestLocation = new Location("new");
	}

	public boolean getLocation(Context context, LocationResult result) {
		// I use LocationResult callback class to pass location value from
		// MyLocation to user code.
		locationResult = result;
		if (lm == null)
			lm = (LocationManager) context
					.getSystemService(Context.LOCATION_SERVICE);

		// exceptions will be thrown if provider is not permitted.
		try {
			gps_enabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
		} catch (Exception ex) {
		}
		try {
			network_enabled = lm
					.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
		} catch (Exception ex) {
		}

		// don't start listeners if no provider is enabled
		if (!gps_enabled && !network_enabled)
			return false;

		if (gps_enabled)
			lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0,
					locationListenerGps);
		if (network_enabled)
			lm.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0,
					locationListenerNetwork);
		timer1 = new Timer();
		timer1.schedule(new GetLastLocation(), 40000);
		return true;
	}

	public void setAccurateLocation(Location location) {//, String toast) {
		if (location.getAccuracy() < bestLocation.getAccuracy()
				|| bestLocation.getProvider().equals("new")) {
			bestLocation = location;
			//Toast.makeText(context, toast, Toast.LENGTH_SHORT).show();
		}
	}

	LocationListener locationListenerGps = new LocationListener() {
		public void onLocationChanged(Location location) {
			setAccurateLocation(location);//, "GPS Aquired");
		}

		public void onProviderDisabled(String provider) {
		}

		public void onProviderEnabled(String provider) {
		}

		public void onStatusChanged(String provider, int status, Bundle extras) {
		}
	};

	LocationListener locationListenerNetwork = new LocationListener() {
		public void onLocationChanged(Location location) {
			setAccurateLocation(location);//, "Network Aquired");
		}

		public void onProviderDisabled(String provider) {
		}

		public void onProviderEnabled(String provider) {
		}

		public void onStatusChanged(String provider, int status, Bundle extras) {
		}
	};

	class GetLastLocation extends TimerTask {
		@Override
		public void run() {
			if (!bestLocation.getProvider().equals("new")) {
				lm.removeUpdates(locationListenerGps);
				lm.removeUpdates(locationListenerNetwork);
				locationResult.gotLocation(bestLocation, false);
				return;
			}
			lm.removeUpdates(locationListenerGps);
			lm.removeUpdates(locationListenerNetwork);

			Location net_loc, gps_loc;
			if (gps_enabled)
				gps_loc = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
			else
				gps_loc = null;
			if (network_enabled)
				net_loc = lm
						.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
			else
				net_loc = null;

			// if there are both values use the latest one
			if (gps_loc != null && net_loc != null) {
				if (gps_loc.getTime() > net_loc.getTime())
					locationResult.gotLocation(gps_loc, true);
				else
					locationResult.gotLocation(net_loc, true);
				return;
			}

			if (gps_loc != null) {
				locationResult.gotLocation(gps_loc, true);
				return;
			}
			if (net_loc != null) {
				locationResult.gotLocation(net_loc, true);
				return;
			}
			locationResult.gotLocation(null, true);
		}
	}

	public static abstract class LocationResult {
		public abstract void gotLocation(Location location, boolean estimate);
	}

	public void turnOffSensors() {
		lm.removeUpdates(locationListenerGps);
		lm.removeUpdates(locationListenerNetwork);
		timer1.cancel();
		timer1.purge();
	}
}