package com.oplay.nohelper.volley.cache.disc.impl;


import com.oplay.nohelper.utils.Util_IO;
import com.oplay.nohelper.volley.Cache;
import com.oplay.nohelper.volley.cache.disc.DiskCache;
import com.oplay.nohelper.volley.cache.disc.naming.FileNameGenerator;
import com.oplay.nohelper.volley.ext.DefaultConfigurationFactory;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by qin on 15-3-13.
 */
public abstract class BaseDiskCache implements DiskCache {

	public static final int DEFAULT_BUFFER_SIZE = 32 * 1024;
	private static final String ERROR_ARG_NULL = " argument must be not null";
	private static final String TEMP_IMAGE_POSTFIX = ".tmp";
	protected final File cacheDir;
	protected final File reserveCacheDir;
	protected final FileNameGenerator fileNameGenerator;
	protected int bufferSize = DEFAULT_BUFFER_SIZE;

	public BaseDiskCache(File cacheDir) {
		this(cacheDir, null, DefaultConfigurationFactory.createFileNameGenerator());
	}

	public BaseDiskCache(File cacheDir, File reserveCacheDir) {
		this(cacheDir, reserveCacheDir, DefaultConfigurationFactory.createFileNameGenerator());
	}

	public BaseDiskCache(File cacheDir, File reserveCacheDir, FileNameGenerator fileNameGenerator) {
		if (cacheDir == null) {
			throw new IllegalArgumentException("cacheDir" + ERROR_ARG_NULL);
		}
		if (fileNameGenerator == null) {
			throw new IllegalArgumentException("fileNameGenerator" + ERROR_ARG_NULL);
		}
		this.cacheDir = cacheDir;
		this.reserveCacheDir = reserveCacheDir;
		this.fileNameGenerator = fileNameGenerator;
	}

	@Override
	public File getDirectory() {
		return cacheDir;
	}

	@Override
	public File get(String url) {
		return getFile(url);
	}

	@Override
	public boolean save(String url, Cache.Entry value) throws IOException {
		File entryFile = getFile(url);
		File tmpFile = new File(entryFile.getAbsolutePath() + TEMP_IMAGE_POSTFIX);
		boolean savedSuccessfully = false;
		try {
			OutputStream os = new BufferedOutputStream(new FileOutputStream(tmpFile), bufferSize);
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
		} finally {
			if (savedSuccessfully && !tmpFile.renameTo(entryFile)) {
				savedSuccessfully = false;
			}
			if (!savedSuccessfully) {
				tmpFile.delete();
			}
		}
		return savedSuccessfully;
	}

	@Override
	public boolean save(String url, InputStream inputStream, Util_IO.CopyListener listener) throws IOException {
		return true;
	}

	@Override
	public boolean remove(String url) {
		return getFile(url).delete();
	}

	@Override
	public void clear() {
		File[] files = cacheDir.listFiles();
		if (files != null) {
			for (File f : files) {
				f.delete();
			}
		}
	}

	@Override
	public void close() {
		//Nothing to do
	}

	@Override
	public String getFileName(String url) {
		return fileNameGenerator.generate(url);
	}

	protected File getFile(String url) {
		String fileName = fileNameGenerator.generate(url);
		File dir = cacheDir;
		if (!cacheDir.exists() && !cacheDir.mkdirs()) {
			if (reserveCacheDir != null && (reserveCacheDir.exists() || reserveCacheDir.mkdirs())) {
				dir = reserveCacheDir;
			}
		}
		return new File(dir, fileName);
	}
}
