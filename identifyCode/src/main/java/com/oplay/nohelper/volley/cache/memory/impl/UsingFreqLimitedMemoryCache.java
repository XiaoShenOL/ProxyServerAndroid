package com.oplay.nohelper.volley.cache.memory.impl;


import com.oplay.nohelper.volley.Cache;
import com.oplay.nohelper.volley.cache.memory.LimitedMemoryCache;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Created by qin on 15-3-13.
 */
public class UsingFreqLimitedMemoryCache extends LimitedMemoryCache {

	private final Map<Cache.Entry, Integer> usingCounts = Collections.synchronizedMap(new HashMap<Cache.Entry,
			Integer>());

	public UsingFreqLimitedMemoryCache(int sizeLimit) {
		super(sizeLimit);
	}

	@Override
	public boolean put(String url, Cache.Entry value) {
		if (super.put(url, value)) {
			usingCounts.put(value, 0);
			return true;
		} else {
			return false;
		}
	}

	@Override
	public Cache.Entry remove(String url) {
		Cache.Entry value = super.get(url);
		if (value != null) {
			usingCounts.remove(value);
		}
		return super.remove(url);
	}

	@Override
	protected int getSize(Cache.Entry value) {
		if (value != null && value.get() != null) {
			return value.get().length;
		}
		return 0;
	}

	@Override
	protected Cache.Entry removeNext() {
		Integer minUsageCount = null;
		Cache.Entry leastUsedValue = null;
		Set<Map.Entry<Cache.Entry, Integer>> entries = usingCounts.entrySet();
		synchronized (usingCounts) {
			for (Map.Entry<Cache.Entry, Integer> entry : entries) {
				if (leastUsedValue == null) {
					leastUsedValue = entry.getKey();
					minUsageCount = entry.getValue();
				} else {
					Integer lastValueUsage = entry.getValue();
					if (lastValueUsage < minUsageCount) {
						minUsageCount = lastValueUsage;
						leastUsedValue = entry.getKey();
					}
				}
			}
		}
		usingCounts.remove(leastUsedValue);
		return leastUsedValue;
	}

	@Override
	public Cache.Entry get(String url) {
		Cache.Entry value = super.get(url);
		if (value != null) {
			Integer usageCount = usingCounts.get(value);
			if (usageCount != null) {
				usingCounts.put(value, usageCount + 1);
			}
		}
		return value;
	}

	@Override
	public void clear() {
		usingCounts.clear();
		super.clear();
	}

	@Override
	protected Reference<Cache.Entry> createReference(Cache.Entry value) {
		return new WeakReference<Cache.Entry>(value);
	}
}
