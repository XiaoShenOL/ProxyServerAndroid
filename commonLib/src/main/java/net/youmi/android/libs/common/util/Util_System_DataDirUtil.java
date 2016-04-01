package net.youmi.android.libs.common.util;

import android.os.Environment;
import android.os.StatFs;

import net.youmi.android.libs.common.debug.Debug_SDK;

/**
 * App私有存储目录容量相关
 * 
 * @author zhitaocai edit on 2014-7-15
 * 
 */
public class Util_System_DataDirUtil {

	/**
	 * 检查应用内data目录是否还有足够的空间
	 * 
	 * @param limtSpace
	 * @return
	 */
	public static boolean IsDataDirEnoughSpace(long limtSpace) {
		try {

			StatFs statFs = new StatFs(Environment.getDataDirectory().getPath());

			// 获取block的SIZE
			long blocSize = statFs.getBlockSize();

			// 己使用的Block的数量
			long availaBlock = statFs.getAvailableBlocks();

			long availableSize = availaBlock * blocSize;
			if (availableSize < 0) {
				availableSize = Math.abs(availableSize);
			}
			if (Debug_SDK.isUtilLog) {
				Debug_SDK.ti(Debug_SDK.mUtilTag, Util_System_DataDirUtil.class, "data:" + "可用容量:" + availableSize
						+ ",需求容量:" + limtSpace + ",可用块:" + availaBlock + ",每块容量:" + blocSize);
			}

			if (availableSize >= limtSpace) {
				if (Debug_SDK.isUtilLog) {
					Debug_SDK.ti(Debug_SDK.mUtilTag, Util_System_DataDirUtil.class, "data存储空间充足");
				}
				return true;
			} else {
				if (Debug_SDK.isUtilLog) {
					Debug_SDK.ti(Debug_SDK.mUtilTag, Util_System_DataDirUtil.class, "data存储空间不足");
				}
			}

		} catch (Throwable e) {
			if (Debug_SDK.isUtilLog) {
				Debug_SDK.te(Debug_SDK.mUtilTag, Util_System_DataDirUtil.class, e);
			}
		}

		return false;

	}

	public static long getCountSize() {
		try {
			String path = Environment.getDataDirectory().getPath();

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
				Debug_SDK.te(Debug_SDK.mUtilTag, Util_System_DataDirUtil.class, e);
			}
		}

		return 0;
	}

	/**
	 * 获取私有app目录
	 * 
	 * @return /data
	 */
	public static String getDataDirRootPath() {
		try {
			return Environment.getDataDirectory().getPath();
		} catch (Throwable e) {
			if (Debug_SDK.isUtilLog) {
				Debug_SDK.te(Debug_SDK.mUtilTag, Util_System_DataDirUtil.class, e);
			}
		}
		return null;
	}

	/**
	 * 返回当前应用内目录可用空间大小
	 * 
	 * @return
	 */
	public static long getAvailableSize() {
		try {

			String path = Environment.getDataDirectory().getPath();

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

		} catch (Throwable e) {
			if (Debug_SDK.isUtilLog) {
				Debug_SDK.te(Debug_SDK.mUtilTag, Util_System_DataDirUtil.class, e);
			}
		}
		return 0;
	}
}
