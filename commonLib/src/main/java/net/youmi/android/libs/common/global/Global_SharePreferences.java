package net.youmi.android.libs.common.global;

import android.content.Context;
import android.content.SharedPreferences;

import net.youmi.android.libs.common.coder.Coder_Base64;
import net.youmi.android.libs.common.coder.Coder_PBE;
import net.youmi.android.libs.common.debug.Debug_SDK;

/**
 * 全局SharedPreferences控制
 * <p>
 * 存储的内容经过PBE加密，外部使用时，注意加锁，可以参考platform类库中的Global_DeveloperConfig.java文件的使用
 * </p>
 * 
 * @author zhitaocai edit on 2014-5-14
 * 
 */
public class Global_SharePreferences {

	/**
	 * 将指定字符串加密并保存到sp文件中
	 * 
	 * @param context
	 * @param spFileName
	 *            sp文件名
	 * @param key_toSave_String
	 *            键
	 * @param toSave_String
	 *            值
	 * @param key_salt
	 *            盐（PBE）
	 * @return
	 */
	public static boolean saveEncodeStringToSharedPreferences(Context context, String spFileName,
			String key_toSave_String, String toSave_String, String key_salt) {
		boolean result = false;

		try {

			if (context == null) {
				return result;
			}

			String psw = context.getPackageName();
			byte[] saltBuff = Coder_PBE.initSalt();
			String saltString = new String(Coder_Base64.encode(saltBuff));
			String encodeString = Coder_PBE.encryptToBase64String(toSave_String, psw, saltBuff);
			if (encodeString != null && saltString != null) {
				SharedPreferences.Editor editor = context.getSharedPreferences(spFileName, Context.MODE_PRIVATE).edit();
				editor.putString(key_salt, saltString);
				editor.putString(key_toSave_String, encodeString);
				result = editor.commit();

				if (Debug_SDK.isGlobalLog) {
					Debug_SDK.td(Debug_SDK.mGlobalTag, Global_SharePreferences.class,
							"save data:\nv: %s  ,k: %s  ,psw: %s", encodeString, saltString, psw);
				}
			} else {
				if (Debug_SDK.isGlobalLog) {
					Debug_SDK.td(Debug_SDK.mGlobalTag, Global_SharePreferences.class,
							"save data:\nen==null || saltValue==null");
				}
			}

		} catch (Throwable e) {
			if (Debug_SDK.isGlobalLog) {
				Debug_SDK.te(Debug_SDK.mGlobalTag, Global_SharePreferences.class, e);
			}
		}

		return result;
	}

	/**
	 * 从sp文件获取指定的字符串(解密)
	 * 
	 * @param context
	 * @param spFileName
	 *            sp文件名
	 * @param key_toGet_String
	 *            键
	 * @param key_salt
	 *            盐（PBE）
	 * @param defaultValue
	 *            默认取出值
	 * @return
	 */
	public static String getStringFromSharedPreferences(Context context, String spFileName, String key_toGet_String,
			String key_salt, String defaultValue) {
		try {

			if (context == null) {
				return defaultValue;
			}
			SharedPreferences sp = context.getSharedPreferences(spFileName, Context.MODE_PRIVATE);
			if (sp == null) {
				return defaultValue;
			}
			String toDecodeString = sp.getString(key_toGet_String, null);
			if (toDecodeString == null) {
				if (Debug_SDK.isGlobalLog) {
					Debug_SDK.te(Debug_SDK.mGlobalTag, Global_SharePreferences.class, "enValue is null");
				}
				return defaultValue;
			}

			String saltValue = sp.getString(key_salt, null);
			if (saltValue == null) {
				if (Debug_SDK.isGlobalLog) {
					Debug_SDK.te(Debug_SDK.mGlobalTag, Global_SharePreferences.class, "saltValue is null");
				}
				return defaultValue;
			}

			byte[] salt = Coder_Base64.decode(saltValue.getBytes());

			if (salt == null) {
				if (Debug_SDK.isGlobalLog) {
					Debug_SDK.te(Debug_SDK.mGlobalTag, Global_SharePreferences.class, "salt is null");
				}
				return defaultValue;
			}

			String psw = context.getPackageName();
			String toGetString = Coder_PBE.decryptFromBase64String(toDecodeString, psw, salt);
			if (toGetString != null) {
				toGetString = toGetString.trim();
				if (Debug_SDK.isGlobalLog) {
					Debug_SDK.td(Debug_SDK.mGlobalTag, Global_SharePreferences.class, "decode value : %s", toGetString);
				}
				if (toGetString.length() <= 0) {
					return defaultValue;
				}
				return toGetString;
			}

		} catch (Throwable e) {
			if (Debug_SDK.isGlobalLog) {
				Debug_SDK.te(Debug_SDK.mGlobalTag, Global_SharePreferences.class, e);
			}
		}
		return defaultValue;
	}

}
