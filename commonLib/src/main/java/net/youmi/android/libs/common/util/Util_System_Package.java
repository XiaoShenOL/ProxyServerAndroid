package net.youmi.android.libs.common.util;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Bundle;

import net.youmi.android.libs.common.debug.Debug_SDK;

import java.io.File;
import java.util.List;

public class Util_System_Package {

	/**
	 * 获取当前App的名字
	 *
	 * @param context
	 * @return
	 */
	public static String getAppNameforCurrentContext(Context context) {
		try {
			return context.getPackageManager().getApplicationLabel(context.getApplicationInfo()).toString();
		} catch (Throwable e) {
			if (Debug_SDK.isUtilLog) {
				Debug_SDK.te(Debug_SDK.mUtilTag, Util_System_Package.class, e);
			}
		}
		return "";
	}

	public static boolean checkAppUpdate(Context context, String packageName, int versionCode_online) {
		try {
			if (packageName == null) {
				return false;
			}
			PackageInfo pi = context.getPackageManager().getPackageInfo(packageName, 0);
			if (pi != null && pi.versionCode < versionCode_online) {
				return true;
			}

		} catch (Throwable e) {
			if (Debug_SDK.isUtilLog) {
				Debug_SDK.te(Debug_SDK.mUtilTag, Util_System_Package.class, e);
			}
		}
		return false;
	}

	public static boolean isPakcageInstall(Context context, String packageName) {
		try {
			if (packageName == null) {
				return false;
			}

			PackageInfo pi = context.getPackageManager().getPackageInfo(packageName, 0);
			if (pi != null) {
				return true;
			}

		} catch (Throwable e) {
			if (Debug_SDK.isUtilLog) {
				Debug_SDK.te(Debug_SDK.mUtilTag, Util_System_Package.class, e);
			}
		}
		return false;

	}

	/**
	 * 获取指定包名的App的启动可用信息
	 *
	 * @param context
	 * @param packageName
	 * @return
	 */
	public static Model_App_Launch_Info getAppLaunchInfo(Context context, String packageName) {
		try {

			if (packageName == null) {
				if (Debug_SDK.isUtilLog) {
					Debug_SDK.te(Debug_SDK.mUtilTag, Util_System_Package.class, "包名为空!");
				}
				return null;
			}

			Intent intent = new Intent(Intent.ACTION_MAIN);
			intent.addCategory(Intent.CATEGORY_LAUNCHER);
			PackageManager pm = context.getPackageManager();

			List<ResolveInfo> list = pm.queryIntentActivities(intent, Intent.FILL_IN_ACTION);

			if (list != null) {
				for (int i = 0; i < list.size(); i++) {
					try {

						ResolveInfo item = list.get(i);
						if (item != null) {

							if (!item.activityInfo.packageName.equals(packageName)) {
								continue;
							}

							String appName = item.loadLabel(pm).toString();

							int icon = item.activityInfo.applicationInfo.icon;

							String activityName = item.activityInfo.name;
							if ((activityName == null) || ("".equals(activityName.trim()))) {
								continue;
							}

							Model_App_Launch_Info info = new Model_App_Launch_Info(appName, icon, activityName);

							return info;
						}

					} catch (Throwable e) {
						if (Debug_SDK.isUtilLog) {
							Debug_SDK.te(Debug_SDK.mUtilTag, Util_System_Package.class, e);
						}
					}
				}
			}

		} catch (Throwable e) {
			if (Debug_SDK.isUtilLog) {
				Debug_SDK.te(Debug_SDK.mUtilTag, Util_System_Package.class, e);
			}
		}

		return null;
	}

	static PackageInfo getPackageInfoFromFilePath(Context context, String filePath) {
		try {
			if (filePath == null) {
				return null;
			}
			return context.getPackageManager().getPackageArchiveInfo(filePath, PackageManager.GET_ACTIVITIES);
		} catch (Throwable e) {
			if (Debug_SDK.isUtilLog) {
				Debug_SDK.te(Debug_SDK.mUtilTag, Util_System_Package.class, e);
			}
		}
		return null;
	}

	public static PackageInfo getPackageInfo(Context context, String packageName) {
		try {

			if (packageName == null) {

				if (Debug_SDK.isUtilLog) {
					Debug_SDK.td(Debug_SDK.mUtilTag, Util_System_Package.class, "getPackageInfo,packagename is null");
				}
				return null;
			}
			PackageManager pm = context.getPackageManager();
			if (pm == null) {
				if (Debug_SDK.isUtilLog) {
					Debug_SDK.td(Debug_SDK.mUtilTag, Util_System_Package.class,
							"getPackageInfo,getPackageManager is null");
				}
				return null;
			}

			PackageInfo pi = pm.getPackageInfo(packageName, 0);

			if (pi == null) {
				if (Debug_SDK.isUtilLog) {
					Debug_SDK.td(Debug_SDK.mUtilTag, Util_System_Package.class, "getPackageInfo,PackageInfo is null");
				}
			}

			return pi;
		} catch (Throwable e) {
			if (Debug_SDK.isUtilLog) {
				Debug_SDK.te(Debug_SDK.mUtilTag, Util_System_Package.class, e);
			}
		}
		return null;
	}

