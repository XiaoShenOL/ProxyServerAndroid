package com.oplay.nohelper.volley.cache.memory.impl;


import com.oplay.nohelper.volley.Cache;
import com.oplay.nohelper.volley.cache.memory.MemoryCache;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by qin on 15-3-13.
 */
public class LimitedAgeMemoryCache implements MemoryCache {

	private final MemoryCache cache;

	private final long maxAge;
	private final Map<String, Long> loadingDates = Collections.synchronizedMap(new HashMap<String, Long>());

	public LimitedAgeMemoryCache(MemoryCache cache, long maxAge) {
		this.cache = cache;
		this.maxAge = maxAge * 1000;
	}

	@Override
	public boolean put(String url, Cache.Entry value) {
		boolean putSuccessfully = cache.put(url, value);
		if (putSuccessfully) {
			loadingDates.put(url, System.currentTimeMillis());
		}
		return putSuccessfully;
	}

	@Override
	public Cache.Entry get(String url) {
		Long loadingDate = loadingDates.get(url);
		if (loadingDate != null && System.currentTimeMillis() - loadingDate > maxAge) {
			cache.remove(url);
			loadingDates.remove(url);
		}
		return cache.get(url);
	}

	@Override
	public Cache.Entry remove(String url) {
		loadingDates.remove(url);
		return cache.remove(url);
	}

	@Override
	public Collection<String> keys() {
		return cache.keys();
	}

	@Override
	public void clear() {
		cache.clear();
		loadingDates.clear();
	}
}
