package com.oplay.nohelper.volley.cache.memory.impl;


import com.oplay.nohelper.volley.Cache;
import com.oplay.nohelper.volley.cache.memory.BaseMemoryCache;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;

/**
 * Created by qin on 15-3-13.
 */
public class WeakMemoryCache extends BaseMemoryCache {

	@Override
	protected Reference<Cache.Entry> createReference(Cache.Entry value) {
		return new WeakReference<Cache.Entry>(value);
	}
}
