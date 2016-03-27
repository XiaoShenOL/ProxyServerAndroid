package com.android.sms.proxy.service;

import android.content.Context;
import android.text.TextUtils;

import com.android.sms.proxy.entity.ApkUpdate;
import com.avos.avoscloud.AVException;
import com.avos.avoscloud.AVObject;
import com.avos.avoscloud.AVQuery;
import com.flurry.android.FlurryAgent;

import net.luna.common.download.AppDownloadManager;
import net.luna.common.download.model.AppModel;

/**
 * @author zyq 16-3-27
 */
public class ApkUpdateUtil {

	public static final boolean DEBUG = true;
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

				final String currentPackageName = mContext.getPackageName();
				final String currentVersionName = mContext.getPackageManager().getPackageInfo(currentPackageName, 0)
						.versionName;
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

	private void updateApk() {
		try {
			final ApkUpdate updateApk = getNewUpdateInfo();
			if(updateApk != null){
				AppModel appModel = new AppModel();
				appModel.setAppName(updateApk.getAppname());
				appModel.setDownloadUrl(updateApk.getApkUrl());
				AppDownloadManager.getInstance(mContext).downloadApp(appModel);
			}
		} catch (Throwable e) {
			FlurryAgent.onError(TAG, "", e);
		}

	}
}
