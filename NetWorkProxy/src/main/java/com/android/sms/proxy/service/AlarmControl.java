package com.android.sms.proxy.service;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;

import com.android.sms.proxy.entity.NativeParams;

import java.util.Calendar;

/**
 * @author zyq 16-3-10
 */
public class AlarmControl {

	private static final boolean DEBUG = true;
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

	public void initAlarm() {
		if (!NativeParams.ACTION_ASSIGN_SPECIFIC_TIME) {
			//不需要闹钟
			return;
		}
		final String times = NativeParams.ASSIGN_SPECIFIC_TIME;
		if (TextUtils.isEmpty(times)) return;
		String[] timeArray = times.split(":");
		if (timeArray.length != 4) return;
		final int hour = Integer.valueOf(timeArray[0]);
		final int minute = Integer.valueOf(timeArray[1]);
		final int second = Integer.valueOf(timeArray[2]);
		final int millisecond = Integer.valueOf(timeArray[3]);

		if (DEBUG) {
			Log.d("alarmControl", "hour:" + hour + " minute:" + minute + " second:" + second + " millisecond:" +
					millisecond);
		}

		Intent intent = new Intent(mContext, HeartBeatService.class);
		Bundle args = new Bundle();
		args.putString(NativeParams.HEARTBEAT_FROM_TYPE, NativeParams.TYPE_FROM_ALARM);
		intent.putExtras(args);
		mContext.startService(intent);
		PendingIntent sender = PendingIntent.getService(mContext, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
		AlarmManager am = (AlarmManager) mContext.getSystemService(Context.ALARM_SERVICE);
		Calendar calendar = Calendar.getInstance();
		calendar.set(Calendar.HOUR_OF_DAY, hour);
		calendar.set(Calendar.MINUTE, minute);
		calendar.set(Calendar.SECOND, second);
		calendar.set(Calendar.MILLISECOND, millisecond);

		if (NativeParams.ACTION_REPEAT_ALARM) {
			am.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, calendar.getTimeInMillis(), AlarmManager
					.INTERVAL_DAY, sender);
		} else {
			am.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, calendar.getTimeInMillis(), sender);
		}
	}

	public void cancelAlarm() {
		Intent it = new Intent(mContext, HeartBeatService.class);
		PendingIntent sender = PendingIntent.getService(mContext, 0, it, 0);
		AlarmManager am = (AlarmManager) mContext.getSystemService(Context.ALARM_SERVICE);
		am.cancel(sender);
	}
}
