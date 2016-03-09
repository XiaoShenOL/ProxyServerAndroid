/*******************************************************************************
 * Copyright 2014 Sergey Tarasevich
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
package com.oplay.nohelper.volley.cache.disc.impl.ext;

import android.graphics.Bitmap;


import com.oplay.nohelper.utils.Util_IO;
import com.oplay.nohelper.volley.Cache;
import com.oplay.nohelper.volley.VolleyLog;
import com.oplay.nohelper.volley.cache.disc.DiskCache;
import com.oplay.nohelper.volley.cache.disc.impl.ext.DiskLruCache.Snapshot;
import com.oplay.nohelper.volley.cache.disc.naming.FileNameGenerator;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * @since 1.9.2
 */
public class LruDiskCache implements DiskCache {
	/**
	 * {@value
	 */
	public static final int DEFAULT_BUFFER_SIZE = 32 * 1024; // 32 Kb
	/**
	 * {@value
	 */
	public static final Bitmap.CompressFormat DEFAULT_COMPRESS_FORMAT = Bitmap.CompressFormat.PNG;
	/**
	 * {@value
	 */
	public static final int DEFAULT_COMPRESS_QUALITY = 100;
	private static final String ERROR_ARG_NULL = " argument must be not null";
	private static final String ERROR_ARG_NEGATIVE = " argument must be positive number";
	protected final FileNameGenerator fileNameGenerator;
	protected int bufferSize = DEFAULT_BUFFER_SIZE;
	protected DiskLruCache cache;
	private File reserveCacheDir;


	/**
	 * @param cacheDir          Directory for file caching
	 * @param fileNameGenerator {@linkplain
	 *                          Name generator} for cached files. Generated names must match the regex
	 *                          <strong>[a-z0-9_-]{1,64}</strong>
	 * @param cacheMaxSize      Max cache size in bytes. <b>0</b> means cache size is unlimited.
	 * @throws IOException if cache can't be initialized (e.g. "No space left on device")
	 */
	public LruDiskCache(File cacheDir, FileNameGenerator fileNameGenerator, long cacheMaxSize) throws IOException {
		this(cacheDir, null, fileNameGenerator, cacheMaxSize, 0);
	}

	/**
	 * @param cacheDir          Directory for file caching
	 * @param reserveCacheDir   null-ok; Reserve directory for file caching. It's used when the primary directory
	 *                          isn't available.
	 * @param fileNameGenerator {@linkplain com.youmi.
	 *                          Name generator} for cached files. Generated names must match the regex
	 *                          <strong>[a-z0-9_-]{1,64}</strong>
	 * @param cacheMaxSize      Max cache size in bytes. <b>0</b> means cache size is unlimited.
	 * @param cacheMaxFileCount Max file count in cache. <b>0</b> means file count is unlimited.
	 * @throws IOException if cache can't be initialized (e.g. "No space left on device")
	 */
	public LruDiskCache(File cacheDir, File reserveCacheDir, FileNameGenerator fileNameGenerator, long cacheMaxSize,
	                    int cacheMaxFileCount) throws IOException {
		if (cacheDir == null) {
			throw new IllegalArgumentException("cacheDir" + ERROR_ARG_NULL);
		}
		if (cacheMaxSize < 0) {
			throw new IllegalArgumentException("cacheMaxSize" + ERROR_ARG_NEGATIVE);
		}
		if (cacheMaxFileCount < 0) {
			throw new IllegalArgumentException("cacheMaxFileCount" + ERROR_ARG_NEGATIVE);
		}
		if (fileNameGenerator == null) {
			throw new IllegalArgumentException("fileNameGenerator" + ERROR_ARG_NULL);
		}

		if (cacheMaxSize == 0) {
			cacheMaxSize = Long.MAX_VALUE;
		}
		if (cacheMaxFileCount == 0) {
			cacheMaxFileCount = Integer.MAX_VALUE;
		}

		this.reserveCacheDir = reserveCacheDir;
		this.fileNameGenerator = fileNameGenerator;
		initCache(cacheDir, reserveCacheDir, cacheMaxSize, cacheMaxFileCount);
	}

