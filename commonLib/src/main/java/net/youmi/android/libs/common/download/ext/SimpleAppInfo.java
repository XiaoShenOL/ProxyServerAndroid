package net.youmi.android.libs.common.download.ext;

import android.content.Context;
import android.text.TextUtils;

import com.google.gson.annotations.SerializedName;

import net.youmi.android.libs.common.debug.AppDebugConfig;
import net.youmi.android.libs.common.debug.Debug_SDK;
import net.youmi.android.libs.common.network.Util_Network_Status;
import net.youmi.android.libs.common.util.Util_System_Intent;

import org.json.JSONObject;

import java.io.File;
import java.io.Serializable;

/**
 * @author: CsHeng (csheng1204[at]gmail[dot]com)
 * Date: 14-3-11
 * Time: 下午4:54
 */
public class SimpleAppInfo<T> implements IConstructable<T>, Serializable {

	public static final int FAKE_INIT_PROGRESS = 1;

	static final long FILE_SIZE_KB = 1024;

	static final long FILE_SIZE_MB = 1024 * 1024;

	@SerializedName("appId")
	protected int mAppId;

	@SerializedName("name")
	protected String mAppName;

	@SerializedName("icon")
	protected String mAppIcon;

	@SerializedName("packageName")
	protected String mPackageName;

	@SerializedName("versionCode")
	protected int mVersionCode;

	@SerializedName("versionName")
	protected String mVersionName;

	@SerializedName("apkSize")
	protected String mApkSizeStr;

	@SerializedName("apkFileSize")
	protected long mApkFileSizeLong;

	//	@SerializedName("apk")
//	protected String mApkUrl;
	@SerializedName("owk")
	protected String mOwkUrl;
	@SerializedName("apkMd5")
	protected String mApkMd5;

	protected DownloadStatus mDownloadStatus;

	protected InstallStatus mInstallStatus;

	protected AppStatus mAppStatus;

	protected File mDestFile;

	protected String mDestFilePath;

	protected int mCompleteProgress;

	/**
	 * 是否启用下载，默认为true，只有在接口为false的时候，才用downloadlabel这个标签
	 */
	@SerializedName("download")
	protected boolean mIsDownload = true;

	@SerializedName("buttonLabel")
	protected String mDownloadLabel;

	transient Context mContext;// not serializable

	public final int getAppId() {
		return mAppId;
	}

	public final void setAppId(int appId) {
		mAppId = appId;
	}

	public final String getAppName() {
		return mAppName;
	}

	public final void setAppName(String appName) {
		mAppName = appName;
	}

	public final String getPackageName() {
		return mPackageName;
	}

	public final void setPackageName(String packageName) {
		mPackageName = packageName;
	}

	public final int getVersionCode() {
		return mVersionCode;
	}

	public final void setVersionCode(int versionCode) {
		mVersionCode = versionCode;
	}

	public final String getVersionName() {
		return mVersionName;
	}

	public final void setVersionName(String versionName) {
		mVersionName = versionName;
	}

	public final String getAppIcon() {
		return mAppIcon;
	}

	public final void setAppIcon(String appIcon) {
		mAppIcon = appIcon;
	}

	public final String getApkSizeStr() {
		return mApkSizeStr;
	}

	public final void setApkSizeStr(String apkSizeStr) {
		mApkSizeStr = apkSizeStr;
	}

	public final long getApkFileSizeLong() {
		return mApkFileSizeLong;
	}

	public final void setApkFileSizeLong(long apkFileSizeLong) {
		mApkFileSizeLong = apkFileSizeLong;
		if (apkFileSizeLong >= FILE_SIZE_MB) {
			mApkSizeStr = String.format("%1$.2fMB", (1.0f) * apkFileSizeLong / FILE_SIZE_MB);
		} else {
			mApkSizeStr = String.format("%1$.2fKB", (1.0f) * apkFileSizeLong / FILE_SIZE_KB);
		}
	}

	public final String getOwkUrl() {
		return mOwkUrl;
	}

	public final void setOwkUrl(String owkUrl) {
		mOwkUrl = owkUrl;
	}

	public final int getCompleteProgress() {
		return mCompleteProgress;
	}

	public final void setCompleteProgress(int completeProgress) {
		mCompleteProgress = completeProgress;
	}

	public final File getDestFile() {
		initFile();
		return mDestFile;
	}

	public final void setDestFile(File destFile) {
		if (destFile != null && !destFile.equals(mDestFile)) {
			mDestFile = destFile;
			mDestFilePath = destFile.getAbsolutePath();
		}
	}

