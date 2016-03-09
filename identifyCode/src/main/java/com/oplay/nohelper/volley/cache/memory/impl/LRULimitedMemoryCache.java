package com.oplay.nohelper.volley.cache.memory.impl;


import com.oplay.nohelper.volley.Cache;
import com.oplay.nohelper.volley.cache.memory.LimitedMemoryCache;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Created by qin on 15-3-13.
 */
public class LRULimitedMemoryCache extends LimitedMemoryCache {

	private static final int INITIAL_CAPACITY = 10;
	private static final float LOAD_FACTOR = 1.1f;

	private final Map<String, Cache.Entry> lruCache = Collections.synchronizedMap(new LinkedHashMap<String,
			Cache.Entry>(INITIAL_CAPACITY, LOAD_FACTOR, true));

	public LRULimitedMemoryCache(int maxSize) {
		super(maxSize);
	}

	@Override
	public boolean put(String url, Cache.Entry value) {
		//当前value.length 超过了最大容量,返回false;
		if (super.put(url, value)) {
			lruCache.put(url, value);
			return true;
		} else {
			return false;
		}
	}

	@Override
	public Cache.Entry remove(String url) {
		lruCache.remove(url);
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
		Cache.Entry mostLongUsedValue = null;
		synchronized (lruCache) {
			Iterator<Map.Entry<String, Cache.Entry>> it = lruCache.entrySet().iterator();
			if (it.hasNext()) {
				Map.Entry<String, Cache.Entry> entry = it.next();
				mostLongUsedValue = entry.getValue();
				it.remove();
			}
		}
		return mostLongUsedValue;
	}

	@Override
	public Cache.Entry get(String url) {
		lruCache.get(url);// call "get" for LRU logic
		return super.get(url);
	}

	@Override
	protected Reference<Cache.Entry> createReference(Cache.Entry value) {
		return new WeakReference<Cache.Entry>(value);
	}
}
