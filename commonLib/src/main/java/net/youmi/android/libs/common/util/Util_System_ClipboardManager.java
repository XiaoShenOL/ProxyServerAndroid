package net.youmi.android.libs.common.util;

import android.annotation.SuppressLint;
import android.content.ClipData;
import android.content.ClipDescription;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Build;

import net.youmi.android.libs.common.debug.Debug_SDK;

/**
 * 剪切板使用（暂时只支持文字剪切）
 * 
 * @author zhitaocai edit on 2014-7-15
 * 
 */
@SuppressLint("NewApi")
public class Util_System_ClipboardManager {

	/**
	 * 保存文字到剪切板中
	 * 
	 * @param str
	 * @return
	 */
	public static boolean setText(Context context, String str) {

		if (context == null) {
			return false;
		}
		Context appliactionContext = null;
		try {
			appliactionContext = context.getApplicationContext();
		} catch (Exception e) {
		}
		if (appliactionContext == null) {
			return false;
		}

		// 如果当前设备的android-sdk 版本号小于11
		if (Build.VERSION.SDK_INT < 11) {
			try {
				android.text.ClipboardManager clipManager = (android.text.ClipboardManager) appliactionContext
						.getSystemService(Context.CLIPBOARD_SERVICE);
				clipManager.setText(str);
				return true;
			} catch (Exception e) {
				if (Debug_SDK.isUtilLog) {
					Debug_SDK.te(Debug_SDK.mUtilTag, Util_System_ClipboardManager.class, e);
				}
			}
			return false;
		}

		// 如果当前设备的android-sdk 版本号大于等于11
		else {
			try {
				ClipboardManager clipManager = (ClipboardManager) appliactionContext
						.getSystemService(Context.CLIPBOARD_SERVICE);
				ClipData clip = ClipData.newPlainText("simple text", str);
				clipManager.setPrimaryClip(clip);
				return true;
			} catch (Exception e) {
				if (Debug_SDK.isUtilLog) {
					Debug_SDK.te(Debug_SDK.mUtilTag, Util_System_ClipboardManager.class, e);
				}
			}
			return false;
		}
	}

	/**
	 * 获取剪切版中的文字，如果有的话
	 * 
	 * @param context
	 * @return
	 */
	public static String getText(Context context) {
		if (context == null) {
			return null;
		}
		Context appliactionContext = null;
		try {
			appliactionContext = context.getApplicationContext();
		} catch (Exception e) {
			if (Debug_SDK.isUtilLog) {
				Debug_SDK.te(Debug_SDK.mUtilTag, Util_System_ClipboardManager.class, e);
			}
		}
		if (appliactionContext == null) {
			return null;
		}

		// 如果当前设备的android-sdk 版本号小于11
		if (Build.VERSION.SDK_INT < 11) {
			try {
				android.text.ClipboardManager clipManager = (android.text.ClipboardManager) appliactionContext
						.getSystemService(Context.CLIPBOARD_SERVICE);
				if (clipManager.hasText()) {
					return clipManager.getText().toString();
				}
			} catch (Exception e) {
				if (Debug_SDK.isUtilLog) {
					Debug_SDK.te(Debug_SDK.mUtilTag, Util_System_ClipboardManager.class, e);
				}
			}
			return null;
		}

		// 如果当前设备的android-sdk 版本号大于等于11
		else {
			try {
				ClipboardManager clipManager = (ClipboardManager) appliactionContext
						.getSystemService(Context.CLIPBOARD_SERVICE);

				if (clipManager.hasPrimaryClip()) {

					// 如果剪切版中的是文字
					if (clipManager.getPrimaryClipDescription().hasMimeType(ClipDescription.MIMETYPE_TEXT_PLAIN)) {
						StringBuilder sb = new StringBuilder();
						ClipData clipData = clipManager.getPrimaryClip();
						for (int i = 0; i < clipData.getItemCount(); ++i) {
							sb.append(clipData.getItemAt(i).getText());

							// ClipData.Item item = clipData.getItemAt(i);
							// CharSequence str = item.coerceToText(MainActivity.this);
							// resultString += str;
						}
						if (sb != null) {
							return sb.toString();
						}
					}
				}
			} catch (Exception e) {
				if (Debug_SDK.isUtilLog) {
					Debug_SDK.te(Debug_SDK.mUtilTag, Util_System_ClipboardManager.class, e);
				}
			}
			return null;
		}

	}

}
