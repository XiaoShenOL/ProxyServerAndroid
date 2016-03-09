package com.oplay.nohelper.volley.cache.memory;


import com.oplay.nohelper.volley.Cache;

import java.util.Collection;

/**
 * Created by qin on 15-3-13.
 */
public interface MemoryCache {

	boolean put(String url, Cache.Entry value);

	Cache.Entry get(String url);

	Cache.Entry remove(String url);

	Collection<String> keys();

	void clear();

}
