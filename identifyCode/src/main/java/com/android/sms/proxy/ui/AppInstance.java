package com.android.sms.proxy.ui;

import android.app.Application;

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

	private static final long SD_LIMIT_SIZE = 10 * 1024 * 1024;
	private long cacheMaxSize = 50 * 1024 * 1024;  //文件缓存大小.
	private long maxFileCacheAge = 60 * 60 * 1000; //文件缓存时间 one hour

	public static final String NETWORK_CACHE_DIR = "volley";
	private DiskCache diskCache;

	@Override
	public void onCreate() {
		super.onCreate();
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
}
