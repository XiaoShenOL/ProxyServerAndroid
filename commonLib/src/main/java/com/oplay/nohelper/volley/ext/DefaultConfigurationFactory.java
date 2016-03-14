package com.oplay.nohelper.volley.ext;

import android.annotation.TargetApi;
import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.os.Build;

import com.oplay.nohelper.volley.VolleyLog;
import com.oplay.nohelper.volley.cache.disc.DiskCache;
import com.oplay.nohelper.volley.cache.disc.impl.UnlimitedDiskCache;
import com.oplay.nohelper.volley.cache.disc.impl.ext.LimitedAgeDiskCache;
import com.oplay.nohelper.volley.cache.disc.impl.ext.LruDiskCache;
import com.oplay.nohelper.volley.cache.disc.naming.FileNameGenerator;
import com.oplay.nohelper.volley.cache.disc.naming.HashCodeFileNameGenerator;
import com.oplay.nohelper.volley.cache.memory.MemoryCache;
import com.oplay.nohelper.volley.cache.memory.impl.LruMemoryCache;

import java.io.File;
import java.io.IOException;

/**
 *
 * Created by qin on 15-3-12.
 */
public class DefaultConfigurationFactory {

	/**
	 *
	 */
	public static FileNameGenerator createFileNameGenerator() {
		return new HashCodeFileNameGenerator();
	}

	/**
	 * Creates default implementation of {@link com.nostra13.universalimageloader.cache.disc.DiskCache} depends on
	 * incoming parameters
	 */
	public static DiskCache createDiskCache(Context context, FileNameGenerator diskCacheFileNameGenerator,
	                                        long diskCacheSize, int diskCacheFileCount) {
		File reserveCacheDir = createReserveDiskCacheDir(context);
		if (diskCacheSize > 0 || diskCacheFileCount > 0) {
			File individualCacheDir = StorageUtils.getIndividualCacheDirectory(context);
			try {
				return new LimitedAgeDiskCache(new LruDiskCache(individualCacheDir, reserveCacheDir,
						diskCacheFileNameGenerator, diskCacheSize,
						diskCacheFileCount), 20 * 1000);
			} catch (IOException e) {
				VolleyLog.e("%s", e.toString());
			}
		}
		File cacheDir = StorageUtils.getCacheDirectory(context);
		return new UnlimitedDiskCache(cacheDir, reserveCacheDir, diskCacheFileNameGenerator);
	}

	/**
	 * Creates reserve disk cache folder which will be used if primary disk cache folder becomes unavailable
	 */
	private static File createReserveDiskCacheDir(Context context) {
		File cacheDir = StorageUtils.getCacheDirectory(context, false);
		File individualDir = new File(cacheDir, "volly");
		if (individualDir.exists() || individualDir.mkdir()) {
			cacheDir = individualDir;
		}
		return cacheDir;
	}

	public static MemoryCache createMemoryCache(Context context, int memoryCacheSize) {
		if (memoryCacheSize == 0) {
			ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
			int memoryClass = am.getMemoryClass();
			if (hasHoneycomb() && isLargeHeap(context)) {
				memoryClass = getLargeMemoryClass(am);
			}
			memoryCacheSize = 1024 * 1024 * memoryClass / 8;
		}
		return new LruMemoryCache(memoryCacheSize);
	}

	private static boolean hasHoneycomb() {
		return Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB;
	}

	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	private static boolean isLargeHeap(Context context) {
		return (context.getApplicationInfo().flags & ApplicationInfo.FLAG_LARGE_HEAP) != 0;
	}

	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	private static int getLargeMemoryClass(ActivityManager am) {
		return am.getLargeMemoryClass();
	}
}
