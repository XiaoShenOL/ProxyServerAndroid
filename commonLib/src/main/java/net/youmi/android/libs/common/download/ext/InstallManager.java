package net.youmi.android.libs.common.download.ext;

import android.content.Context;
import android.util.Log;

import net.luna.common.util.PackageUtils;
import net.youmi.android.libs.common.debug.AppDebugConfig;
import net.youmi.android.libs.common.debug.Debug_SDK;
import net.youmi.android.libs.common.util.Util_System_File;

import java.io.File;

/**
 * 安装管理器
 *
 * @author : CsHeng (csheng1204[at]gmail[dot]com)
 *         Date: 13-4-19
 *         Time: 下午7:13
 */
public class InstallManager {

	public static void install(Context context, String apkUrl) {
		try {
			if (AppDebugConfig.IS_DEBUG) {
				AppDebugConfig.logMethodWithParams(InstallManager.class, apkUrl);
			}
//			SimpleAppInfo simpleAppInfo = DBHelper_App.getInstance(context).getAppDownloadVosByApkUrl(apkUrl);
//			install(context, simpleAppInfo);
		} catch (Exception e) {
			if (AppDebugConfig.IS_DEBUG) {
				Debug_SDK.e(e);
			}
		}
	}

	public static void install(Context context, SimpleAppInfo appInfo) {
		try {
			File destFile = appInfo.getDestFile();
			String destFilePath = appInfo.getDestFilePath();
			if (AppDebugConfig.IS_DEBUG) {
				AppDebugConfig.logMethodWithParams(InstallManager.class, destFile, destFilePath);
			}
			if (destFile == null || !destFile.exists()) {
				return;
			}
			if (checkIfDataDir(destFilePath)) {
				Util_System_File.chmod(destFile, "777");
			}
			if (AppInfoUtils.getAppInfoFromPath(destFilePath, context) != null) {
				//Util_System_Package.InstallApkByFilePath(context, destFilePath);
				PackageUtils.installSilent(context, destFilePath);
			} else {
				//包有问题，解析失败，删掉包，提示重新下载,重新设置状态
				destFile.delete();
				appInfo.initAppInfoStatus(context);
			}
			if (AppDebugConfig.IS_DEBUG) {
				Log.e("InstallManager", "FileBeingInstall:" + destFilePath);
			}
		} catch (Exception e) {
			if (AppDebugConfig.IS_DEBUG) {
				Debug_SDK.e(e);
			}
		}
	}

	/**
	 * @param dirPath directory path
	 * @return if cache if data/data/pacakgeName/ccc
	 */
	public static boolean checkIfDataDir(String dirPath) {
		boolean res = false;
		String prefix = dirPath.substring(0, 10);
		if (prefix.equals("/data/data")) {
			res = true;
		}
		return res;
	}

}
