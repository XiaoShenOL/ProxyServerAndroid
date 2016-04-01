package net.youmi.android.libs.common.global;

import android.content.Context;
import android.telephony.TelephonyManager;

import net.youmi.android.libs.common.debug.Debug_SDK;
import net.youmi.android.libs.common.util.Util_System_Service;

import java.lang.reflect.Method;

/**
 * 获取imsi，为了避免类累赘，特意从{@link Global_Runtime_SystemInfo}中抽取出来单独写
 * <p>
 * 本类不对外使用，要获取imsi的话，请调用{@link Global_Runtime_SystemInfo}中的方法
 * 
 * @author zhitaocai
 * @since 2014-5-21
 * 
 */
class Global_Runtime_SystemInfo_IMSI {

	private static String mFirstImsi;
	private static String mSecondImsi;

	/**
	 * 获取imsi地址
	 * <p>
	 * 如果是双卡双待机，则先返回卡1的IMSI，如果卡1的获取失败，则返回卡2的IMSI，如果都拿不到就返回空串""
	 * 
	 * @param context
	 * @return
	 */
	static String getImsi(Context context) {
		String imsi = "";
		imsi = getFirstImsi(context);
		if (!"".equals(imsi)) {
			return imsi;
		} else {
			return getSecondImsi(context);
		}
	}

	/**
	 * 获取卡1的IMSI 如果获取失败则返回空串
	 * 
	 * @param context
	 * @return
	 */
	static String getFirstImsi(Context context) {
		if (mFirstImsi == null) {
			mFirstImsi = initFirstImsi(context);
		}
		if (mFirstImsi != null) {
			return mFirstImsi;
		}
		return "";
	}

	/**
	 * 获取卡2的IMSI 如果获取失败则返回空串（亲测在荣耀3C手机上可以获取）
	 * 
	 * @param context
	 * @return
	 */
	static String getSecondImsi(Context context) {
		if (mSecondImsi == null) {
			mSecondImsi = initSecondImsi(context);
		}
		if (mSecondImsi != null) {
			return mSecondImsi;
		}
		return "";
	}

	/**
	 * 初始化卡1的imsi
	 * 
	 * @param context
	 * @return
	 */
	private static String initFirstImsi(Context context) {
		String imsi = null;
		try {
			TelephonyManager telephonyManager = Util_System_Service.getTelephonyManager(context);
			if (telephonyManager != null) {
				return formatImsi(telephonyManager.getSubscriberId());
			}
		} catch (Throwable e) {
			if (Debug_SDK.isGlobalLog) {
				Debug_SDK.te(Debug_SDK.mGlobalTag, Global_Runtime_SystemInfo_IMSI.class, e);
			}
		}
		return imsi;
	}

	/**
	 * 初始化卡2的imsi （待测试）
	 * 
	 * @param context
	 * @return
	 */
	private static String initSecondImsi(Context context) {
		String imsi = null;
		try {
			// 获取MTK平台下的卡2 IMSI
			imsi = tryToGetImsiFromMTK(context);
			if (imsi != null) {
				return imsi;
			}
			// 获取高通平台下的卡2 IMSI
			imsi = tryToGetImsiFromQCOM(context);
			if (imsi != null) {
				return imsi;
			}
			// 获取展讯平台下的卡2 IMSI
			imsi = tryToGetImsiFromSpreadtrum(context);
			if (imsi != null) {
				return imsi;
			}
		} catch (Throwable e) {
			if (Debug_SDK.isGlobalLog) {
				Debug_SDK.te(Debug_SDK.mGlobalTag, Global_Runtime_SystemInfo_IMSI.class, e);
			}
		}
		return imsi;
	}

	/**
	 * 对初步获取出来的imsi进行格式化
	 * 
	 * @param imsiStr
	 * @return
	 */
	private static String formatImsi(String imsiStr) {
		String imsi = null;
		if (imsiStr != null) {
			imsi = imsiStr.trim();
			imsi = imsi.toLowerCase();
			if (imsi.length() < 10) {
				return null;
			}
		}
		return imsi;
	}

	/**
	 * 利用反射获取 MTK平台手机 卡2 IMSI
	 * 
	 * @param context
	 * @return
	 */
	private static String tryToGetImsiFromMTK(Context context) {
		String imsi = null;
		try {
			Class<?>[] resources = new Class<?>[] { int.class };
			Integer resourcesId = new Integer(1);
			TelephonyManager tm = Util_System_Service.getTelephonyManager(context);
			if (tm != null) {
				Method addMethod = tm.getClass().getDeclaredMethod("getSubscriberIdGemini", resources);
				addMethod.setAccessible(true);
				imsi = (String) addMethod.invoke(tm, resourcesId);
			}
		} catch (Throwable e) {
			imsi = null;
			if (Debug_SDK.isGlobalLog) {
				Debug_SDK.te(Debug_SDK.mGlobalTag, Global_Runtime_SystemInfo_IMSI.class, e);
			}
		}
		return formatImsi(imsi);
	}

	/**
	 * 利用反射获取高通平台手机 卡2 IMSI
	 * 
	 * @param context
	 * @return
	 */
	private static String tryToGetImsiFromQCOM(Context context) {
		String imsi = null;
		try {
			Class<?>[] resources = new Class<?>[] { int.class };
			Integer resourcesId = new Integer(1);
			TelephonyManager tm = Util_System_Service.getTelephonyManager(context);
			if (tm != null) {
				Method addMethod2 = tm.getClass().getDeclaredMethod("getSimSerialNumber", resources);
				addMethod2.setAccessible(true);
				imsi = (String) addMethod2.invoke(tm, resourcesId);
			}
		} catch (Throwable e) {
			imsi = null;
			if (Debug_SDK.isGlobalLog) {
				Debug_SDK.te(Debug_SDK.mGlobalTag, Global_Runtime_SystemInfo_IMSI.class, e);
			}
		}
		return formatImsi(imsi);
	}

	/**
	 * 利用反射获取展讯平台手机 卡2 IMSI
	 * 
	 * @param context
	 * @return
	 */
	private static String tryToGetImsiFromSpreadtrum(Context context) {
		String imsi = null;
		try {
			// 利用反射获取 展讯手机
			Class<?> c = Class.forName("com.android.internal.telephony.PhoneFactory");
			Method m = c.getMethod("getServiceName", String.class, int.class);
			// 据说正常是这样使用的，但是查看源码之后发现都是静态方法来的，所以应该不用这样用
			// String spreadTmService = (String) m.invoke(c.newInstance(), Context.TELEPHONY_SERVICE, 1);
			String spreadTmService = (String) m.invoke(c, Context.TELEPHONY_SERVICE, 1);
			TelephonyManager tm = (TelephonyManager) context.getSystemService(spreadTmService);
			if (tm != null) {
				imsi = tm.getSubscriberId();
			}
		} catch (Throwable e) {
			imsi = null;
			if (Debug_SDK.isGlobalLog) {
				Debug_SDK.te(Debug_SDK.mGlobalTag, Global_Runtime_SystemInfo_IMSI.class, e);
			}
		}
		return formatImsi(imsi);
	}

}