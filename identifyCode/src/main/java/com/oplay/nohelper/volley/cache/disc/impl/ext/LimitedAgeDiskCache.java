package com.oplay.nohelper.volley.cache.disc.impl.ext;

import com.oplay.nohelper.utils.Util_IO;
import com.oplay.nohelper.volley.Cache;
import com.oplay.nohelper.volley.cache.disc.DiskCache;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by qin on 15-3-13.
 */
public class LimitedAgeDiskCache implements DiskCache {

	private final DiskCache cache;

	private final long maxAge;
	private final Map<File, Long> loadingDates = Collections.synchronizedMap(new HashMap<File, Long>());

	public LimitedAgeDiskCache(DiskCache cache, long maxAge) {
		this.cache = cache;
		this.maxAge = maxAge;
	}

	@Override
	public File getDirectory() {
		return cache.getDirectory();
	}

	@Override
	public File get(String url) {
		File file = cache.get(url);
		if (file != null && file.exists()) {
			boolean cached;
			Long loadingDate = loadingDates.get(file);
			if (loadingDate == null) {
				cached = false;
				loadingDate = file.lastModified();
			} else {
				cached = true;
			}

			if (System.currentTimeMillis() - loadingDate > maxAge) {
				cache.remove(url);
				loadingDates.remove(file);
			} else if (!cached) {
				loadingDates.put(file, loadingDate);
			}
			return file.exists() ? file : null;
		}
		return null;
	}

	@Override
	public boolean save(String url, Cache.Entry entry) throws IOException {
		boolean successfully = cache.save(url, entry);
		rememberUsage(url);
		return successfully;
	}

	@Override
	public boolean save(String url, InputStream inputStream, Util_IO.CopyListener listener) throws IOException {
		boolean succcessfully = cache.save(url, inputStream, listener);
		rememberUsage(url);
		return succcessfully;
	}

	@Override
	public boolean remove(String url) {
		boolean isContain = false;
		File file = get(url);
		if (file != null && loadingDates.containsKey(file)) {
			isContain = true;
		}
		boolean successfully = cache.remove(url);
		if (successfully && isContain) {
			loadingDates.remove(file);
		}
		return successfully;
	}

	@Override
	public void clear() {
		loadingDates.clear();
		cache.clear();
	}

	@Override
	public void close() {
		cache.close();
	}

	@Override
	public String getFileName(String url) {
		return null;
	}

	private void rememberUsage(String url) {
		File file = get(url);
		if (file != null) {
			long currentTime = System.currentTimeMillis();
			file.setLastModified(currentTime);
			loadingDates.put(file, currentTime);
		}
	}
}
