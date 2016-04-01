package net.youmi.android.libs.common.cache;

import net.youmi.android.libs.common.basic.Basic_StringUtil;
import net.youmi.android.libs.common.debug.Debug_SDK;

/**
 * 保存字节数组的代理缓存类
 * 
 * @author zhitaocai edit on 2014-6-27
 * 
 */
public class Proxy_Basic_CacheManager {

	private String mPsw;
	private Proxy_DB_Cache_Helper mDb;

	Proxy_Basic_CacheManager(String psw, Proxy_DB_Cache_Helper db) {
		mPsw = psw;
		mDb = db;
	}

	/**
	 * 保存缓存
	 * 
	 * @param context
	 * @param key
	 * @param value
	 * @param cacheValidTime
	 *            缓存保存时间，单位为ms，-1表示永久保存.
	 * @param encryptType
	 *            使用的加密模式,详见 {@linkplain Cache_Security_Type}
	 * @return
	 */
	protected boolean saveCache(String key, byte[] value, long cacheValidTime, int encryptType) {
		try {
			if (value == null || mPsw == null || mDb == null) {
				return false;
			}
			if (Basic_StringUtil.isNullOrEmpty(key)) {
				return false;
			}

			byte[] buff = Cache_Security_Manager.encryptValue(value, mPsw, encryptType);
			return mDb.saveCache(key, buff, cacheValidTime);
		} catch (Throwable e) {
			if (Debug_SDK.isCacheLog) {
				Debug_SDK.te(Debug_SDK.mCacheTag, this, e);
			}
		}
		return false;
	}

	/**
	 * 读取缓存
	 * 
	 * @param context
	 * @param key
	 * @param encryptType
	 *            使用的加密模式,详见 {@linkplain Cache_Security_Type}
	 * @return
	 */
	protected byte[] getCache(String key, int encryptType) {

		try {
			if (mDb == null || mPsw == null) {
				return null;
			}

			if (Basic_StringUtil.isNullOrEmpty(key)) {
				return null;
			}

			byte[] buffToDecrypt = mDb.getCache(key);

			if (buffToDecrypt == null) {
				return null;
			}

			return Cache_Security_Manager.decryptValue(buffToDecrypt, mPsw, encryptType);
		} catch (Throwable e) {
			if (Debug_SDK.isCacheLog) {
				Debug_SDK.te(Debug_SDK.mCacheTag, this, e);
			}
		}
		return null;
	}

	protected boolean removeCacheByCacheKey(String cacheKey) {
		return mDb.deleteCacheByCacheKey(cacheKey);
	}

	protected String getPassword() {
		return mPsw;
	}

	protected Proxy_DB_Cache_Helper getDbHelper() {
		return mDb;
	}

	protected String[] getKeys() {
		try {
			return mDb.getKeys();
		} catch (Throwable e) {
			if (Debug_SDK.isCacheLog) {
				Debug_SDK.te(Debug_SDK.mCacheTag, this, e);
			}
		}
		return null;
	}

}