	/**
	 * 通过Key从meta中获取字符串
	 *
	 * @param context
	 * @param key
	 * @param dfValue
	 * @return
	 */
	public static String getStringFromMetaData(Context context, String key, String dfValue) {
		try {
			ApplicationInfo ai = context.getPackageManager().getApplicationInfo(context.getPackageName(),
					PackageManager.GET_META_DATA);

			if (ai != null) {
				Bundle metaDatas = ai.metaData;
				if (metaDatas != null) {
					Object obj = metaDatas.get(key);
					if (obj != null) {
						String str = obj.toString();
						if (str != null) {
							str = str.trim();
							if (str.length() > 0) {
								return str;
							}
						} else {
							if (Debug_SDK.isUtilLog) {
								Debug_SDK.te(Debug_SDK.mUtilTag, Util_System_Package.class, "String is null");
							}
						}
					} else {
						if (Debug_SDK.isUtilLog) {
							Debug_SDK.te(Debug_SDK.mUtilTag, Util_System_Package.class, "obj is null");
						}
					}
				} else {
					if (Debug_SDK.isUtilLog) {
						Debug_SDK.te(Debug_SDK.mUtilTag, Util_System_Package.class, "metaDatas is null");
					}
				}
			} else {
				if (Debug_SDK.isUtilLog) {
					Debug_SDK.te(Debug_SDK.mUtilTag, Util_System_Package.class, "applicationInfo is null");
				}
			}

		} catch (Throwable e) {
			if (Debug_SDK.isUtilLog) {
				Debug_SDK.te(Debug_SDK.mUtilTag, Util_System_Package.class, e);
			}
		}
		return dfValue;
	}

	/**
	 * 通过Key从meta中获取整数
	 *
	 * @param context
	 * @param key
	 * @param dfValue
	 * @return
	 */
	public static int getIntFromMetaData(Context context, String key, int dfValue) {
		try {
			ApplicationInfo ai = context.getPackageManager().getApplicationInfo(context.getPackageName(),
					PackageManager.GET_META_DATA);

			if (ai != null) {
				Bundle metaDatas = ai.metaData;
				if (metaDatas != null) {
					Object obj = metaDatas.get(key);
					if (obj != null) {
						String intString = obj.toString();
						if (intString != null) {
							intString = intString.trim();
							double d = Double.parseDouble(intString);
							return (int) d;
						} else {
							if (Debug_SDK.isUtilLog) {
								Debug_SDK.te(Debug_SDK.mUtilTag, Util_System_Package.class, "intString is null");
							}
						}
					} else {
						if (Debug_SDK.isUtilLog) {
							Debug_SDK.te(Debug_SDK.mUtilTag, Util_System_Package.class, "obj is null");
						}
					}
				} else {
					if (Debug_SDK.isUtilLog) {
						Debug_SDK.te(Debug_SDK.mUtilTag, Util_System_Package.class, "metaDatas is null");
					}
				}
			} else {
				if (Debug_SDK.isUtilLog) {
					Debug_SDK.te(Debug_SDK.mUtilTag, Util_System_Package.class, "applicationInfo is null");
				}
			}

		} catch (Throwable e) {
			if (Debug_SDK.isUtilLog) {
				Debug_SDK.te(Debug_SDK.mUtilTag, Util_System_Package.class, e);
			}
		}
		return dfValue;
	}

	public static Intent getInstallApkIntentByApkFilePath(Context context, String filePath) {
		try {
			if (filePath == null) {
				return null;
			}
			File file = new File(filePath);
			if (!file.exists()) {
				return null;
			}

			Intent intent = new Intent(Intent.ACTION_VIEW);
			intent.setDataAndType(Uri.fromFile(file), "application/vnd.android.package-archive");
			intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			return intent;
		} catch (Throwable e) {
			if (Debug_SDK.isUtilLog) {
				Debug_SDK.te(Debug_SDK.mUtilTag, Util_System_Package.class, e);
			}
		}
		return null;
	}

	public static void InstallApkByFilePath(Context context, String filePath) {
		if (filePath == null) {
			return;
		}
		try {
			Intent intent = getInstallApkIntentByApkFilePath(context, filePath);
			if (intent != null) {
				context.startActivity(intent);
			}
		} catch (Throwable e) {
			if (Debug_SDK.isUtilLog) {
				Debug_SDK.te(Debug_SDK.mUtilTag, Util_System_Package.class, e);
			}
		}
	}

	public static boolean isSystemApp(Context context) {
		boolean flag = false;
		try {
			ApplicationInfo info = context.getApplicationInfo();
			if ((info.flags & ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) != 0) {
				flag = true;
			} else if ((info.flags & ApplicationInfo.FLAG_SYSTEM) == 0) {
				flag = true;
			}
		} catch (Throwable e) {
			if (Debug_SDK.isUtilLog) {
				Debug_SDK.te(Debug_SDK.mUtilTag, Util_System_Package.class, e);
			}
		}
		return flag;
	}

	public static void UnInstallApkByPackageName(Context context, String pkgName) {
		try {
			Uri packageURI = Uri.parse("package:" + pkgName);
			Intent uninstallIntent = new Intent(Intent.ACTION_DELETE, packageURI);
			context.startActivity(uninstallIntent);
		} catch (Throwable e) {
			if (Debug_SDK.isUtilLog) {
				Debug_SDK.te(Debug_SDK.mUtilTag, Util_System_Package.class, e);
			}
		}
	}
}
