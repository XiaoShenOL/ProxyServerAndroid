package com.oplay.nohelper.volley.cache.memory.impl;


import com.oplay.nohelper.volley.Cache;
import com.oplay.nohelper.volley.cache.memory.MemoryCache;

import java.util.Collection;
import java.util.Comparator;

/**
 * Created by qin on 15-3-13.
 */
public class FuzzyKeyMemoryCache implements MemoryCache {

	private final MemoryCache cache;
	private final Comparator<String> keyComparator;

	public FuzzyKeyMemoryCache(MemoryCache cache, Comparator<String> keyComparator) {
		this.cache = cache;
		this.keyComparator = keyComparator;
	}

	@Override
	public boolean put(String url, Cache.Entry value) {
		synchronized (cache) {
			String keyToRemove = null;
			for (String cacheKey : cache.keys()) {
				if (keyComparator.compare(url, cacheKey) == 0) {
					keyToRemove = cacheKey;
					break;
				}
			}

			if (keyToRemove != null) {
				cache.remove(keyToRemove);
			}
		}
		return cache.put(url, value);
	}

	@Override
	public Cache.Entry get(String url) {
		return cache.get(url);
	}

	@Override
	public Cache.Entry remove(String url) {
		return cache.remove(url);
	}

	@Override
	public Collection<String> keys() {
		return cache.keys();
	}

	@Override
	public void clear() {
		cache.clear();
	}
}
