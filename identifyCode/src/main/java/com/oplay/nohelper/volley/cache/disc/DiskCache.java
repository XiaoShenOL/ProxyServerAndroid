package com.oplay.nohelper.volley.cache.disc;

import com.oplay.nohelper.utils.Util_IO;
import com.oplay.nohelper.volley.Cache;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by qin on 15-3-13.
 */
public interface DiskCache {

	File getDirectory();

	File get(String url);

	boolean save(String url, Cache.Entry entry) throws IOException;

	boolean save(String url, InputStream inputStream, Util_IO.CopyListener listener) throws IOException;

	boolean remove(String url);

	void clear();

	void close();

	String getFileName(String url);

}
