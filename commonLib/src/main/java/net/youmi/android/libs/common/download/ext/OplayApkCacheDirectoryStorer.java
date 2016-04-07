package net.youmi.android.libs.common.download.ext;

import android.content.Context;
import android.os.Environment;

import net.youmi.android.libs.common.debug.AppDebugConfig;
import net.youmi.android.libs.common.debug.Debug_SDK;
import net.youmi.android.libs.common.download.filestorer.FileCacheDirectoryStorer;
import net.youmi.android.libs.common.util.Util_System_SDCard_Util;

import java.io.File;
import java.io.IOException;

/**
 * 指定两个保存apk文件的目录(优先使用sdcard)
 *
 * @author jen
 */
public class OplayApkCacheDirectoryStorer extends FileCacheDirectoryStorer {

	public static final String DIR_DOWNLOAD = "/s/d";
	private static OplayApkCacheDirectoryStorer mSdcardFileDirInstance;
	private static OplayApkCacheDirectoryStorer mDataFilesDirInstance;

	private OplayApkCacheDirectoryStorer(File directory, long dirLimtMaxSize,
	                                     long perFileLimtMaxTimeMillSecond) throws IOException {
		super(directory, dirLimtMaxSize, perFileLimtMaxTimeMillSecond);
	}

	public static OplayApkCacheDirectoryStorer getInstance(Context context) {
		try {
			/**
			 * deprecated
			 * 检测的步骤是：<br/>
			 *  1、检测SD卡是否可用，且空间充足？ <br/>
			 *  2、使用cache/dataDir，且空间充足？ <br/>
			 *  3、return null，外部应该处理:提示不可用
			 */
			if (Util_System_SDCard_Util.IsSdCardCanWrite(context)) {
				return getSdcardFilesDirInstance();
			}
			return getDataFilesDirInstance(context);
		} catch (Throwable e) {
			if (AppDebugConfig.IS_DEBUG) {
				Debug_SDK.e(e);
			}
		}
		return mSdcardFileDirInstance;
	}

	/**
	 * 获取保存Apk文件的存储卡目录操作类<br/>
	 *
	 * @return
	 */
	private static synchronized OplayApkCacheDirectoryStorer getSdcardFilesDirInstance() {
		try {
			if (mSdcardFileDirInstance == null) {
				final File dir = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + DIR_DOWNLOAD);
				mSdcardFileDirInstance = new OplayApkCacheDirectoryStorer(dir, UN_LIMT_STORE_SIZE, UN_LIMT_STORE_TIME);
			}
			return mSdcardFileDirInstance;
		} catch (Throwable e) {
			if (AppDebugConfig.IS_DEBUG) {
				Debug_SDK.e(e);
			}
		}
		return mDataFilesDirInstance;
	}

	/**
	 * 获取保存Apk文件的内部存储目录操作类<br/>
	 *
	 * @param context
	 * @return
	 */
	private static synchronized OplayApkCacheDirectoryStorer getDataFilesDirInstance(Context context) {

		try {
			if (mDataFilesDirInstance == null) {
			    /* 此处必须使用files/目录下面installer才能访问,其他自定义的东西会不行 */
				// File dir = new File(context.getCacheDir().getAbsolutePath()
				// + "/download");
				final File dir = new File(context.getFilesDir().getAbsolutePath());
				mDataFilesDirInstance = new OplayApkCacheDirectoryStorer(dir, UN_LIMT_STORE_SIZE, UN_LIMT_STORE_TIME);
			}
			return mDataFilesDirInstance;

		} catch (Throwable e) {
			if (AppDebugConfig.IS_DEBUG) {
				Debug_SDK.e(e);
			}
		}
		return mDataFilesDirInstance;

	}

}
