package net.youmi.android.libs.common.util;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Build.VERSION_CODES;

import net.youmi.android.libs.common.debug.Debug_SDK;

public class Util_System_Permission {

	/**
	 * 是否具有相应权限
	 * 
	 * @param context
	 * @param permissionName
	 * @return
	 */
	public static boolean isWithPermission(Context context, String permissionName) {
		try {
			if (context.checkCallingOrSelfPermission(permissionName) == PackageManager.PERMISSION_DENIED) {
				return false;
			}
		} catch (Throwable e) {
			if (Debug_SDK.isUtilLog) {
				Debug_SDK.te(Debug_SDK.mUtilTag, Util_System_Permission.class, e);
			}
		}
		return true;
	}

	/**
	 * 检查是否具有写入外部存储卡的权限
	 * 
	 * @param context
	 * @return
	 */
	public static boolean isWith_WRITE_EXTERNAL_STORAGE_Permission(Context context) {
		try {

			// Util_SDK_Compatibility.getSDKLevel();
			int sdkLevel = Build.VERSION.SDK_INT;

			if (sdkLevel < VERSION_CODES.DONUT) {
				return true;
			}

			return isWithPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE);
		} catch (Throwable e) {
			if (Debug_SDK.isUtilLog) {
				Debug_SDK.te(Debug_SDK.mUtilTag, Util_System_Permission.class, e);
			}
		}
		return false;
	}

	/**
	 * 检查是否具有联网INTERNET权限
	 * 
	 * @param context
	 * @return
	 */
	public static boolean isWith_INTERNET_Permission(Context context) {
		return isWithPermission(context, Manifest.permission.INTERNET);
	}

	/**
	 * 检查是否具有获取手机信息READ_PHONE_STATE权限
	 * 
	 * @param context
	 * @return
	 */
	public static boolean isWith_READ_PHONE_STATE_Permission(Context context) {
		return isWithPermission(context, Manifest.permission.READ_PHONE_STATE);
	}

	/**
	 * 检查是否具有ACCESS_NETWORK_STATE权限
	 * 
	 * @param context
	 * @return
	 */
	public static boolean isWith_ACCESS_NETWORK_STATE_Permission(Context context) {
		return isWithPermission(context, Manifest.permission.ACCESS_NETWORK_STATE);
	}

	/**
	 * 检查是否具有ACCESS_FINE_LOCATION权限
	 * 
	 * @param context
	 * @return
	 */
	public static boolean isWith_ACCESS_FINE_LOCATION_Permission(Context context) {
		return isWithPermission(context, Manifest.permission.ACCESS_FINE_LOCATION);
	}

	/**
	 * 检查是否具有ACCESS_COARSE_LOCATION权限
	 * 
	 * @param context
	 * @return
	 */
	public static boolean isWith_ACCESS_COARSE_LOCATION_Permission(Context context) {
		return isWithPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION);
	}

	/**
	 * 检查是否具有ACCESS_WIFI_STATE权限
	 * 
	 * @param context
	 * @return
	 */
	public static boolean isWith_ACCESS_WIFI_STATE_Permission(Context context) {
		return isWithPermission(context, Manifest.permission.ACCESS_WIFI_STATE);
	}

	/**
	 * 检查是否具有创建快捷方式的权限。
	 * 
	 * @param context
	 * @return
	 */
	public static boolean isWith_INSTALL_SHORTCUT_Permission(Context context) {
		return isWithPermission(context, "com.android.launcher.permission.INSTALL_SHORTCUT");
	}

	/**
	 * 检查是否具有添加系统浏览器书签的权限
	 * 
	 * @param context
	 * @return
	 */
	public static boolean isWith_WRITE_HISTORY_BOOKMARKS(Context context) {
		return isWithPermission(context, "com.android.browser.permission.WRITE_HISTORY_BOOKMARKS");
	}

	/**
	 * 检查是否具有SYSTEM_ALERT_WINDOW方法
	 * 
	 * @param context
	 * @return
	 */
	public static boolean isWith_SYSTEM_ALERT_WINDOW_Permission(Context context) {
		return isWithPermission(context, Manifest.permission.SYSTEM_ALERT_WINDOW);
	}

	/**
	 * 检查是否具有GET_TASK方法
	 * 
	 * @param context
	 * @return
	 */
	public static boolean isWith_GET_TASK_Permission(Context context) {
		return isWithPermission(context, Manifest.permission.GET_TASKS);
	}
}
