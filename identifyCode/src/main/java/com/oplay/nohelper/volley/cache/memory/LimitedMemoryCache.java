package com.oplay.nohelper.volley.cache.memory;


import com.oplay.nohelper.volley.Cache;
import com.oplay.nohelper.volley.VolleyLog;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by qin on 15-3-13.
 */
public abstract class LimitedMemoryCache extends BaseMemoryCache {

	private static final int MAX_NORMAL_CACHE_SIZE_IN_MB = 16;
	private static final int MAX_NORMAL_CACHE_SIZE = MAX_NORMAL_CACHE_SIZE_IN_MB * 1024 * 1024;

	private final int sizeLimit;
	private final AtomicInteger cacheSize;

	/**
	 * Contains strong references to stored objects . Each next object is added last. If hard cache size will exceed
	 * limit thrn
	 * first object is deleted (but it continue exist at softMap) andr can be collected by GC at any time
	 */
	private final List<Cache.Entry> hardCache = Collections.synchronizedList(new LinkedList<Cache.Entry>());

	public LimitedMemoryCache(int sizeLimit) {
		this.sizeLimit = sizeLimit;
		cacheSize = new AtomicInteger();
		if (sizeLimit > MAX_NORMAL_CACHE_SIZE) {
			VolleyLog.wtf("You set too larget memory cache size (more than %1$d Mb)", MAX_NORMAL_CACHE_SIZE_IN_MB);
		}
	}

	@Override
	public boolean put(String url, Cache.Entry value) {
		boolean putSuccessfully = false;
		//Try to add value to hard cache
		int valueSize = getSize(value);
		int sizeLimit = getSizeLimit();
		int curCacheSize = cacheSize.get();

		if (valueSize < sizeLimit) {
			while (curCacheSize + valueSize > sizeLimit) {
				Cache.Entry removedValue = removeNext();
				if (hardCache.remove(removedValue)) {
					curCacheSize = cacheSize.addAndGet(-getSize(removedValue));
				}
			}
			hardCache.add(value);
			cacheSize.addAndGet(valueSize);

			putSuccessfully = true;
		}
		super.put(url, value);
		return putSuccessfully;
	}

	@Override
	public Cache.Entry remove(String url) {
		Cache.Entry value = super.get(url);
		if (value != null) {
			if (hardCache.remove(value)) {
				cacheSize.addAndGet(-getSize(value));
			}
		}
		return super.remove(url);
	}

	protected int getSizeLimit() {
		return sizeLimit;
	}

	protected abstract int getSize(Cache.Entry value);

	protected abstract Cache.Entry removeNext();
}
