package net.youmi.android.libs.common.cache;

/**
 * 缓存加解密类型
 */
public class Cache_Security_Type {

	/**
	 * 明文保存，不使用加密
	 */
	public final static int TYPE_NONE = 0;

	/**
	 * 使用GZIP&PBE加密
	 */
	public final static int TYPE_PBE = 1;

}