	public final String getDestFilePath() {
		initFile();
		return mDestFilePath;
	}

	public final void setDestFilePath(String destFilePath) {
		if (!TextUtils.isEmpty(destFilePath) && !destFilePath.equalsIgnoreCase(mDestFilePath)) {
			try {
				mDestFilePath = destFilePath;
				mDestFile = new File(destFilePath);
			} catch (Exception e) {
				if (AppDebugConfig.IS_DEBUG) {
					Debug_SDK.e(e);
				}
			}
		}
	}

	public final String getApkMd5() {
		return mApkMd5;
	}

	public final void setApkMd5(String apkMd5) {
		mApkMd5 = apkMd5;
	}

	public final String getDownloadLabel() {
		return mDownloadLabel;
	}

	public final void setDownloadLabel(String downloadLabel) {
		mDownloadLabel = downloadLabel;
	}

	public final boolean isDownload() {
		return mIsDownload;
	}

	public final void setDownload(boolean isDownload) {
		mIsDownload = isDownload;
	}

	public boolean isHasUpdate(SimpleAppInfo installedInfo) {
		try {
			return installedInfo.getVersionCode() < mVersionCode ||
					installedInfo.getVersionName().compareToIgnoreCase(mVersionName) < 0;
		} catch (Exception e) {
			if (AppDebugConfig.IS_DEBUG) {
				Debug_SDK.e(e);
			}
		}
		return false;
	}

	public boolean isFileExists() {
		return mDestFile != null && mDestFile.exists();
	}

	public void setContext(Context context) {
		mContext = context.getApplicationContext();
	}

	public void initFile() {
		try {
			if (mDestFilePath == null || mDestFile == null) {
				mDestFilePath = OplayApkFileNameCreator.getInstance(mContext).getStoreFilePathByRawUrl(mOwkUrl);
				mDestFile = new File(mDestFilePath);
			}
		} catch (Exception e) {
			if (AppDebugConfig.IS_DEBUG) {
				Debug_SDK.e(e);
			}
		}
	}

	public DownloadStatus getDownloadStatus() {
		return mDownloadStatus;
	}

	public void setDownloadStatus(DownloadStatus downloadStatus) {
		mDownloadStatus = downloadStatus;
	}

	public InstallStatus getInstallStatus() {
		return mInstallStatus;
	}

	public void setInstallStatus(InstallStatus installStatus) {
		mInstallStatus = installStatus;
	}

	public AppStatus getAppStatus() {
		return mAppStatus;
	}

	public void setAppStatus(AppStatus appStatus) {
		mAppStatus = appStatus;
	}

	public void initDownloadStatus(Context context) {
		if (isDownload()) {
			setDownloadStatus(OplayDownloadManager.getInstance(context).getAppDownloadStatus(getOwkUrl()));
		} else {
			setDownloadStatus(DownloadStatus.DISABLE);
		}
	}

	public void initInstallStatus(Context context) {
		final AppDownloadTaskVo installedInfo = AppInfoUtils.getInstalledAppInfo(mPackageName, context);
		if (installedInfo != null) {
			if (isHasUpdate(installedInfo)) {
				setInstallStatus(InstallStatus.UPDATABLE);
			} else {
				setInstallStatus(InstallStatus.INSTALLED);
			}
		} else {
			setInstallStatus(null);
		}
	}

	public void initAppInfoStatus(Context context) {
		setContext(context);
		initDownloadStatus(context);
		initInstallStatus(context);
		AppStatus status = getAppStatus(mDownloadStatus, mInstallStatus);
		setAppStatus(status);
	}

	/**
	 * 根据当前下载状态和应用是否已经安装来决定应用状态是什么情况，用于显示文字
	 *
	 * @param ds
	 * @param is
	 * @return
	 */
	private AppStatus getAppStatus(DownloadStatus ds, InstallStatus is) {
		//任务不再下载列表当中
		if (ds == null) {
			if (is == null) {
				return AppStatus.DOWNLOADABLE;
			}
			if (is.equals(InstallStatus.INSTALLED)) {
				return AppStatus.OPENABLE;
			}
			if (is.equals(InstallStatus.UPDATABLE)) {
				return AppStatus.UPDATABLE;
			}
		}
		switch (ds) {
			case DISABLE: {
				if (is != null) {
					return AppStatus.OPENABLE;
				}
				return AppStatus.DISABLE;
			}
			case DOWNLOADING:
			case PENDING:
				return AppStatus.PAUSABLE;
			case PAUSED:
				return AppStatus.RESUMABLE;
			case FAILED:
				return AppStatus.RETRYABLE;
			case FINISHED:
				if (is == null) {
					return AppStatus.INSTALLABLE;
				}
				if (is.equals(InstallStatus.INSTALLED)) {
					return AppStatus.OPENABLE;
				}
				if (is.equals(InstallStatus.UPDATABLE)) {
					return AppStatus.INSTALLABLE;
				}
		}
		return null;
	}

