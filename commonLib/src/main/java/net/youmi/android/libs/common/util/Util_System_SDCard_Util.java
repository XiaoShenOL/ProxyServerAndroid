package net.youmi.android.libs.common.util;

import android.content.Context;
import android.os.Environment;
import android.os.StatFs;

import net.youmi.android.libs.common.debug.Debug_SDK;

public class Util_System_SDCard_Util {

	/**
	 * 判断sd卡是否可读
	 * 
	 * @return
	 */
	public static boolean IsSdCardCanRead() {
		String state = Environment.getExternalStorageState();
		if (state.equals(Environment.MEDIA_MOUNTED) || state.equals(Environment.MEDIA_MOUNTED_READ_ONLY)) {
			// 待验证
			return true;
		}
		return false;
	}

	/**
	 * 判断sd卡是否可写
	 * 
	 * @return
	 */
	public static boolean IsSdCardCanWrite(Context context) {

		try {

			String state = Environment.getExternalStorageState();
			if (state.equals(Environment.MEDIA_MOUNTED)) {
				if (Util_System_Permission.isWith_WRITE_EXTERNAL_STORAGE_Permission(context)) {
					// 具有写权限
					return true;
				}
			}
		} catch (Throwable e) {
			if (Debug_SDK.isUtilLog) {
				Debug_SDK.te(Debug_SDK.mUtilTag, Util_System_SDCard_Util.class, e);
			}
		}
		return false;
	}

	public static boolean IsSdCardCanWrite_And_EnoughSpace(Context context, long limtSpace) {
		try {

			if (IsSdCardCanWrite(context)) {
				if (Debug_SDK.isUtilLog) {
					Debug_SDK.ti(Debug_SDK.mUtilTag, Util_System_SDCard_Util.class, "sdcard 可写");
				}

				String path = Environment.getExternalStorageDirectory().getPath();

				StatFs statFs = new StatFs(path);

				// 获取block的SIZE
				long blocSize = statFs.getBlockSize();

				// 己使用的Block的数量
				long availaBlock = statFs.getAvailableBlocks();

				long availableSize = availaBlock * blocSize;
				if (availableSize < 0) {
					availableSize = Math.abs(availableSize);
				}

				if (Debug_SDK.isUtilLog) {
					Debug_SDK.ti(Debug_SDK.mUtilTag, Util_System_SDCard_Util.class, "sdcard:" + path + ",可用容量:"
							+ availableSize + ",需求容量:" + limtSpace + ",可用块:" + availaBlock + ",每块容量:" + blocSize);
				}

				if (availableSize >= limtSpace) {
					if (Debug_SDK.isUtilLog) {
						Debug_SDK.ti(Debug_SDK.mUtilTag, Util_System_SDCard_Util.class, "sdcard容量充足");
					}
					return true;
				} else {
					if (Debug_SDK.isUtilLog) {
						Debug_SDK.te(Debug_SDK.mUtilTag, Util_System_SDCard_Util.class, "sdcard容量不足");
					}
				}
			} else {
				if (Debug_SDK.isUtilLog) {
					Debug_SDK.te(Debug_SDK.mUtilTag, Util_System_SDCard_Util.class, "sdcard不可写");
				}
			}
		} catch (Throwable e) {
			if (Debug_SDK.isUtilLog) {
				Debug_SDK.te(Debug_SDK.mUtilTag, Util_System_SDCard_Util.class, e);
			}

		}
		return false;
	}

	public static long getCountSize() {
		try {
			String path = Environment.getExternalStorageDirectory().getPath();

			StatFs statFs = new StatFs(path);

			// 获取block的SIZE
			long blocSize = statFs.getBlockSize();

			// 己使用的Block的数量
			long countBlock = statFs.getBlockCount();

			long availableSize = countBlock * blocSize;
			if (availableSize < 0) {
				availableSize = Math.abs(availableSize);
			}

			return availableSize;
		} catch (Throwable e) {
			if (Debug_SDK.isUtilLog) {
				Debug_SDK.te(Debug_SDK.mUtilTag, Util_System_SDCard_Util.class, e);
			}

		}

		return 0;
	}

	/**
	 * 获取sdcard根目录
	 * 
	 * @return
	 */
	public static String getSdcardRootPath() {
		try {
			return Environment.getExternalStorageDirectory().getPath();
		} catch (Throwable e) {
			if (Debug_SDK.isUtilLog) {
				Debug_SDK.te(Debug_SDK.mUtilTag, Util_System_SDCard_Util.class, e);
			}
		}
		return null;
	}

	public static long getAvailableSize(Context context) {
		try {

			if (IsSdCardCanWrite(context)) {

				if (Debug_SDK.isUtilLog) {
					Debug_SDK.tw(Debug_SDK.mUtilTag, Util_System_SDCard_Util.class, "sdcard可写");
				}

				String path = Environment.getExternalStorageDirectory().getPath();

				StatFs statFs = new StatFs(path);

				// 获取block的SIZE
				long blocSize = statFs.getBlockSize();

				// 己使用的Block的数量
				long availaBlock = statFs.getAvailableBlocks();

				long availableSize = availaBlock * blocSize;
				if (availableSize < 0) {
					availableSize = Math.abs(availableSize);
				}

				return availableSize;

			} else {
				if (Debug_SDK.isUtilLog) {
					Debug_SDK.tw(Debug_SDK.mUtilTag, Util_System_SDCard_Util.class, "sdcard不可写");
				}
			}
		} catch (Throwable e) {
			if (Debug_SDK.isUtilLog) {
				Debug_SDK.te(Debug_SDK.mUtilTag, Util_System_SDCard_Util.class, e);
			}
		}
		return 0;
	}

}
