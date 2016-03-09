package com.oplay.nohelper.volley.ext;

import android.content.Context;

import com.oplay.nohelper.volley.VolleyLog;
import com.oplay.nohelper.volley.cache.disc.DiskCache;
import com.oplay.nohelper.volley.cache.disc.naming.FileNameGenerator;
import com.oplay.nohelper.volley.cache.memory.MemoryCache;

/**
 * volley的配置信息类
 * Created by qin on 15-3-13.
 */
public final class VolleyConfiguration {

	public final MemoryCache memoryCache;
	public final DiskCache diskCache;
	public final FileNameGenerator fileNameGenerator;

	public VolleyConfiguration(final Builder builder) {
		memoryCache = builder.memoryCache;
		diskCache = builder.diskCache;
		fileNameGenerator = builder.diskCacheFileNameGenerator;
	}

	public static VolleyConfiguration createDefault(Context context) {
		return new Builder(context).build();
	}

	public static class Builder {
		private static final String WARNING_OVERLAP_DISK_CACHE_PARAMS = "diskCache()," +
				"diskCacheSize() and diskCacheFIleCount calls overlap each other";
		private static final String WARNING_OVERLAP_DISK_CACHE_NAME_GENERATOR = "diskCache() and " +
				"diskCacheFileNameGenerator() calls overlap each other";
		private static final String WARNING_OVERLAP_MEMORY_CACHE = "memoryCache() and memoryCacheSize() calls " +
				"overlap" +
				" " +
				"each other";

		private Context context;

		private MemoryCache memoryCache;
		private DiskCache diskCache;
		private FileNameGenerator diskCacheFileNameGenerator;


		private int memoryCacheSize = 0;
		private int diskCacheSize = 0;
		private int diskCacheFileCount = 0;

		public Builder(Context context) {
			this.context = context.getApplicationContext();
		}

		public Builder memoryCacheSize(int memoryCacheSize) {
			if (memoryCacheSize <= 0)
				throw new IllegalArgumentException("memoryCacheSize must be a positive number");

			if (memoryCache != null) {
				VolleyLog.wtf("%s", WARNING_OVERLAP_MEMORY_CACHE);
			}
			this.memoryCacheSize = memoryCacheSize;
			return this;
		}

		public Builder memoryCacheSizePercentage(int availableMemoryPercent) {
			if (availableMemoryPercent <= 0 || availableMemoryPercent >= 100) {
				throw new IllegalArgumentException("availableMemoryPercent must be in range (0 < % < 100)");
			}

			if (memoryCache != null) {
				VolleyLog.wtf("%s", WARNING_OVERLAP_MEMORY_CACHE);
			}

			long availableMemory = Runtime.getRuntime().maxMemory();
			memoryCacheSize = (int) (availableMemory * (availableMemoryPercent / 100f));
			return this;
		}

		public Builder memoryCache(MemoryCache memoryCache) {
			if (memoryCacheSize != 0) {
				VolleyLog.wtf("%s", WARNING_OVERLAP_MEMORY_CACHE);
			}

			this.memoryCache = memoryCache;
			return this;
		}

		public Builder diskCacheSize(int maxCacheSize) {
			if (maxCacheSize <= 0)
				throw new IllegalArgumentException("maxCacheSize must be a positive number");

			if (diskCache != null) {
				VolleyLog.wtf("%s", WARNING_OVERLAP_DISK_CACHE_PARAMS);
			}

			this.diskCacheSize = maxCacheSize;
			return this;
		}

		public Builder diskCacheFileCount(int maxFileCount) {
			if (maxFileCount <= 0)
				throw new IllegalArgumentException("maxFileCount must be a positive number");

			if (diskCache != null) {
				VolleyLog.wtf("%s", WARNING_OVERLAP_DISK_CACHE_PARAMS);
			}

			this.diskCacheFileCount = maxFileCount;
			return this;
		}

		public Builder diskCacheFileNameGenerator(FileNameGenerator fileNameGenerator) {
			if (diskCache != null) {
				VolleyLog.wtf("%s", WARNING_OVERLAP_DISK_CACHE_NAME_GENERATOR);
			}

			this.diskCacheFileNameGenerator = fileNameGenerator;
			return this;
		}

		public Builder diskCache(DiskCache diskCache) {
			if (diskCacheSize > 0 || diskCacheFileCount > 0) {
				VolleyLog.wtf("%s", WARNING_OVERLAP_DISK_CACHE_PARAMS);
			}
			if (diskCacheFileNameGenerator != null) {
				VolleyLog.wtf("%s", WARNING_OVERLAP_DISK_CACHE_NAME_GENERATOR);
			}

			this.diskCache = diskCache;
			return this;
		}


		private void initEmptyFieldsWithDefaultValues() {

			if (diskCacheFileNameGenerator == null) {
				diskCacheFileNameGenerator = DefaultConfigurationFactory.createFileNameGenerator();
			}
			if (diskCache == null) {
				diskCache = DefaultConfigurationFactory
						.createDiskCache(context, diskCacheFileNameGenerator, diskCacheSize, diskCacheFileCount);
			}

			if (memoryCache == null) {
				memoryCache = DefaultConfigurationFactory.createMemoryCache(context, memoryCacheSize);
			}

		}

		public VolleyConfiguration build() {
			initEmptyFieldsWithDefaultValues();
			return new VolleyConfiguration(this);
		}


	}
}