	public void handleOnClick() {
		if (AppDebugConfig.IS_DEBUG) {
			AppDebugConfig.logMethodWithParams(this, mAppStatus);
		}
		if (mAppStatus == null) {
			if (AppDebugConfig.IS_DEBUG) {
				AppDebugConfig.logMethodWithParams(this, getPackageName(), "mAppStatus NULL!!!!!!! ");
			}
			return;
		}
		//switch前需要更新一下状态，否则确实会出现已暂停统计出错问题
		initAppInfoStatus(mContext);
		switch (mAppStatus) {
			case DOWNLOADABLE:
			case UPDATABLE:
				if (Util_Network_Status.getNetworkType(mContext) == Util_Network_Status.TYPE_WIFI) {
					startDownload();
				} else {
//					DialogFragment_Confirm dialogFragment = DialogFragment_Confirm.newInstance(
//							mContext.getString(R.string.dialog_confirm_title),
//							mContext.getString(R.string.dialog_download_notice),
//							mContext.getString(R.string.dialog_confirm_cancel),
//							mContext.getString(R.string.dialog_confirm_confirm));
//					dialogFragment.setListener(new DialogFragment_Confirm.OnDialogClickListener() {
//						@Override
//						public void onPositiveClick() {
//							startDownload();
//						}
//
//						@Override
//						public void onNegativeClick() {
//
//						}
//					});
//					dialogFragment.show(fragmentManager, "download");
				}
				break;
			case INSTALLABLE:
				initFile();
				startInstall();
				break;
			case OPENABLE:
				Util_System_Intent.startActivityByPackageName(mContext, mPackageName);
				break;
			case PAUSABLE:
				stopDownload();
				break;
			case RESUMABLE:
			case RETRYABLE:
				if (Util_Network_Status.getNetworkType(mContext) == Util_Network_Status.TYPE_WIFI) {
					restartDownload();
				} else {
//					final DialogFragment_Confirm dialog = DialogFragment_Confirm.newInstance(
//							mContext.getString(R.string.dialog_confirm_title),
//							mContext.getString(R.string.dialog_download_notice),
//							mContext.getString(R.string.dialog_confirm_cancel),
//							mContext.getString(R.string.dialog_download_confirm)
//					);
//					dialog.setListener(new DialogFragment_Confirm.OnDialogClickListener() {
//						@Override
//						public void onPositiveClick() {
//							startDownload();
//						}
//
//						@Override
//						public void onNegativeClick() {
//
//						}
//					});
//					dialog.show(fragmentManager, "download");
				}
				break;
			case DISABLE:
			default:
				break;
		}
	}

	public void startDownload() {
		try {
			OplayDownloadManager.getInstance(mContext).addDownloadTask(this);
			//Util_Toast.toast(mContext.getString(R.string.toast_download_add_new_task));
		} catch (Exception e) {
			if (AppDebugConfig.IS_DEBUG) {
				Debug_SDK.e(e);
			}
		}
	}

	public void stopDownload() {
		try {
			OplayDownloadManager.getInstance(mContext).stopDownloadTask(this);
		} catch (Exception e) {
			if (AppDebugConfig.IS_DEBUG) {
				Debug_SDK.e(e);
			}
		}
	}

	public void restartDownload() {
		try {
			OplayDownloadManager.getInstance(mContext).restartDownloadTask(this);
		} catch (Exception e) {
			if (AppDebugConfig.IS_DEBUG) {
				Debug_SDK.e(e);
			}
		}
	}

	public void startInstall() {
		try {
			InstallManager.install(mContext, this);
		} catch (Exception e) {
			if (AppDebugConfig.IS_DEBUG) {
				Debug_SDK.e(e);
			}
		}
	}

	@Override
	public T newInstanceConstructor(JSONObject jsonObject, Context context) {
		return null;
	}

	@Override
	public String toString() {
		return "SimpleAppInfo{" +
				"mPackageName='" + mPackageName + '\'' +
				'}';
	}
}
