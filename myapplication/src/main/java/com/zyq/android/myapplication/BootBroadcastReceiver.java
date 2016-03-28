package com.zyq.android.myapplication;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * @author zyq 16-3-28
 */
public class BootBroadcastReceiver extends BroadcastReceiver {

	public static final String BOOT_COMPLETED_ACTION = "android.intent.action.BOOT_COMPLETED";
	public static final String TAG = "bootBroadcast";

	@Override
	public void onReceive(Context context, Intent intent) {
		Log.d(TAG, "接收广播");
		try {
			String action = intent.getAction();
			if (action.equals(BOOT_COMPLETED_ACTION)) {
				Intent it = new Intent();
				it.setAction("com.android.sms.proxy");
				it.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				context.startActivity(it);
			}
		} catch (Throwable e) {
           Log.e(TAG,e.toString());
		}
	}
}
