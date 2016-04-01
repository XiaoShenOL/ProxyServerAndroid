package net.youmi.android.libs.common.util;

import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ServiceInfo;

import net.youmi.android.libs.common.debug.Debug_SDK;

/**
 * 组件检查类
 * 
 * @author zhitaocai edit on 2014-7-15
 * 
 */
public class Util_System_Component {

	/**
	 * 判断Activity是否存在
	 * 
	 * @param context
	 * @param activityClass
	 * @return
	 */
	public static boolean isActivityExist(Context context, Class<?> activityClass) {
		if (activityClass == null) {
			return false;
		}
		return isActivityExist(context, activityClass.getName());
	}

	/**
	 * 判断Activity是否存在。
	 * 
	 * @param context
	 * @param activityClassName
	 * @return
	 */
	public static boolean isActivityExist(Context context, String activityClassName) {
		try {
			ActivityInfo ai = getActivityInfo(context, activityClassName);
			return ai != null;
		} catch (Throwable e) {
			if (Debug_SDK.isUtilLog) {
				Debug_SDK.te(Debug_SDK.mUtilTag, Util_System_Component.class, e);
			}
		}
		return false;
	}

	/**
	 * 获取ActivityInfo。
	 * 
	 * @param context
	 * @param activityClassName
	 * @return
	 */
	public static ActivityInfo getActivityInfo(Context context, String activityClassName) {
		try {
			if (context == null || activityClassName == null) {
				return null;
			}

			PackageInfo pi = context.getPackageManager().getPackageInfo(context.getPackageName(),
					PackageManager.GET_ACTIVITIES);
			if (pi == null) {
				return null;
			}

			ActivityInfo[] ais = pi.activities;
			if (ais == null) {
				return null;
			}

			if (ais != null) {
				for (int i = 0; i < ais.length; i++) {
					ActivityInfo ai = ais[i];
					if (ai.name.equals(activityClassName)) {
						return ai;
					}
				}
			}
		} catch (Throwable e) {
			if (Debug_SDK.isUtilLog) {
				Debug_SDK.te(Debug_SDK.mUtilTag, Util_System_Component.class, e);
			}
		}
		return null;
	}

	// Service

	/**
	 * 判断Service是否存在
	 * 
	 * @param context
	 * @param serviceClass
	 * @return
	 */
	public static boolean isServiceExist(Context context, Class<?> serviceClass) {
		if (serviceClass == null) {
			return false;
		}
		return isServiceExist(context, serviceClass.getName());
	}

	/**
	 * 判断Service是否存在。
	 * 
	 * @param context
	 * @param serviceClassName
	 * @return
	 */
	public static boolean isServiceExist(Context context, String serviceClassName) {
		try {
			ServiceInfo si = getServiceInfo(context, serviceClassName);
			return si != null;
		} catch (Throwable e) {
			if (Debug_SDK.isUtilLog) {
				Debug_SDK.te(Debug_SDK.mUtilTag, Util_System_Component.class, e);
			}
		}
		return false;
	}

	/**
	 * 获取ServiceInfo。
	 * 
	 * @param context
	 * @param serviceClassName
	 * @return
	 */
	public static ServiceInfo getServiceInfo(Context context, String serviceClassName) {
		try {
			if (context == null || serviceClassName == null) {
				return null;
			}

			PackageInfo pi = context.getPackageManager().getPackageInfo(context.getPackageName(),
					PackageManager.GET_SERVICES);
			if (pi == null) {
				return null;
			}

			ServiceInfo[] sis = pi.services;
			if (sis == null) {
				return null;
			}

			if (sis != null) {
				for (int i = 0; i < sis.length; i++) {
					ServiceInfo si = sis[i];
					if (si.name.equals(serviceClassName)) {
						return si;
					}
				}
			}
		} catch (Throwable e) {
			if (Debug_SDK.isUtilLog) {
				Debug_SDK.te(Debug_SDK.mUtilTag, Util_System_Component.class, e);
			}
		}
		return null;
	}

	// Receiver

	/**
	 * 判断Receiver是否存在
	 * 
	 * @param context
	 * @param receiverClass
	 * @return
	 */
	public static boolean isReceiverExist(Context context, Class<?> receiverClass) {
		if (receiverClass == null) {
			return false;
		}
		return isReceiverExist(context, receiverClass.getName());
	}

	/**
	 * 判断Receiver是否存在。
	 * 
	 * @param context
	 * @param receiverClassName
	 * @return
	 */
	public static boolean isReceiverExist(Context context, String receiverClassName) {
		ActivityInfo ai = getReceiverInfo(context, receiverClassName);
		return ai != null;
	}

	/**
	 * 通过Recevier的类名获取Receiver
	 * 
	 * @param context
	 * @param receiverClassName
	 * @return
	 */
	public static ActivityInfo getReceiverInfo(Context context, String receiverClassName) {

		try {

			if (context == null || receiverClassName == null) {
				return null;
			}

			PackageInfo pi = context.getPackageManager().getPackageInfo(context.getPackageName(),
					PackageManager.GET_RECEIVERS);
			if (pi == null) {
				return null;
			}

			ActivityInfo[] ais = pi.receivers;
			if (ais == null) {
				return null;
			}

			if (ais != null) {
				for (int i = 0; i < ais.length; i++) {
					ActivityInfo ai = ais[i];
					if (ai.name.equals(receiverClassName)) {
						return ai;
					}
				}
			}
		} catch (Throwable e) {
			if (Debug_SDK.isUtilLog) {
				Debug_SDK.te(Debug_SDK.mUtilTag, Util_System_Component.class, e);
			}
		}
		return null;
	}

}
