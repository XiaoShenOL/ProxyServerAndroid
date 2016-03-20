package com.oplay.nohelper.utils;

import android.app.ActivityManager;
import android.content.Context;

import java.util.List;

/**
 * @author zyq 16-3-20
 */
public class Util_Service {

	public static boolean isServiceRunning(Context context,String serviceClassName){
		final ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
		final List<ActivityManager.RunningServiceInfo> services = activityManager.getRunningServices(Integer.MAX_VALUE);

		for (ActivityManager.RunningServiceInfo runningServiceInfo : services) {
			if (runningServiceInfo.service.getClassName().equals(serviceClassName)){
				return true;
			}
		}
		return false;
	}
}
