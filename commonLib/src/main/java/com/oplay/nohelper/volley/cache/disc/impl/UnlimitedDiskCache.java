package com.oplay.nohelper.volley.cache.disc.impl;

import com.oplay.nohelper.volley.cache.disc.naming.FileNameGenerator;
import com.oplay.nohelper.volley.ext.DefaultConfigurationFactory;

import java.io.File;

/**
 * Created by qin on 15-3-13.
 */
public class UnlimitedDiskCache extends BaseDiskCache {

	public UnlimitedDiskCache(File cacheDir) {
		super(cacheDir);
	}

	public UnlimitedDiskCache(File cacheDir, File reserveCacheDir) {
		super(cacheDir, reserveCacheDir, DefaultConfigurationFactory.createFileNameGenerator());
	}

	public UnlimitedDiskCache(File cacheDir, File reserveCacheDir, FileNameGenerator fileNameGenerator) {
		super(cacheDir, reserveCacheDir, fileNameGenerator);
	}
}
