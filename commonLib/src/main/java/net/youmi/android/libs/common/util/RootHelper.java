package net.youmi.android.libs.common.util;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

import net.youmi.android.libs.common.debug.Debug_SDK;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

public class RootHelper {

	// 执行linux命令并且输出结果
	protected static List<String> execRootCmd(String cmd) {
		List<String> vector = new ArrayList<String>();
		Process process = null;
		OutputStream os = null;
		DataOutputStream dos = null;
		InputStream is = null;
		DataInputStream dis = null;
		try {
			process = Runtime.getRuntime().exec("su ");// 经过Root处理的android系统即有su命令
			os = process.getOutputStream();
			dos = new DataOutputStream(os);
			is = process.getInputStream();
			dis = new DataInputStream(is);
			dos.writeBytes(cmd + "\n");
			dos.flush();
			String str3 = dis.readLine();
			vector.add(str3);
			dos.writeBytes("exit\n");
			dos.flush();
			process.waitFor();
			return vector;
		} catch (Exception localException) {
			localException.printStackTrace();
		} finally {
			try {
				if (dos != null) {
					dos.close();
				}
			} catch (Exception e) {
				if (Debug_SDK.isUtilLog) {
					Debug_SDK.te(Debug_SDK.mUtilTag, RootHelper.class, e);
				}
			}
			try {
				if (process != null) {
					process.destroy();
				}
			} catch (Exception e) {
				if (Debug_SDK.isUtilLog) {
					Debug_SDK.te(Debug_SDK.mUtilTag, RootHelper.class, e);
				}
			}
			try {
				if (os != null) {
					os.close();
				}
			} catch (Throwable e) {
				if (Debug_SDK.isUtilLog) {
					Debug_SDK.te(Debug_SDK.mUtilTag, RootHelper.class, e);
				}
			}
		}
		return null;
	}

	// 执行linux命令但不关注结果输出
	public static int execRootCmdSilent(String cmd) {
		Process process = null;
		DataOutputStream dos = null;
		try {
			process = Runtime.getRuntime().exec("su");
			Object oj = process.getOutputStream();
			dos = new DataOutputStream((OutputStream) oj);
			dos.writeBytes(cmd + "\n");
			dos.flush();
			dos.writeBytes("exit\n");
			dos.flush();
			process.waitFor();
			oj = process.exitValue();
			return 0;
		} catch (Exception e) {
			if (Debug_SDK.isUtilLog) {
				Debug_SDK.te(Debug_SDK.mUtilTag, RootHelper.class, e);
			}

		} finally {
			try {
				if (dos != null) {
					dos.close();
				}
			} catch (Exception e) {
				if (Debug_SDK.isUtilLog) {
					Debug_SDK.te(Debug_SDK.mUtilTag, RootHelper.class, e);
				}
			}
			try {
				if (process != null) {
					process.destroy();
				}
			} catch (Exception e) {
				if (Debug_SDK.isUtilLog) {
					Debug_SDK.te(Debug_SDK.mUtilTag, RootHelper.class, e);
				}

			}
		}
		return -1;
	}

	// 判断机器Android是否已经root，即是否获取root权限
	public static boolean haveRoot() {
		int i = execRootCmdSilent("echo test"); // 通过执行测试命令来检测
		return i != -1;
	}

	static public int installApk(Context context, String apkpath) {
		// 方法1
		/*
		 * String cmd = "cp "+apkpath+" "+"/data/app"; execRootCmdSilent(cmd);
		 */
		// 方法2
		String cmd = "pm install -r " + apkpath;
		execRootCmdSilent(cmd);
		return 0;
	}

	static public int uninstallApk(Context context, String packageName) {
		try {

			PackageManager aPackageManager = context.getPackageManager();
			// List aList = aPackageManager.queryIntentActivities(
			// aIntent, PackageManager.PERMISSION_GRANTED);
			List<PackageInfo> aList = aPackageManager.getInstalledPackages(0);
			ApplicationInfo ai = null;
			for (PackageInfo rInfo : aList) {
				if (rInfo.packageName.equals(packageName)) {
					ai = rInfo.applicationInfo;
				}
			}
			if (ai != null) {
				execRootCmd("pm uninstall " + ai.packageName);
			}
		} catch (Throwable e) {
        	if (Debug_SDK.isUtilLog) {
        		Debug_SDK.te(Debug_SDK.mUtilTag, RootHelper.class, e);
        	}

		}
		return 0;
	}
}
