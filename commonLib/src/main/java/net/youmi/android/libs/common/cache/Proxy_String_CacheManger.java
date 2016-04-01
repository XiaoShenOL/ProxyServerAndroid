package net.youmi.android.libs.common.cache;

import net.youmi.android.libs.common.debug.Debug_SDK;
import net.youmi.android.libs.common.global.Global_Charsets;

/**
 * 保存字符串的代理缓存类
 * 
 * @author zhitaocai edit on 2014-6-27
 */
public class Proxy_String_CacheManger {

	/**
	 * 基础代理缓存管理器
	 */
	Proxy_Basic_CacheManager mCacheManager;

	/**
	 * 加密类型，指定明文或各种加密方式，默认加密算法为PBE
	 */
	private int mEncryptType = Cache_Security_Type.TYPE_PBE;

	public Proxy_String_CacheManger(String psw, Proxy_DB_Cache_Helper db) {
		mCacheManager = new Proxy_Basic_CacheManager(psw, db);
	}

	public Proxy_String_CacheManger(String psw, Proxy_DB_Cache_Helper db, int encryptType) {
		this(psw, db);
		mEncryptType = encryptType;
	}

	/**
	 * 保存缓存
	 * 
	 * @param key
	 * @param value
	 * @param validTime_ms
	 * @return
	 */
	public boolean saveCache(String key, String value, long validTime_ms) {
		try {
			if (value == null || key == null) {
				return false;
			}
			byte[] buff = value.getBytes(Global_Charsets.UTF_8);
			return mCacheManager.saveCache(key, buff, validTime_ms, mEncryptType);
		} catch (Throwable e) {
			if (Debug_SDK.isCacheLog) {
				Debug_SDK.te(Debug_SDK.mCacheTag, this, e);
			}
		}
		return false;
	}

	/**
	 * 获取缓存
	 * 
	 * @param key
	 * @param defaultValue
	 * @return
	 */
	public String getCache(String key, String defaultValue) {
		try {

			byte[] buff = mCacheManager.getCache(key, mEncryptType);
			if (buff != null) {
				String value = new String(buff, Global_Charsets.UTF_8);
				if (value != null) {
					value = value.trim();
					if (value.length() > 0) {
						return value;
					}
				}
			}

		} catch (Throwable e) {
			if (Debug_SDK.isCacheLog) {
				Debug_SDK.te(Debug_SDK.mCacheTag, this, e);
			}
		}
		return defaultValue;
	}

	public boolean removeCacheByCacheKey(String cacheKey) {
		return mCacheManager.removeCacheByCacheKey(cacheKey);
	}

	public int getEncryptType() {
		return mEncryptType;
	}

	public String getPassword() {
		return mCacheManager.getPassword();
	}

	public Proxy_DB_Cache_Helper getDbHelper() {
		return mCacheManager.getDbHelper();
	}

	public String[] getKeys() {
		try {
			return mCacheManager.getDbHelper().getKeys();
		} catch (Throwable e) {
			if (Debug_SDK.isCacheLog) {
				Debug_SDK.te(Debug_SDK.mCacheTag, this, e);
			}
		}
		return null;
	}

}
