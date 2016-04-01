package net.youmi.android.libs.common.download.ext;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;

import net.youmi.android.libs.common.debug.AppDebugConfig;
import net.youmi.android.libs.common.debug.Debug_SDK;

import java.io.File;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;


public class AppInfoUtils {
	private static final long SIZE_KB = 1024;
	private final static long SIZE_MB = 1024 * SIZE_KB;
	private final static long SIZE_GB = 1024 * SIZE_MB;

	/**
	 * Get apk file resources: include name, pkgName, icon, mVersionName and mVersionCode
	 *
	 * @param storeFilePath
	 * @param context
	 * @return {@link AppDownloadTaskVo} or null if PackageInfo cannot resolve
	 */
	public static AppDownloadTaskVo getAppInfoFromPath(String storeFilePath, Context context) {

		try {
			final PackageManager pm = context.getPackageManager();
			final PackageInfo pkgInfo = pm.getPackageArchiveInfo(storeFilePath, PackageManager.GET_ACTIVITIES);
			AppDownloadTaskVo appInfoVo = null;
			if (pkgInfo != null) {
				appInfoVo = new AppDownloadTaskVo();
				ApplicationInfo applicationInfo = pkgInfo.applicationInfo;
				/* 必须加这两句，不然下面icon获取是default icon而不是应用包的icon */
				applicationInfo.sourceDir = storeFilePath;
				applicationInfo.publicSourceDir = storeFilePath;
				appInfoVo.setAppName(pm.getApplicationLabel(applicationInfo).toString());
				appInfoVo.setPackageName(applicationInfo.packageName);
				appInfoVo.setVersionName(pkgInfo.versionName);
				appInfoVo.setVersionCode(pkgInfo.versionCode);
				appInfoVo.setApkIconDrawable(pm.getApplicationIcon(applicationInfo));
				appInfoVo.setDestFilePath(storeFilePath);
			}
			return appInfoVo;
		} catch (Throwable e) {
			if (AppDebugConfig.IS_DEBUG) {
				Debug_SDK.e(e);
			}
		}
		return null;
	}

	/**
	 * Retrieve appinfo from installed packages
	 *
	 * @param packageName
	 * @param context
	 * @return null if mPackageName not installed, otherwise return {@link AppDownloadTaskVo}
	 */
	public static AppDownloadTaskVo getInstalledAppInfo(String packageName, Context context) {
		if (TextUtils.isEmpty(packageName)) {
			return null;
		}
		final PackageManager pm = context.getPackageManager();
		PackageInfo pkgInfo = null;
		AppDownloadTaskVo appInfoVo = null;
		try {
			pkgInfo = pm.getPackageInfo(packageName, PackageManager.GET_ACTIVITIES);
			if (pkgInfo != null) {
				appInfoVo = new AppDownloadTaskVo();
				final ApplicationInfo applicationInfo = pkgInfo.applicationInfo;
				appInfoVo.setAppName(pm.getApplicationLabel(applicationInfo).toString());
				appInfoVo.setPackageName(applicationInfo.packageName);
				appInfoVo.setVersionName(pkgInfo.versionName);
				appInfoVo.setVersionCode(pkgInfo.versionCode);
				appInfoVo.setApkIconDrawable(pm.getApplicationIcon(applicationInfo));
			}
		} catch (PackageManager.NameNotFoundException e) {
//            if (AppDebugConfig.IS_DEBUG) {
//                Log.v(AppInfoUtils.class.getSimpleName(),
//                        String.format("NameNotFoundException:NotInstalled:[%s]", mPackageName));
//            }
		}
		return appInfoVo;
	}

