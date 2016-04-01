package net.youmi.android.libs.common.cache;

import net.youmi.android.libs.common.coder.Coder_GZIP_PBE;
import net.youmi.android.libs.common.debug.Debug_SDK;

/**
 * 缓存加解密管理器
 * 
 * @author zhitaocai edit on 2014-6-27
 * 
 */
public class Cache_Security_Manager {

	/**
	 * 加密value
	 * 
	 * @param buffToEncrypt
	 * @param psw
	 * @param encryptType
	 *            {@link Cache_Security_Type}
	 * @return
	 */
	public static byte[] encryptValue(byte[] buffToEncrypt, String psw, int encryptType) {
		byte[] result = null;
		try {
			switch (encryptType) {
			// 明文保存
			case Cache_Security_Type.TYPE_NONE:
				result = buffToEncrypt;
				break;

			// 先使用gzip压缩，加密内容，然后使用PBE加密
			case Cache_Security_Type.TYPE_PBE:
				result = Coder_GZIP_PBE.encrypt(buffToEncrypt, psw);
				break;

			default:
				result = Coder_GZIP_PBE.encrypt(buffToEncrypt, psw);
				break;
			}
		} catch (Throwable e) {
			if (Debug_SDK.isCacheLog) {
				Debug_SDK.te(Debug_SDK.mCacheTag, Cache_Security_Manager.class, e);
			}
		}
		return result;
	}

	/**
	 * 解密value
	 * 
	 * @param buffToDecrypt
	 * @param psw
	 * @param encryptType
	 *            {@link Cache_Security_Type}
	 * @return
	 */
	public static byte[] decryptValue(byte[] buffToDecrypt, String psw, int encryptType) {
		byte[] result = null;
		try {
			switch (encryptType) {

			// 明文保存的
			case Cache_Security_Type.TYPE_NONE:
				result = buffToDecrypt;
				break;

			// GZIP&PBE解密
			case Cache_Security_Type.TYPE_PBE:
				result = Coder_GZIP_PBE.decrypt(buffToDecrypt, psw);
				break;

			default:
				result = Coder_GZIP_PBE.decrypt(buffToDecrypt, psw);
				break;
			}
		} catch (Throwable e) {
			if (Debug_SDK.isCacheLog) {
				Debug_SDK.te(Debug_SDK.mCacheTag, Cache_Security_Manager.class, e);
			}
		}
		return result;
	}
}
