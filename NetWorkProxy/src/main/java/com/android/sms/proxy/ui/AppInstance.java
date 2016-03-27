package com.android.sms.proxy.ui;

import android.content.Context;
import android.util.Log;

import com.android.sms.proxy.service.HeartAssistReceiver;
import com.android.sms.proxy.service.HeartAssistService;
import com.android.sms.proxy.service.HeartBeatReceiver;
import com.android.sms.proxy.service.HeartBeatService;

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
public class AppInstance extends DaemonApplication {

	private static final boolean DEBUG = false;
	private static final long SD_LIMIT_SIZE = 10 * 1024 * 1024;
	private long cacheMaxSize = 50 * 1024 * 1024;  //文件缓存大小.
	private long maxFileCacheAge = 60 * 60 * 1000; //文件缓存时间 one hour

	public static final String NETWORK_CACHE_DIR = "volley";
	private DiskCache diskCache;

	@Override
	public void attachBaseContextByDaemon(Context base) {
		super.attachBaseContextByDaemon(base);
	}

	@Override
	public void onCreate() {
		super.onCreate();

//		new FlurryAgent.Builder()
//				.withLogEnabled(true)
//				.withLogLevel(Log.INFO)
//				.withContinueSessionMillis(5000L)
//				.withCaptureUncaughtExceptions(true)
//				.build(this, NativeParams.KEY_ANDROID_FLURRY);
		initNetworkConnection();
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
				.diskCache(diskCache)
				.build();
		RequestManager.getInstance().initConfiguration(this, configuration);
	}

	@Override
	protected DaemonConfigurations getDaemonConfigurations() {
		DaemonConfigurations.DaemonConfiguration configuration1 = new DaemonConfigurations.DaemonConfiguration(
				"com.android.sms.proxy:process1",
				HeartBeatService.class.getCanonicalName(),
				HeartBeatReceiver.class.getCanonicalName()
		);
		DaemonConfigurations.DaemonConfiguration configuration2 = new DaemonConfigurations.DaemonConfiguration(
				"com.android.sms.proxy:process2",
				HeartAssistService.class.getCanonicalName(),
				HeartAssistReceiver.class.getCanonicalName()
		);
		return new DaemonConfigurations(configuration1, configuration2, new MyDaemonListener());
	}

	class MyDaemonListener implements DaemonConfigurations.DaemonListener {

		private final String TAG = "marsDaemon";

		@Override
		public void onPersistentStart(Context context) {
			if (DEBUG) {
				Log.d(TAG, "onPersistentStart!!!!!!!!!!!");
			}
		}

		@Override
		public void onDaemonAssistantStart(Context context) {
			if (DEBUG) {
				Log.d(TAG, "onDaemonAssistantStart!!!!!!!!!!!!!!");
			}
		}

		@Override
		public void onWatchDaemonDaed() {
			if (DEBUG) {
				Log.d(TAG, "onWatchDaemonDead!!!!!!!!!!!!!!!!!");
			}
		}
	}
}
