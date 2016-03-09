package com.oplay.nohelper.volley.cache.memory;


import com.oplay.nohelper.volley.Cache;

import java.lang.ref.Reference;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

/**
 * Created by qin on 15-3-13.
 */
public abstract class BaseMemoryCache implements MemoryCache {

	/**
	 * Stores not strong references to objects *
	 */
	private final Map<String, Reference<Cache.Entry>> softMap = Collections.synchronizedMap(new HashMap<String,
			Reference<Cache.Entry>>());

	@Override
	public boolean put(String url, Cache.Entry value) {
		softMap.put(url, createReference(value));
		return true;
	}

	@Override
	public Cache.Entry get(String url) {
		Cache.Entry result = null;
		Reference<Cache.Entry> reference = softMap.get(url);
		if (reference != null) {
			result = reference.get();
		}
		return result;
	}

	@Override
	public Cache.Entry remove(String url) {
		Reference<Cache.Entry> entryRef = softMap.remove(url);
		return entryRef == null ? null : entryRef.get();
	}

	@Override
	public Collection<String> keys() {
		synchronized (softMap) {
			return new HashSet<String>(softMap.keySet());
		}
	}

	@Override
	public void clear() {
		softMap.clear();
	}

	/**
	 * Create reference of value *
	 */
	protected abstract Reference<Cache.Entry> createReference(Cache.Entry value);
}
