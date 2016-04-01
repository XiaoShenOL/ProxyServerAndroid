package net.youmi.android.libs.common.basic;

import android.content.Context;

import net.youmi.android.libs.common.debug.Debug_SDK;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.Properties;

public class Basic_Properties {

	public static Properties getPropertiesFromFile(Context context, String storeFileName) {
		Properties p = new Properties();
		InputStream iStream = null;
		try {
			iStream = context.openFileInput(storeFileName);
			if (iStream != null) {
				p.load(iStream);
			}
		} catch (Throwable e) {
			if (Debug_SDK.isBasicLog) {
				Debug_SDK.te(Debug_SDK.mBasicTag, Basic_Properties.class, e);
			}
		} finally {
			try {
				if (iStream != null) {
					iStream.close();
				}
			} catch (Throwable e) {
				if (Debug_SDK.isBasicLog) {
					Debug_SDK.te(Debug_SDK.mBasicTag, Basic_Properties.class, e);
				}
			}
		}
		return p;
	}

	/**
	 * 将properties保存到指定的文件中
	 * 
	 * @param context
	 * @param properties
	 * @param storeFileName
	 * @return
	 */
	public static synchronized boolean savePropertiesToFile(Context context, Properties properties, String storeFileName) {
		try {
			OutputStream os = context.openFileOutput(storeFileName, Context.MODE_PRIVATE);
			if (os != null) {
				properties.store(os, null);

				try {
					os.close();
				} catch (Throwable e) {
					if (Debug_SDK.isBasicLog) {
						Debug_SDK.te(Debug_SDK.mBasicTag, Basic_Properties.class, e);
					}
				}
				return true;
			}
		} catch (Throwable e) {
			if (Debug_SDK.isBasicLog) {
				Debug_SDK.te(Debug_SDK.mBasicTag, Basic_Properties.class, e);
			}
		}
		return false;
	}

	public static String getString(Properties p, String key, String defaultValue) {
		try {
			return p.getProperty(key, defaultValue);
		} catch (Throwable e) {
			if (Debug_SDK.isBasicLog) {
				Debug_SDK.te(Debug_SDK.mBasicTag, Basic_Properties.class, e);
			}
		}
		return defaultValue;
	}

	public static int getInt(Properties p, String key, int defaultValue) {
		try {
			String str = getString(p, key, null);
			if (str == null) {
				return defaultValue;
			}
			return Integer.parseInt(str);
		} catch (Throwable e) {
			if (Debug_SDK.isBasicLog) {
				Debug_SDK.te(Debug_SDK.mBasicTag, Basic_Properties.class, e);
			}
		}
		return defaultValue;
	}

	public static long getLong(Properties p, String key, long defaultValue) {
		try {
			String str = getString(p, key, null);
			if (str == null) {
				return defaultValue;
			}
			return Long.parseLong(str);
		} catch (Throwable e) {
			if (Debug_SDK.isBasicLog) {
				Debug_SDK.te(Debug_SDK.mBasicTag, Basic_Properties.class, e);
			}
		}
		return defaultValue;
	}
}
