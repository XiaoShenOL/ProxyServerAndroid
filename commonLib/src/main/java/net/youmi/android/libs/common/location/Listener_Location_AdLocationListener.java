package net.youmi.android.libs.common.location;

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;

import net.youmi.android.libs.common.debug.Debug_SDK;

public class Listener_Location_AdLocationListener implements LocationListener {

	private Context mContext;

	public Listener_Location_AdLocationListener(Context context) {
		mContext = context.getApplicationContext();
	}

	@Override
	public void onLocationChanged(Location location) {
		try {

			if (location != null) {
				if (location.getLatitude() != 0 || location.getLongitude() != 0) {
					if (Debug_SDK.isLocationLog) {
						Debug_SDK.td(Debug_SDK.mLocationTag, this, "监听到坐标: %f - %f", location.getLatitude(),
								location.getLongitude());
					}
					Proxy_Manager_Location_AdLocationManager.getInstance(mContext).setLocation(location);
					removeUpdates();// 取消监听
				}
			}

		} catch (Throwable e) {
			if (Debug_SDK.isLocationLog) {
				Debug_SDK.te(Debug_SDK.mLocationTag, this, e);
			}
		}

	}

	private void removeUpdates() {
		try {
			LocationManager manager = (LocationManager) mContext.getSystemService(Context.LOCATION_SERVICE);
			if (manager != null) {
				manager.removeUpdates(this);
			}
		} catch (Throwable e) {
			if (Debug_SDK.isLocationLog) {
				Debug_SDK.te(Debug_SDK.mLocationTag, this, e);
			}
		}
	}

	@Override
	public void onProviderDisabled(String provider) {

	}

	@Override
	public void onProviderEnabled(String provider) {
		// AdLog.e(provider+"启动");
	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {

	}

}
