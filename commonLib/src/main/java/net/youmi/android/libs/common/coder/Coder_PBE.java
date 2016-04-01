package net.youmi.android.libs.common.coder;

import net.youmi.android.libs.common.debug.Debug_SDK;
import net.youmi.android.libs.common.global.Global_Charsets;

import java.security.Key;
import java.util.Random;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.PBEParameterSpec;

public class Coder_PBE {

	/**
	 * 支持以下任意一种算法
	 * <p/>
	 * 
	 * <pre>
	 * PBEWithMD5AndDES
	 * PBEWithMD5AndTripleDES
	 * PBEWithSHA1AndDESede
	 * PBEWithSHA1AndRC2_40
	 * </pre>
	 */
	public static final String ALGORITHM = "PBEWITHMD5andDES";

	/**
	 * 盐初始化
	 * 
	 * @return
	 * @throws Throwable
	 */
	public static byte[] initSalt() throws Throwable {
		byte[] salt = new byte[8];
		Random random = new Random();
		random.nextBytes(salt);
		return salt;
	}

	/**
	 * 转换密钥<br>
	 * 
	 * @param password
	 * @return
	 * @throws Throwable
	 */
	private static Key toKey(String password) throws Throwable {
		PBEKeySpec keySpec = new PBEKeySpec(password.toCharArray());
		SecretKeyFactory keyFactory = SecretKeyFactory.getInstance(ALGORITHM);
		SecretKey secretKey = keyFactory.generateSecret(keySpec);

		return secretKey;
	}

	/**
	 * 加密
	 * 
	 * @param data
	 *            数据
	 * @param password
	 *            密码
	 * @param salt
	 *            盐
	 * @return
	 * @throws Throwable
	 */
	public static byte[] encrypt(byte[] data, String password, byte[] salt) throws Throwable {

		Key key = toKey(password);

		PBEParameterSpec paramSpec = new PBEParameterSpec(salt, 100);
		Cipher cipher = Cipher.getInstance(ALGORITHM);
		cipher.init(Cipher.ENCRYPT_MODE, key, paramSpec);

		return cipher.doFinal(data);

	}

	/**
	 * @param toEncrypt
	 *            加密原始串
	 * @param password
	 *            密码
	 * @param salt
	 *            盐
	 * @return
	 */
	public static String encryptToBase64String(String toEncrypt, String password, byte[] salt) {

		try {
			return new String(Coder_Base64.encode(encrypt(toEncrypt.getBytes(), password, salt)), Global_Charsets.UTF_8);
		} catch (Throwable e) {
			if (Debug_SDK.isCoderLog) {
				Debug_SDK.te(Debug_SDK.mCoderTag, Coder_PBE.class, e);
			}
		}
		return null;
	}

	/**
	 * @param toEncrypt
	 *            加密原始串
	 * @param password
	 *            密码
	 * @param salt
	 *            盐
	 * @return
	 */
	public static String encryptToBase64String(byte[] toEncrypt, String password, byte[] salt) {

		try {
			return new String(Coder_Base64.encode(encrypt(toEncrypt, password, salt)), Global_Charsets.UTF_8);
		} catch (Throwable e) {
			if (Debug_SDK.isCoderLog) {
				Debug_SDK.te(Debug_SDK.mCoderTag, Coder_PBE.class, e);
			}
		}
		return null;
	}

	/**
	 * 解密
	 * 
	 * @param data
	 *            数据
	 * @param password
	 *            密码
	 * @param salt
	 *            盐
	 * @return
	 * @throws Throwable
	 */
	public static byte[] decrypt(byte[] data, String password, byte[] salt) throws Throwable {
		Key key = toKey(password);
		PBEParameterSpec paramSpec = new PBEParameterSpec(salt, 100);
		Cipher cipher = Cipher.getInstance(ALGORITHM);
		cipher.init(Cipher.DECRYPT_MODE, key, paramSpec);
		return cipher.doFinal(data);
	}

	public static String decryptFromBase64String(String base64String, String password, byte[] salt) {
		try {
			byte[] data = Coder_Base64.decode(base64String.getBytes());
			byte[] buff = decrypt(data, password, salt);
			return new String(buff);

		} catch (Throwable e) {
			if (Debug_SDK.isCoderLog) {
				Debug_SDK.te(Debug_SDK.mCoderTag, Coder_PBE.class, e);
			}
		}
		return null;
	}

	public static byte[] decryptFromBase64StringToBytes(String base64String, String password, byte[] salt) {
		try {
			byte[] data = Coder_Base64.decode(base64String.getBytes());
			byte[] buff = decrypt(data, password, salt);
			return buff;
		} catch (Throwable e) {
			if (Debug_SDK.isCoderLog) {
				Debug_SDK.te(Debug_SDK.mCoderTag, Coder_PBE.class, e);
			}
		}
		return null;
	}

}
