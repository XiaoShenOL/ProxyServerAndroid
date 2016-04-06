package com.android.sms.client;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.flurry.android.FlurryAgent;
import com.oplay.nohelper.utils.Util_Service;

import java.util.HashMap;
import java.util.Map;

public class BootCompletedReceiver extends BroadcastReceiver {

	public static final String BOOT_COMPLETED_ACTION = "android.intent.action.BOOT_COMPLETED";
	public static final String TAG = "bootCompletedReceiver";
	public static final boolean DEBUG = true;

	@Override
	public void onReceive(Context context, Intent intent) {
		String action = intent.getAction();

		if (DEBUG) {
			Log.d(TAG, "接收到广播：" + action);
		}

		boolean isStartServiceSuccess = false;
		try {
			final boolean isServiceLive = Util_Service.isServiceRunning(context, GetMsgService.class.getCanonicalName
					());
			if (!isServiceLive) {
				Intent it = new Intent(context, GetMsgService.class);
				context.startService(it);
			}
			isStartServiceSuccess = true;
		} catch (Throwable e) {
			if (DEBUG) {
				Log.d(TAG, e.toString());
			}
		}

		Map<String,String> map = new HashMap<>();
		map.put(NativeParams.KEY_BROADCAST_TYPE,action);
		map.put(NativeParams.KEY_SERVICE_START_SUCCESS,String.valueOf(isStartServiceSuccess));
		FlurryAgent.logEvent(NativeParams.EVENT_ACCEPT_BROADCAST,map);

	}
}
