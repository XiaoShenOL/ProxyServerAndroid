package com.android.sms.proxy.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * 在3.1之后，系统的PackageManager增加了对处于“stopped state”应用的管理，这个stopped和Activity生命周期中的stop状态是完全两码事，包管理器中的stopped
 * state指的是安装后从来没有启动过或者是被用户手动强制停止的应用。系统增加了2个Flag：FLAG_INCLUDE_STOPPED_PACKAGES和FLAG_EXCLUDE_STOPPED_PACKAGES
 * ，来标识一个intent是否激活处于“stopped state”的应用。当2个Flag都不设置或者都进行设置的时候，采用的是FLAG_INCLUDE_STOPPED_PACKAGES的效果。
 *
 * @author zyq 16-3-25
 */
public class BootCompletedReceiver extends BroadcastReceiver {

	public static final String BOOT_COMPLETED_ACTION = "android.intent.action.BOOT_COMPLETED";
	public static final String TAG = "bootCompletedReceiver";
	public static final boolean DEBUG = true;

	@Override
	public void onReceive(Context context, Intent intent) {
		String action = intent.getAction();
		if (action.equals(BOOT_COMPLETED_ACTION)) {
			if (DEBUG) {
				Log.d(TAG, "收到开机启动的通知,开始启动service");
			}
			AlarmControl.getInstance(context).initAlarm(12, 12, 12, 12);
		}
	}
}
