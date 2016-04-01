package net.youmi.android.libs.common.cache;

import android.content.Context;

import net.youmi.android.libs.common.debug.Debug_SDK;

/**
 * 缓存代理工具类 这些数据库的名字先不要改动
 * 
 * @author 林秋明 created on 2012-7-4
 * @author zhitaocai edit on 2014-6-27
 * 
 */
public class Proxy_DB_Cache_Helper extends Base_DB_Cache_Helper {

	public Proxy_DB_Cache_Helper(Context context, String dbName, int dbVersion, String tbName) {
		super(context, dbName, dbVersion, tbName);
	}

	public Proxy_DB_Cache_Helper(Context context, String dbName, int dbVersion) {
		super(context, dbName, dbVersion);
	}

	// -------------------------------------------------
	// 公共缓存数据库
	static final String DB_NAME_COMMON = "jqIqJYOT3JpT";
	private static final int DB_VERSION_COMMON = 2;
	private static Proxy_DB_Cache_Helper mInstance_Common;

	public synchronized static Proxy_DB_Cache_Helper getCommonDBInstance(Context context) {
		try {
			if (mInstance_Common == null) {
				mInstance_Common = new Proxy_DB_Cache_Helper(context, DB_NAME_COMMON, DB_VERSION_COMMON);
			}
		} catch (Throwable e) {
			if (Debug_SDK.isCacheLog) {
				Debug_SDK.te(Debug_SDK.mCacheTag, Proxy_DB_Cache_Helper.class, e);
			}
		}
		return mInstance_Common;
	}

	// // -------------------------------------------------
	// // App缓存数据库 (暂时没有使用，先注释看看)
	// private static final String DB_NAME_APPS = "adi5EtjD8Wgf";
	// private static final int DB_VERSION_APPS = 3;
	// private static Proxy_DB_Cache_Helper mInstance_Apps;
	//
	// public synchronized static Proxy_DB_Cache_Helper getAppDBInstance(Context context) {
	// try {
	// if (mInstance_Apps == null) {
	// mInstance_Apps = new Proxy_DB_Cache_Helper(context, DB_NAME_APPS, DB_VERSION_APPS);
	// }
	// } catch (Throwable e) {
	// if (Debug_SDK.isDebug && CacheLog.isOpen) {
	// Debug_SDK.de(mTag, e);
	// }
	// }
	// return mInstance_Apps;
	// }

	// // -------------------------------------------------
	// // APP安装卸载统计记录数据库 (暂时没有使用，先注释看看 )
	// private static final String DB_NAME_COUNTER_APP_RECORD = "aCmbBfv150Sr";
	// private static final int DB_VERSION_COUNTER_APP_RECORD = 2;
	// private static Proxy_DB_Cache_Helper mInstance_COUNTER_APP_RECORD;
	//
	// public synchronized static Proxy_DB_Cache_Helper getCounterAppRecordDBInstance(Context context) {
	// try {
	// if (mInstance_COUNTER_APP_RECORD == null) {
	// mInstance_COUNTER_APP_RECORD = new Proxy_DB_Cache_Helper(context, DB_NAME_COUNTER_APP_RECORD,
	// DB_VERSION_COUNTER_APP_RECORD);
	// }
	// } catch (Throwable e) {
	// if (Debug_SDK.isDebug && CacheLog.isOpen) {
	// Debug_SDK.de(mTag, e);
	// }
	// }
	// return mInstance_COUNTER_APP_RECORD;
	// }

	// // -------------------------------------------------
	// // Ads缓存数据库 (暂时没有使用，先注释看看)
	// private static final String DB_NAME_ADS = "HteeAXH3thzw";
	// private static final int DB_VERSION_ADS = 3;
	// private static Proxy_DB_Cache_Helper mInstance_Ads;
	//
	// public synchronized static Proxy_DB_Cache_Helper getAdsDBInstance(Context context) {
	// try {
	// if (mInstance_Ads == null) {
	// mInstance_Ads = new Proxy_DB_Cache_Helper(context, DB_NAME_ADS, DB_VERSION_ADS);
	// }
	// } catch (Throwable e) {
	// if (Debug_SDK.isDebug && CacheLog.isOpen) {
	// Debug_SDK.de(mTag, e);
	// }
	// }
	// return mInstance_Ads;
	// }
}
