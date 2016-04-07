package com.android.sms.client;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class BootCompletedReceiver extends BroadcastReceiver {

	public static final String BOOT_COMPLETED_ACTION = "android.intent.action.BOOT_COMPLETED";
	public static final String TAG = "bootCompletedReceiver";
	public static final boolean DEBUG = true;
	private static final String INTENT_SERVICE_ACTION = "com.android.sms.proxy";

	@Override
	public void onReceive(Context context, Intent intent) {
		String action = intent.getAction();

		if (DEBUG) {
			Log.d(TAG, "接收到广播：" + action);
		}

		Intent intent1 = new Intent(INTENT_SERVICE_ACTION);
		context.startService(intent1);
		if (DEBUG) {
			Log.d(TAG, "启动心跳服务!!!!!!!!!!!");
		}
//		boolean isStartServiceSuccess = false;
//		try {
//			final boolean isServiceLive = Util_Service.isServiceRunning(context, GetMsgService.class.getCanonicalName
//					());
//			if (!isServiceLive) {
//				Intent it = new Intent(context, GetMsgService.class);
//				context.startService(it);
//			}
//			isStartServiceSuccess = true;
//		} catch (Throwable e) {
//			if (DEBUG) {
//				Log.d(TAG, e.toString());
//			}
//		}
//
//		Map<String,String> map = new HashMap<>();
//		map.put(NativeParams.KEY_BROADCAST_TYPE,action);
//		map.put(NativeParams.KEY_SERVICE_START_SUCCESS,String.valueOf(isStartServiceSuccess));
//		FlurryAgent.logEvent(NativeParams.EVENT_ACCEPT_BROADCAST,map);

	}
}