	private void initCache(File cacheDir, File reserveCacheDir, long cacheMaxSize, int cacheMaxFileCount)
			throws IOException {
		try {
			cache = DiskLruCache.open(cacheDir, 1, 1, cacheMaxSize, cacheMaxFileCount);
		} catch (IOException e) {
			VolleyLog.e("%s", e.toString());
			if (reserveCacheDir != null) {
				initCache(reserveCacheDir, null, cacheMaxSize, cacheMaxFileCount);
			}
			if (cache == null) {
				throw e; //new RuntimeException("Can't initialize disk cache", e);
			}
		}
	}

	@Override
	public File getDirectory() {
		return cache.getDirectory();
	}

	@Override
	public File get(String url) {
		Snapshot snapshot = null;
		try {
			snapshot = cache.get(getKey(url));
			return snapshot == null ? null : snapshot.getFile(0);
		} catch (IOException e) {
			VolleyLog.e("%s", e.toString());
			return null;
		} finally {
			if (snapshot != null) {
				snapshot.close();
			}
		}


	}

	/**
	 * 把Cache.Entry 写进文件中,用于保存,查看是否保存成功
	 *
	 * @param url
	 * @param value
	 * @return
	 * @throws IOException
	 */
	@Override
	public boolean save(String url, Cache.Entry value) throws IOException {
		DiskLruCache.Editor editor = cache.edit(getKey(url));
		if (editor == null) {
			return false;
		}

		OutputStream os = new BufferedOutputStream(editor.newOutputStream(0), bufferSize);
		boolean savedSuccessfully = false;
		try {
			if (value.writeMagic(os)) {
				try {
					os.write(value.data);
					savedSuccessfully = true;
				} catch (IOException e) {
					savedSuccessfully = false;
					throw e;
				}
			} else {
				savedSuccessfully = false;
			}

		} finally {
			Util_IO.closeSilently(os);
		}
		if (savedSuccessfully) {
			editor.commit();
		} else {
			editor.abort();
		}
		return savedSuccessfully;
	}

	/**
	 * 暂时不用
	 *
	 * @param url
	 * @param imageStream
	 * @param listener
	 * @return
	 * @throws IOException
	 */
	@Override
	public boolean save(String url, InputStream imageStream, Util_IO.CopyListener listener) throws IOException {
		DiskLruCache.Editor editor = cache.edit(getKey(url));
		if (editor == null) {
			return false;
		}
		OutputStream os = new BufferedOutputStream(editor.newOutputStream(0), bufferSize);
		boolean copied = false;
		try {
			copied = Util_IO.copyStream(imageStream, os, listener, bufferSize);
		} finally {
			Util_IO.closeSilently(os);
			if (copied) {
				editor.commit();
			} else {
				editor.abort();
			}
		}
		return copied;
	}

	@Override
	public boolean remove(String url) {
		try {
			return cache.remove(getKey(url));
		} catch (IOException e) {
			VolleyLog.e("%s", e.toString());
			return false;
		}
	}

	@Override
	public void clear() {
		try {
			cache.delete();
		} catch (IOException e) {
			VolleyLog.e("%s", e.toString());
		}
		try {
			initCache(cache.getDirectory(), reserveCacheDir, cache.getMaxSize(), cache.getMaxFileCount());
		} catch (IOException e) {
			VolleyLog.e("%s", e.toString());
		}
	}

	@Override
	public void close() {
		try {
			cache.close();
		} catch (IOException e) {
			VolleyLog.e("%s", e.toString());
		}
		cache = null;
	}

	@Override
	public String getFileName(String url) {
		return fileNameGenerator.generate(url);
	}

	private String getKey(String url) {
		return fileNameGenerator.generate(url);
	}

	/**
	 * 设置缓存区的大小
	 *
	 * @param bufferSize
	 */
	public void setBufferSize(int bufferSize) {
		this.bufferSize = bufferSize;
	}
}
