package com.android.sms.proxy.service;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import com.oplay.nohelper.utils.Util_Service;

/**
 * @author zyq 16-3-10
 */
public class AlarmControl {

	private volatile static AlarmControl instance;
	private Context mContext;

	public AlarmControl(Context context) {
		this.mContext = context;
	}

	public static AlarmControl getInstance(Context context) {
		if (instance == null) {
			synchronized (AlarmControl.class) {
				if (instance == null) {
					instance = new AlarmControl(context);
				}
			}
		}
		return instance;
	}

	public void initAlarm(int hour, int minute, int second, int millisecond) {
		final boolean isServiceLive = Util_Service.isServiceRunning(mContext, HeartBeatService.class.getCanonicalName
				());
		if (!isServiceLive) {
			Intent intent = new Intent(mContext, HeartBeatService.class);
			mContext.startService(intent);
		}
//		PendingIntent sender = PendingIntent.getService(mContext, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
//		AlarmManager am = (AlarmManager) mContext.getSystemService(Context.ALARM_SERVICE);
//
////		Calendar calendar = Calendar.getInstance();
////		calendar.set(Calendar.HOUR_OF_DAY, hour);
////		calendar.set(Calendar.MINUTE, minute);
////		calendar.set(Calendar.SECOND, second);
////		calendar.set(Calendar.MILLISECOND, millisecond);
//
//		am.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), AlarmManager.INTERVAL_DAY, sender);
	}

	public void cancelAlarm() {
		Intent it = new Intent(mContext, HeartBeatService.class);
		PendingIntent sender = PendingIntent.getService(mContext, 0, it, 0);
		AlarmManager am = (AlarmManager) mContext.getSystemService(Context.ALARM_SERVICE);
		am.cancel(sender);
	}
}
