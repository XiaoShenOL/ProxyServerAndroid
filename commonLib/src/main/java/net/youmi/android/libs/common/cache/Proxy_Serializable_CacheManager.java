package net.youmi.android.libs.common.cache;

import android.content.Context;

import net.youmi.android.libs.common.coder.Coder_GZIP_PBE;
import net.youmi.android.libs.common.coder.Coder_Md5;
import net.youmi.android.libs.common.debug.Debug_SDK;
import net.youmi.android.libs.common.global.Global_Charsets;

import java.util.ArrayList;
import java.util.List;

/**
 * 将序列化对象变成Json字符串保存到缓存中，已经反序列化字符串获取json对象
 * 
 * @author zhitaocai edit on 2014-6-27
 * 
 */
public class Proxy_Serializable_CacheManager {

	/**
	 * 字符串缓存代理类
	 */
	private Proxy_String_CacheManger mStringCacheManager;

	public Proxy_Serializable_CacheManager(Context context, String psw, Proxy_DB_Cache_Helper db) {
		mStringCacheManager = new Proxy_String_CacheManger(psw, db);
	}

	public Proxy_Serializable_CacheManager(Context context, String psw, Proxy_DB_Cache_Helper db, int encryptType) {
		mStringCacheManager = new Proxy_String_CacheManger(psw, db, encryptType);
	}

	public boolean saveCache(Interface_Serializable[] list) {
		try {

			if (list == null) {
				if (Debug_SDK.isCacheLog) {
					Debug_SDK.td(Debug_SDK.mCacheTag, this, "list == null");
				}
				return false;
			}
			if (list.length == 0) {
				if (Debug_SDK.isCacheLog) {
					Debug_SDK.td(Debug_SDK.mCacheTag, this, "list.length == 0");
				}
				return false;
			}

			List<Cache_Model> cacheList = new ArrayList<Cache_Model>();

			for (int i = 0; i < list.length; i++) {
				Interface_Serializable serializable = list[i];
				if (serializable != null) {

					String key = serializable.getCacheKey();
					String jsonValue = serializable.serialize();

					if (jsonValue == null || key == null) {
						continue;
					}
					long cacheValidTime_ms = serializable.getValidCacheTime_ms();
					byte[] jsonData = jsonValue.getBytes(Global_Charsets.UTF_8);
					byte[] data = Coder_GZIP_PBE.encrypt(jsonData, mStringCacheManager.getPassword());

					Cache_Model item = new Cache_Model(key, data, cacheValidTime_ms);
					cacheList.add(item);

				}
			}

			return mStringCacheManager.getDbHelper().saveCacheList(cacheList);

		} catch (Throwable e) {
			if (Debug_SDK.isCacheLog) {
				Debug_SDK.te(Debug_SDK.mCacheTag, this, e);
			}
		}
		return false;
	}

	public boolean saveCache(Interface_Serializable value) {
		try {
			if (value == null) {
				return false;
			}
			String key = value.getCacheKey();
			if (key == null) {
				return false;
			}
			String jsonValue = value.serialize();
			return mStringCacheManager.saveCache(key, jsonValue, value.getValidCacheTime_ms());
		} catch (Throwable e) {
			if (Debug_SDK.isCacheLog) {
				Debug_SDK.te(Debug_SDK.mCacheTag, this, e);
			}
		}
		return false;
	}

	public boolean getCache(Interface_Serializable serializable) {
		try {
			if (serializable == null) {
				if (Debug_SDK.isCacheLog) {
					Debug_SDK.td(Debug_SDK.mCacheTag, this, "serializable is null");
				}
				return false;
			}

			String key = serializable.getCacheKey();
			String json = mStringCacheManager.getCache(key, null);
			if (json == null) {
				if (Debug_SDK.isCacheLog) {
					Debug_SDK.td(Debug_SDK.mCacheTag, this, "json is null");
				}
				return false;
			}

			return serializable.deserialize(json);

		} catch (Throwable e) {
			if (Debug_SDK.isCacheLog) {
				Debug_SDK.te(Debug_SDK.mCacheTag, this, e);
			}
		}
		return false;
	}

	public boolean removeCacheByCacheKey(String cacheKey) {
		return mStringCacheManager.removeCacheByCacheKey(cacheKey);
	}

	public String generateMd5CacheKey(String src) {
		try {
			return Coder_Md5.md5(src);
		} catch (Throwable e) {
			if (Debug_SDK.isCacheLog) {
				Debug_SDK.te(Debug_SDK.mCacheTag, this, e);
			}
		}
		return src;
	}

	public int getEncryptType() {
		return mStringCacheManager.getEncryptType();
	}

	public Proxy_DB_Cache_Helper getDbHelper() {
		return mStringCacheManager.getDbHelper();
	}

	public String[] getKeys() {
		try {
			return mStringCacheManager.getDbHelper().getKeys();
		} catch (Throwable e) {
			if (Debug_SDK.isCacheLog) {
				Debug_SDK.te(Debug_SDK.mCacheTag, this, e);
			}
		}
		return null;
	}

	public String getPassword() {
		return mStringCacheManager.getPassword();
	}
}
