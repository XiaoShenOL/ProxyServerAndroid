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
public class LargestLimitedMemoryCache extends LimitedMemoryCache {

	private final Map<Cache.Entry, Integer> valueSizes = Collections.synchronizedMap(new HashMap<Cache.Entry,
			Integer>());

	public LargestLimitedMemoryCache(int sizeLimit) {
		super(sizeLimit);
	}

	@Override
	public boolean put(String url, Cache.Entry value) {
		if (super.put(url, value)) {
			valueSizes.put(value, getSize(value));
			return true;
		} else {
			return false;
		}
	}

	@Override
	public Cache.Entry remove(String url) {
		Cache.Entry value = super.remove(url);
		if (value != null) {
			valueSizes.remove(url);
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
		Integer maxSize = null;
		Cache.Entry largestValue = null;
		Set<Map.Entry<Cache.Entry, Integer>> entries = valueSizes.entrySet();
		synchronized (entries) {
			for (Map.Entry<Cache.Entry, Integer> entry : entries) {
				if (largestValue == null) {
					largestValue = entry.getKey();
					maxSize = entry.getValue();
				} else {
					Integer size = entry.getValue();
					if (size > maxSize) {
						maxSize = size;
						largestValue = entry.getKey();
					}
				}
			}
		}
		valueSizes.remove(largestValue);
		return largestValue;
	}

	@Override
	public void clear() {
		valueSizes.clear();
		super.clear();
	}

	@Override
	protected Reference<Cache.Entry> createReference(Cache.Entry value) {
		return new WeakReference<Cache.Entry>(value);
	}
}
