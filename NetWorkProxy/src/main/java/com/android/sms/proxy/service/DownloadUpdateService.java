package com.android.sms.proxy.service;

import android.app.Notification;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import com.android.sms.proxy.entity.NativeParams;
import com.flurry.android.FlurryAgent;
import com.oplay.nohelper.utils.Util_Service;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * @author zyq 16-4-12
 */
public class DownloadUpdateService extends Service {
	public static final boolean DEBUG = NativeParams.DOWNLOAD_UPDATE_APK_DEBUG;
	public static final String TAG = "downloadUpdateService";

	private ScheduledExecutorService mExecutorService;
	private UpdateOnlineConfigRunnable mUpdateOnlineConfigRunnable;
	private ApkUpdateRunnable mApkUpdateRunnable;
	private String versionName = "";


	@Override
	public void onCreate() {
		super.onCreate();
		if (DEBUG) {
			Log.d(TAG, "onCreate()");
		}
		try {
			versionName = this.getPackageManager().getPackageInfo
					(this.getPackageName(), 0).versionName;
		} catch (Throwable e) {
			if (DEBUG) {
				Log.e(TAG, e.toString());
			}
		}
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		scheduledWithFixedDelay();
		startForeground(NativeParams.SERVICE_NOTIFICATION_ID, new Notification());
		return START_NOT_STICKY;
	}

	public void notifyHeartBeatService() {
		final boolean isHeartBeatServiceRunning = Util_Service.isServiceRunning(this, HeartBeatService.class
				.getCanonicalName());
		if (isHeartBeatServiceRunning) {
			HeartBeatService.getInstance().scheduledWithFixedDelay(NativeParams.HEARTBEAT_PROXY_INTERVAL);
		}
		if (DEBUG) {
			Log.d(TAG, "结束自身!!!!!!!!");
		}
		stopForeground(true);
		stopSelf();
	}

	public void runUpdateApk() {
		if(DEBUG){
			Log.d(TAG,"叫心跳服务开始更新apk");
		}
		final boolean isHeartBeatServiceRunning = Util_Service.isServiceRunning(this, HeartBeatService.class
				.getCanonicalName());
		if (isHeartBeatServiceRunning) {
			HeartBeatService.getInstance().runUpdatedApk();
		}
		if (DEBUG) {
			Log.d(TAG, "结束自身!!!!!!!!");
		}
		stopForeground(true);
		stopSelf();
	}


	public void scheduledWithFixedDelay() {
		try {
			if (mExecutorService == null) {
				mExecutorService = Executors.newScheduledThreadPool(1);
			}
			if (mUpdateOnlineConfigRunnable == null) {
				mUpdateOnlineConfigRunnable = new UpdateOnlineConfigRunnable(this, this);
			}
			mExecutorService.schedule(mUpdateOnlineConfigRunnable, 0, TimeUnit.SECONDS);
		} catch (Throwable e) {
			if (DEBUG) {
				Log.e(TAG, e.toString());
			}
			FlurryAgent.onError(TAG, "", e);
		}
	}

	@Nullable
	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}




	@Override
	public void onDestroy() {
		super.onDestroy();
		//stopForeground(true);
		if (DEBUG) {
			Log.d(TAG, "onDestroy!!!!");
		}
	}
}
