package com.oplay.nohelper.volley.cache.memory.impl;


import com.oplay.nohelper.volley.Cache;
import com.oplay.nohelper.volley.cache.memory.LimitedMemoryCache;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by qin on 15-3-13.
 */
public class FIFOLimitedMemoryCache extends LimitedMemoryCache {

	private final List<Cache.Entry> queue = Collections.synchronizedList(new LinkedList<Cache.Entry>());

	public FIFOLimitedMemoryCache(int sizeLimit) {
		super(sizeLimit);
	}

	@Override
	public boolean put(String url, Cache.Entry value) {
		if (super.put(url, value)) {
			queue.add(value);
			return true;
		} else {
			return false;
		}
	}

	@Override
	public Cache.Entry remove(String url) {
		Cache.Entry value = super.get(url);
		if (value != null) {
			queue.remove(value);
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
		return queue.remove(0);
	}

	@Override
	public void clear() {
		queue.clear();
		super.clear();
	}

	@Override
	protected Reference<Cache.Entry> createReference(Cache.Entry value) {
		return new WeakReference<Cache.Entry>(value);
	}
}
