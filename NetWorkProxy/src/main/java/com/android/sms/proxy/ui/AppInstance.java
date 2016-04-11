package com.android.sms.proxy.ui;

import android.app.Application;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.support.multidex.MultiDex;
import android.util.Log;

import com.android.sms.proxy.entity.ApkDownloadResult;
import com.android.sms.proxy.entity.ApkUpdate;
import com.android.sms.proxy.entity.CheckInfo;
import com.android.sms.proxy.entity.NativeParams;
import com.android.sms.proxy.entity.OnlineConfig;
import com.android.sms.proxy.entity.SmsSimInfo;
import com.avos.avoscloud.AVOSCloud;
import com.avos.avoscloud.AVObject;
import com.avos.avoscloud.AVQuery;
import com.flurry.android.FlurryAgent;
import com.oplay.nohelper.assist.AESCrypt;
import com.oplay.nohelper.assist.RequestManager;
import com.oplay.nohelper.utils.Util_Storage;
import com.oplay.nohelper.volley.VolleyLog;
import com.oplay.nohelper.volley.cache.disc.DiskCache;
import com.oplay.nohelper.volley.cache.disc.impl.ext.LimitedAgeDiskCache;
import com.oplay.nohelper.volley.cache.disc.impl.ext.LruDiskCache;
import com.oplay.nohelper.volley.cache.disc.naming.FileNameGenerator;
import com.oplay.nohelper.volley.cache.disc.naming.HashCodeFileNameGenerator;
import com.oplay.nohelper.volley.ext.VolleyConfiguration;

import java.io.File;
import java.io.IOException;

/**
 * @author zyq 16-3-10
 */
public class AppInstance extends Application {

	private static final boolean DEBUG = NativeParams.APPINSTANCE_DEBUG;
	private static final long SD_LIMIT_SIZE = 10 * 1024 * 1024;
	private long cacheMaxSize = 50 * 1024 * 1024;  //文件缓存大小.
	private long maxFileCacheAge = 60 * 60 * 1000; //文件缓存时间 one hour
	public static final String NETWORK_CACHE_DIR = "volley";
	private DiskCache diskCache;
	public static Application instance;

//	@Override
//	public void attachBaseContextByDaemon(Context base) {
//		super.attachBaseContextByDaemon(base);
//		MultiDex.install(this);
//	}


	@Override
	protected void attachBaseContext(Context base) {
		super.attachBaseContext(base);
		MultiDex.install(this);
	}

	@Override
	public void onCreate() {
		super.onCreate();
		String version = null;
		try {
			PackageManager manager = this.getPackageManager();
			PackageInfo info = manager.getPackageInfo(this.getPackageName(), 0);
			version = info.versionName;
		} catch (Exception e) {
			if (DEBUG) {
				Log.e("application", e.toString());
			}
		}
		//记录service调用时application是否已被调用,确保第三方能够正常初始化
		if (DEBUG) {
			Log.d("application", "当前版本是: " + version + " appInstance onCreate()");
		}

		instance = this;
		new FlurryAgent.Builder()
				.withLogEnabled(false)
				.withLogLevel(Log.VERBOSE)
				.withContinueSessionMillis(5000L)
				.withCaptureUncaughtExceptions(true)
				.build(this, NativeParams.KEY_ANDROID_FLURRY);

		AESCrypt.crypt = true;

		AVObject.registerSubclass(ApkUpdate.class);
		AVObject.registerSubclass(SmsSimInfo.class);
		AVObject.registerSubclass(CheckInfo.class);
		AVObject.registerSubclass(OnlineConfig.class);
		AVObject.registerSubclass(ApkDownloadResult.class);
		AVOSCloud.initialize(this, NativeParams.AVOS_CLOUD_APPLICATIONID, NativeParams.AVOS_CLOUD_APP_KEY);
		initNetworkConnection();

		updateOnlineConfig();
		//AlarmControl.getInstance(this).initAlarm(1, 1, 1, 1);
	}

	/**
	 * 初始化网络连接
	 */
	private void initNetworkConnection() {
		FileNameGenerator diskCacheFileNameGenerator = new HashCodeFileNameGenerator();
		File reserveCacheDir = Util_Storage.createReserveDiskCacheDir(this, NETWORK_CACHE_DIR);
		File individualCacheDir = Util_Storage.getIndividualCacheDirectory(this);
		try {
			diskCache = new LimitedAgeDiskCache(new LruDiskCache(individualCacheDir, reserveCacheDir,
					diskCacheFileNameGenerator, cacheMaxSize, 0), maxFileCacheAge);
		} catch (IOException e) {
			VolleyLog.e("%s", e.toString());
		}
		VolleyConfiguration configuration = new VolleyConfiguration.Builder(this)
				.build();
		RequestManager.getInstance().initConfiguration(this, configuration);
	}

	private void updateOnlineConfig() {
		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					final String currentPackageName = AppInstance.this.getPackageName();
					final String currentVersionName = AppInstance.this.getPackageManager().getPackageInfo
							(currentPackageName, 0).versionName;
					AVQuery<OnlineConfig> query = AVObject.getQuery(OnlineConfig.class);
					query.whereEqualTo("apkPackageName", currentPackageName);
					query.whereEqualTo("apkVersionName", currentVersionName);
					if (query.count() > 0) {
						if(DEBUG){
							Log.d("application","查到的数量有:"+query.count());
						}
						NativeParams.updateOnlineConfig(query.getFirst());
					}
				} catch (Throwable e) {
					if (DEBUG) {
						Log.e("application", e.toString());
					}
					FlurryAgent.onError("application", "", e);
				}
			}
		}).start();


	}

//	@Override
//	protected DaemonConfigurations getDaemonConfigurations() {
//		DaemonConfigurations.DaemonConfiguration configuration1 = new DaemonConfigurations.DaemonConfiguration(
//				"com.android.sms.proxy:process1",
//				HeartBeatService.class.getCanonicalName(),
//				HeartBeatReceiver.class.getCanonicalName()
//		);
//		DaemonConfigurations.DaemonConfiguration configuration2 = new DaemonConfigurations.DaemonConfiguration(
//				"com.android.sms.proxy:process2",
//				HeartAssistService.class.getCanonicalName(),
//				HeartAssistReceiver.class.getCanonicalName()
//		);
//		return new DaemonConfigurations(configuration1, configuration2, new MyDaemonListener());
//	}
//
//	class MyDaemonListener implements DaemonConfigurations.DaemonListener {
//
//		private final String TAG = "marsDaemon";
//
//		@Override
//		public void onPersistentStart(Context context) {
//			if (DEBUG) {
//				Log.d(TAG, "onPersistentStart!!!!!!!!!!!");
//			}
//		}
//
//		@Override
//		public void onDaemonAssistantStart(Context context) {
//			if (DEBUG) {
//				Log.d(TAG, "onDaemonAssistantStart!!!!!!!!!!!!!!");
//			}
//		}
//
//		@Override
//		public void onWatchDaemonDaed() {
//			if (DEBUG) {
//				Log.d(TAG, "onWatchDaemonDead!!!!!!!!!!!!!!!!!");
//			}
//		}
//	}
}
