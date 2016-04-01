package net.youmi.android.libs.common.download.ext;

import android.content.Context;
import android.graphics.drawable.Drawable;

import net.youmi.android.libs.common.basic.Basic_JSONUtil;
import net.youmi.android.libs.common.debug.AppDebugConfig;
import net.youmi.android.libs.common.debug.Debug_SDK;

import org.json.JSONObject;

public class AppDownloadTaskVo extends SimpleAppInfo<AppDownloadTaskVo> implements Cloneable {

	//you should mark the attribute you don't want to serialize as transient
	protected transient Drawable mApkIconDrawable;

	/**
	 * 是否可升级
	 */
	protected boolean mIsUpdatable = false;

	public AppDownloadTaskVo() {
	}

	public AppDownloadTaskVo(int appId, String appName, String destFilePath, String rawUrl, String iconUrl,
	                         String packageName, int versionCode, String versionName, String serverFileMd5,
	                         long apkFileSize, String apkSize, int percentage, Drawable apkIconDrawable,
	                         boolean isUpdatable) {
		mAppId = appId;
		mAppName = appName;
		mDestFilePath = destFilePath;
//        this.apkPath = apkPath;
//        this.opkPath = opkPath;
		mOwkUrl = rawUrl;
		mAppIcon = iconUrl;
		mPackageName = packageName;
		mVersionCode = versionCode;
		mVersionName = versionName;
		mApkMd5 = serverFileMd5;
		mApkFileSizeLong = apkFileSize;
		mApkSizeStr = apkSize;
		mCompleteProgress = percentage;
		mApkIconDrawable = apkIconDrawable;
		mIsUpdatable = isUpdatable;
	}

	@Override
	public AppDownloadTaskVo newInstanceConstructor(JSONObject jsonObject, Context context) {
		try {
			if (jsonObject == null) {
				return null;
			}
			mIsDownload = Basic_JSONUtil.getBoolean(jsonObject, "download", true);
			mDownloadLabel = Basic_JSONUtil.getString(jsonObject, "buttonLabel", "");
			mAppId = Basic_JSONUtil.getInt(jsonObject, "appId", 0);
			mAppName = Basic_JSONUtil.getString(jsonObject, "name", "");
			mPackageName = Basic_JSONUtil.getString(jsonObject, "packageName", "");
			mVersionCode = Basic_JSONUtil.getInt(jsonObject, "versionCode", 0);
			mVersionName = Basic_JSONUtil.getString(jsonObject, "versionName", "");
			mOwkUrl = Basic_JSONUtil.getString(jsonObject, "owk", "");
			mApkMd5 = Basic_JSONUtil.getString(jsonObject, "apkMd5", "");
			mApkSizeStr = Basic_JSONUtil.getString(jsonObject, "apkSize", "");
			mApkFileSizeLong = Basic_JSONUtil.getLong(jsonObject, "apkFileSize", 0);
			mAppIcon = Basic_JSONUtil.getString(jsonObject, "icon", "");
			initAppInfoStatus(context);
		} catch (Throwable e) {
			if (AppDebugConfig.IS_DEBUG) {
				Debug_SDK.e(e);
			}
		}
		return this;
	}

	public Drawable getApkIconDrawable() {
		return mApkIconDrawable;
	}

	public void setApkIconDrawable(Drawable apkIconDrawable) {
		mApkIconDrawable = apkIconDrawable;
	}

	@Override
	protected Object clone() throws CloneNotSupportedException {
		super.clone();
		return new AppDownloadTaskVo(mAppId, mAppName, mDestFilePath, mOwkUrl, mAppIcon, mPackageName, mVersionCode,
				mVersionName, mApkMd5, mApkFileSizeLong, mApkSizeStr, mCompleteProgress, mApkIconDrawable,
				mIsUpdatable);
	}

	public boolean isUpdatable() {
		return mIsUpdatable;
	}

	public void setUpdatable(boolean updatable) {
		mIsUpdatable = updatable;
	}
}