	public static Drawable getInstallAppIconDrawable(String packageName, Context context) {
		if (TextUtils.isEmpty(packageName) || context == null) return null;
		try {
			final PackageManager pm = context.getPackageManager();
			final PackageInfo pkgInfo = pm.getPackageInfo(packageName, PackageManager.GET_ACTIVITIES);
			if (pkgInfo != null) {
				final ApplicationInfo applicationInfo = pkgInfo.applicationInfo;
				return pm.getApplicationIcon(applicationInfo);
			}
		} catch (Throwable e) {
			if (AppDebugConfig.IS_DEBUG) {
				Debug_SDK.e(e);
			}
		}
		return null;
	}

	/**
	 * Get apk file icon drawable
	 *
	 * @param apkPath
	 * @param context
	 * @return default app icon (Green android robot) or Icon Drawable, null if apk file cannot resolve
	 */
	public static Drawable getIconFromFile(String apkPath, Context context) {
		if (TextUtils.isEmpty(apkPath)) {
			return null;
		}
		final PackageManager pm = context.getPackageManager();
		final PackageInfo pkgInfo = pm.getPackageArchiveInfo(apkPath, PackageManager.GET_ACTIVITIES);
		Drawable icon = null;
		if (pkgInfo != null) {
			ApplicationInfo applicationInfo = pkgInfo.applicationInfo;
			/* 必须加这两句，不然下面icon获取是default icon而不是应用包的icon */
			applicationInfo.sourceDir = apkPath;
			applicationInfo.publicSourceDir = apkPath;
			icon = pm.getApplicationIcon(applicationInfo);
		}
		return icon;
	}

	/**
	 * Get installed app list (Exclude system apps)
	 *
	 * @param context
	 * @return List of {@link AppDownloadTaskVo}
	 */
	public static List<AppDownloadTaskVo> getInstalledAppInfoVosExcludeSystemApps(Context context) {
		final List<AppDownloadTaskVo> appList = new ArrayList<AppDownloadTaskVo>();
		final PackageManager pm = context.getPackageManager();
		final List<PackageInfo> packages = pm.getInstalledPackages(0);
		for (PackageInfo info : packages) {
			final ApplicationInfo applicationInfo = info.applicationInfo;
			// Only display the non-system app info
			if ((applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) == 0) {
				AppDownloadTaskVo tmpInfo = new AppDownloadTaskVo();
				tmpInfo.setAppName(applicationInfo.loadLabel(pm).toString());
				tmpInfo.setPackageName(info.packageName);
				tmpInfo.setVersionName(info.versionName);
				tmpInfo.setVersionCode(info.versionCode);
				tmpInfo.setApkIconDrawable(applicationInfo.loadIcon(pm));
				appList.add(tmpInfo);
			}
		}
		return appList;
	}

	public static List<String> getInstalledAppsExcludeSystemApps(Context context) {
		final List<String> appList = new ArrayList<String>();
		final PackageManager pm = context.getPackageManager();
		final List<PackageInfo> packages = pm.getInstalledPackages(0);
		for (PackageInfo info : packages) {
			final ApplicationInfo applicationInfo = info.applicationInfo;
			// Only display the non-system app info
			if ((applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) == 0) {
				appList.add(info.packageName);
			}
		}
		return appList;
	}

	/**
	 * delete package file
	 *
	 * @param filePath
	 * @return
	 */
	public static boolean deletePackage(String filePath) {
		final File file = new File(filePath);
		boolean res = false;
		if (file.exists()) {
			res = file.delete();
		}
		return res;
	}

	/**
	 * 将long型大小转换成Size大小
	 */
	public static String fromLong2String(long size) {
		DecimalFormat df = new DecimalFormat("#.00");
		double dSize;
		if (size < SIZE_KB) {
			return size + "B";
		} else if (size < SIZE_MB) {
			dSize = size * 1.0 / SIZE_KB;
			return df.format(dSize) + "KB";
		} else if (size < SIZE_GB) {
			dSize = size * 1.0 / SIZE_MB;
			return df.format(dSize) + "MB";
		} else {
			dSize = size * 1.0 / SIZE_GB;
			return df.format(dSize) + "GB";
		}
	}
}
