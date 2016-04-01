package net.youmi.android.libs.common.cache;

import android.content.Context;

import net.youmi.android.libs.common.debug.Debug_SDK;

/**
 * 公共缓存类
 * <p>
 * 1、可以保存字符串缓存 <br>
 * 2、可以保存实现接口{@linkplain Interface_Serializable}的对象
 * 
 * @author jen
 * @author zhitaocai edit on 2014-6-27
 */
public class Proxy_Common_CacheManager {

	private final static String DEFAULT_PSW_COMMON = "R42ga7hZ";
	private static Proxy_String_CacheManger mStringCacheManger;
	private static Proxy_Serializable_CacheManager mSerializableCacheManager;

	private synchronized static void initStringCacheManager(Context context) {
		try {
			if (mStringCacheManger == null) {
				mStringCacheManger = new Proxy_String_CacheManger(DEFAULT_PSW_COMMON,
						Proxy_DB_Cache_Helper.getCommonDBInstance(context));
			}
		} catch (Throwable e) {
			if (Debug_SDK.isCacheLog) {
				Debug_SDK.te(Debug_SDK.mCacheTag, Proxy_Common_CacheManager.class, e);
			}
		}
	}

	private synchronized static void initSerializableCacheManager(Context context) {
		try {
			if (mSerializableCacheManager == null) {
				mSerializableCacheManager = new Proxy_Serializable_CacheManager(context, DEFAULT_PSW_COMMON,
						Proxy_DB_Cache_Helper.getCommonDBInstance(context));
			}
		} catch (Throwable e) {
			if (Debug_SDK.isCacheLog) {
				Debug_SDK.te(Debug_SDK.mCacheTag, Proxy_Common_CacheManager.class, e);
			}
		}
	}

	/**
	 * 保存缓存
	 * 
	 * @param context
	 * @param key
	 * @param value
	 * @param validTime_ms
	 *            缓存时间（ -1 为永久缓存）
	 * @return
	 */
	public static boolean saveCache(Context context, String key, String value, long validTime_ms) {
		try {
			initStringCacheManager(context);
			return mStringCacheManger.saveCache(key, value, validTime_ms);
		} catch (Throwable e) {
			if (Debug_SDK.isCacheLog) {
				Debug_SDK.te(Debug_SDK.mCacheTag, Proxy_Common_CacheManager.class, e);
			}
		}
		return false;
	}

	/**
	 * 保存缓存
	 * 
	 * @param context
	 * @param key
	 * @param value
	 * @param validTime_ms
	 *            缓存时间（ -1 为永久缓存）
	 * @return
	 */
	public static boolean saveCache(Context context, String key, boolean value, long validTime_ms) {
		try {
			initStringCacheManager(context);
			String v = value ? "1" : "0";
			return mStringCacheManger.saveCache(key, v, validTime_ms);
		} catch (Throwable e) {
			if (Debug_SDK.isCacheLog) {
				Debug_SDK.te(Debug_SDK.mCacheTag, Proxy_Common_CacheManager.class, e);
			}
		}
		return false;
	}

	public static boolean saveCache(Context context, Interface_Serializable serializable) {
		try {
			initSerializableCacheManager(context);
			return mSerializableCacheManager.saveCache(serializable);
		} catch (Throwable e) {
			if (Debug_SDK.isCacheLog) {
				Debug_SDK.te(Debug_SDK.mCacheTag, Proxy_Common_CacheManager.class, e);
			}
		}
		return false;
	}

	/**
	 * 获取缓存
	 * 
	 * @param context
	 * @param key
	 * @param defaultValue
	 * @return
	 */
	public static String getCache(Context context, String key, String defaultValue) {
		try {
			initStringCacheManager(context);
			return mStringCacheManger.getCache(key, defaultValue);
		} catch (Throwable e) {
			if (Debug_SDK.isCacheLog) {
				Debug_SDK.te(Debug_SDK.mCacheTag, Proxy_Common_CacheManager.class, e);
			}
		}
		return defaultValue;
	}

	/**
	 * 获取缓存
	 * 
	 * @param context
	 * @param serializable
	 * @return
	 */
	public static boolean getCache(Context context, Interface_Serializable serializable) {
		try {
			initSerializableCacheManager(context);
			return mSerializableCacheManager.getCache(serializable);
		} catch (Throwable e) {
			if (Debug_SDK.isCacheLog) {
				Debug_SDK.te(Debug_SDK.mCacheTag, Proxy_Common_CacheManager.class, e);
			}
		}
		return false;
	}

	/**
	 * 获取boolean型的值
	 * 
	 * @param context
	 * @param key
	 * @param dfValue
	 * @return
	 */
	public static boolean getCache(Context context, String key, boolean dfValue) {
		try {
			initStringCacheManager(context);
			String v = mStringCacheManger.getCache(key, null);

			if (v == null) {
				return dfValue;
			}

			if ("1".equals(v)) {
				return true;
			}

			if ("0".equals(v)) {
				return false;
			}
		} catch (Throwable e) {
			if (Debug_SDK.isCacheLog) {
				Debug_SDK.te(Debug_SDK.mCacheTag, Proxy_Common_CacheManager.class, e);
			}
		}
		return dfValue;
	}

	/**
	 * 获取公共缓存的文件路径(只有十分个别的用例需要用到这个方法，尽量不要使用这个方法)
	 * 
	 * @return
	 */
	public static String getCommomCacheFilePath(Context context) {
		return "/data/data/" + context.getPackageName() + "/databases/" + Proxy_DB_Cache_Helper.DB_NAME_COMMON;
	}

}
