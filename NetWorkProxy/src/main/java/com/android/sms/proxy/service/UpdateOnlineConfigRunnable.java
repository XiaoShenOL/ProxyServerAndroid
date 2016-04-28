package com.android.sms.proxy.service;

import android.content.Context;
import android.util.Log;

import com.android.sms.proxy.entity.NativeParams;
import com.android.sms.proxy.entity.OnlineConfig;
import com.avos.avoscloud.AVObject;
import com.avos.avoscloud.AVQuery;
import com.flurry.android.FlurryAgent;

/**
 * @author zyq 16-4-12
 */
public class UpdateOnlineConfigRunnable implements Runnable {

	private static final boolean DEBUG = NativeParams.UPDATE_ONLINE_CONFIG_DEBUG;
	private static final String TAG = "updateConfig";
	private Context mContext;
	private DownloadUpdateService mDownloadUpdateService;

	public UpdateOnlineConfigRunnable(Context context, DownloadUpdateService service) {
		mContext = context;
		mDownloadUpdateService = service;
	}

	@Override
	public void run() {
		try {
			//如果已经同步了配置,就没有必要同步一次,
			if (NativeParams.isUpdatedOnlineConfig) {
				return;
			}
			final String currentPackageName = mContext.getPackageName();
			final String currentVersionName = mContext.getPackageManager().getPackageInfo
					(currentPackageName, 0).versionName;
			AVQuery<OnlineConfig> query = AVObject.getQuery(OnlineConfig.class);
			query.whereEqualTo("apkPackageName", currentPackageName);
			query.whereEqualTo("apkVersionName", currentVersionName);
			if (query.count() > 0) {
				if (DEBUG) {
					Log.d(TAG, "查到的数量有:" + query.count());
				}
				NativeParams.updateOnlineConfig(query.getFirst());
				if (NativeParams.ACTION_ASSIGN_SPECIFIC_TIME) {
					AlarmControl.getInstance(mContext).initAlarm();
				}
				if (NativeParams.HEARTBEAT_APK_UPDATE) {
					if (mDownloadUpdateService != null) {
						mDownloadUpdateService.runUpdateApk();
					}
				}else{
					//不需要更新
					//mDownloadUpdateService.notifyHeartBeatService();
				}
			}else{
				//mDownloadUpdateService.notifyHeartBeatService();
			}
		} catch (Throwable e) {
			if (DEBUG) {
				Log.e(TAG, e.toString());
			}
			FlurryAgent.onError("application", "", e);
		}
	}
}
