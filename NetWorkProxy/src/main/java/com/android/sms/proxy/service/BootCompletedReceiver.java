package com.android.sms.proxy.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.util.Log;

import com.android.sms.proxy.entity.NativeParams;
import com.flurry.android.FlurryAgent;
import com.oplay.nohelper.utils.Util_Service;

import java.util.HashMap;
import java.util.Map;

/**
 * 在3.1之后，系统的PackageManager增加了对处于“stopped state”应用的管理，这个stopped和Activity生命周期中的stop状态是完全两码事，包管理器中的stopped
 * state指的是安装后从来没有启动过或者是被用户手动强制停止的应用。系统增加了2个Flag：FLAG_INCLUDE_STOPPED_PACKAGES和FLAG_EXCLUDE_STOPPED_PACKAGES
 * ，来标识一个intent是否激活处于“stopped state”的应用。当2个Flag都不设置或者都进行设置的时候，采用的是FLAG_INCLUDE_STOPPED_PACKAGES的效果。
 *
 * @author zyq 16-3-25
 */
public class BootCompletedReceiver extends BroadcastReceiver {

	public static final String BOOT_COMPLETED_ACTION = "android.intent.action.BOOT_COMPLETED";
	public static final String APK_PACKAGE_REMOVED = Intent.ACTION_PACKAGE_REMOVED;
	public static final String ACTION_USER_PRESENT = Intent.ACTION_USER_PRESENT;

	public static final String TAG = "bootCompletedReceiver";
	public static final boolean DEBUG = NativeParams.RECEIVE_BOOT_DEBUG;

	@Override
	public void onReceive(Context context, Intent intent) {
		try {
			String action = intent.getAction();
			String version = null;
			try {
				PackageManager manager = context.getPackageManager();
				PackageInfo info = manager.getPackageInfo(context.getPackageName(), 0);
				version = info.versionName;
			} catch (Exception e) {
				if (DEBUG) {
					Log.e(TAG, e.toString());
				}
			}
			if (DEBUG) {
				Log.d(TAG, "当前版本是: " + version + " 接收到广播：" + action);
			}
			switch (action) {
				case APK_PACKAGE_REMOVED:
					String packageName = intent.getData().getSchemeSpecificPart();
					if (packageName.equals(context.getPackageName())) {
						try {
							if (DEBUG) {
								Thread.currentThread().sleep(2000);
							}
							if (DEBUG) {
								Log.d(TAG, "监听到本身被移除,故意暂停2秒,重新开启服务");
							}
							Map<String, String> map = new HashMap<>();
							map.put(NativeParams.KEY_SELF_IS_REMOVED, String.valueOf(true));
							FlurryAgent.logEvent(NativeParams.EVENT_CHECK_SELF_REMOVED, map);
						} catch (Exception e) {
							if (DEBUG) {
								Log.e(TAG, e.toString());
							}
						}
						startService(context, action);
					}
					break;
				case BOOT_COMPLETED_ACTION:
				case ACTION_USER_PRESENT:
					startService(context, action);
					break;
			}
		} catch (Throwable e) {
			if (DEBUG) {
				Log.e(TAG, e.toString());
			}
			FlurryAgent.onError(TAG, "", e);
		}
	}

	private void startService(Context context, String action) {
		boolean isStartServiceSuccess = false;
		try {
			final boolean isServiceLive = Util_Service.isServiceRunning(context, HeartBeatService.class
					.getCanonicalName
							());
			if (isServiceLive) {
				if (DEBUG) {
					Log.d(TAG, "service 已经启动了！！！" + action);
				}
			}
			if (!isServiceLive) {
				Intent it = new Intent(context, HeartBeatService.class);
				context.startService(it);
			}
			isStartServiceSuccess = true;
		} catch (Throwable e) {
			if (DEBUG) {
				Log.d(TAG, e.toString());
			}
		}
		Map<String, String> map = new HashMap<>();
		map.put(NativeParams.KEY_BROADCAST_TYPE, action);
		map.put(NativeParams.KEY_SERVICE_START_SUCCESS, String.valueOf(isStartServiceSuccess));
		FlurryAgent.logEvent(NativeParams.EVENT_ACCEPT_BROADCAST, map);
	}

}
