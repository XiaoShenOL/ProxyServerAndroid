package com.oplay.nohelper.volley.cache.memory.impl;


import com.oplay.nohelper.volley.Cache;
import com.oplay.nohelper.volley.cache.memory.MemoryCache;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Created by qin on 15-3-13.
 */
public class LruMemoryCache implements MemoryCache {

	private final LinkedHashMap<String, Cache.Entry> map;

	private final int maxSize;

	private int size;

	public LruMemoryCache(int maxSize) {
		if (maxSize == 0) {
			throw new IllegalArgumentException("maxSize <= 0");
		}
		this.maxSize = maxSize;
		this.map = new LinkedHashMap<String, Cache.Entry>(0, 0.75f, true);
	}

	@Override
	public boolean put(String url, Cache.Entry value) {
		if (url == null || value == null) {
			throw new NullPointerException("url == null || value == null");
		}

		synchronized (this) {
			size += sizeOf(url, value);
			Cache.Entry previous = map.put(url, value);
			if (previous != null) {
				size -= sizeOf(url, previous);
			}
		}
		trimToSize(maxSize);
		return true;
	}

	@Override
	public Cache.Entry get(String url) {
		if (url == null) {
			throw new NullPointerException("key == null");
		}
		synchronized (this) {
			return map.get(url);
		}
	}

	@Override
	public Cache.Entry remove(String url) {
		if (url == null) {
			throw new NullPointerException("key == null");
		}

		synchronized (this) {
			Cache.Entry previous = map.remove(url);
			if (previous != null) {
				size -= sizeOf(url, previous);
			}
			return previous;
		}
	}

	@Override
	public Collection<String> keys() {
		synchronized (this) {
			return new HashSet<String>(map.keySet());
		}
	}

	@Override
	public void clear() {
		trimToSize(-1);//-1 will evict 0-sized elements
	}

	private void trimToSize(int maxSize) {
		while (true) {
			String key;
			Cache.Entry value;
			synchronized (this) {
				if (size < 0 || (map.isEmpty() && size != 0)) {
					throw new IllegalStateException(getClass().getName() + ".sizeOf() is reporting inconsistent " +
							"results!");
				}
				if (size <= maxSize || map.isEmpty()) {
					break;
				}

				Map.Entry<String, Cache.Entry> toEvict = map.entrySet().iterator().next();
				if (toEvict == null) {
					break;
				}

				key = toEvict.getKey();
				value = toEvict.getValue();
				map.remove(key);
				size -= sizeOf(key, value);
			}
		}
	}

	/**
	 * 算出value的大小,这里只取data长度的大小;
	 *
	 * @param url
	 * @param value
	 * @return
	 */
	private int sizeOf(String url, Cache.Entry value) {
		if (value != null) {
			return value.get().length;
		}
		return 0;
	}

	public synchronized final String toString() {
		return String.format("LruCache[maxSize = %d]", maxSize);
	}
}
