package net.youmi.android.libs.common.util;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.net.Uri;

import net.youmi.android.libs.common.debug.Debug_SDK;

public class Util_System_Intent_Network {

	public static void wirelessSetting(Context context) {
		try {
			context.startActivity(new Intent(android.provider.Settings.ACTION_WIRELESS_SETTINGS)
					.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
		} catch (Throwable e) {
			if (Debug_SDK.isUtilLog) {
				Debug_SDK.te(Debug_SDK.mUtilTag, Util_System_Intent_Network.class, e);
			}
		}
	}

	/**
	 * 获取打开url的Intent
	 * 
	 * @param context
	 * @param browserPackageName
	 *            可为null
	 * @param browserActivity
	 *            可为null
	 * @param url
	 * @return
	 */
	public static Intent getToWebUrlIntent(Context context, String browserPackageName, String browserActivity,
			String url) {

		Intent intent = null;
		try {

			if (browserPackageName != null) {

				if (browserActivity != null) {
					try {
						PackageInfo pi = context.getPackageManager().getPackageInfo(browserPackageName, 0);

						if (pi != null) {
							intent = new Intent();
							intent.setAction(Intent.ACTION_VIEW);
							intent.setClassName(browserPackageName, browserActivity);
							intent.addCategory(Intent.CATEGORY_DEFAULT);
							intent.setData(Uri.parse(url));
						}

					} catch (Throwable e) {
						if (Debug_SDK.isUtilLog) {
							Debug_SDK.te(Debug_SDK.mUtilTag, Util_System_Intent_Network.class, e);
						}
					}

				} else {

					try {
						intent = context.getPackageManager().getLaunchIntentForPackage(browserPackageName);
						intent.setAction(Intent.ACTION_VIEW);
						intent.addCategory(Intent.CATEGORY_DEFAULT);
						intent.setData(Uri.parse(url));

					} catch (Throwable e) {
						if (Debug_SDK.isUtilLog) {
							Debug_SDK.te(Debug_SDK.mUtilTag, Util_System_Intent_Network.class, e);
						}
					}
				}

			}

			if (intent == null) {
				intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
			}

			intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

		} catch (Throwable e) {
			if (Debug_SDK.isUtilLog) {
				Debug_SDK.te(Debug_SDK.mUtilTag, Util_System_Intent_Network.class, e);
			}
		}
		return intent;
	}

}
