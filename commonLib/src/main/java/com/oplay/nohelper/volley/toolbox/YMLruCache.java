package com.oplay.nohelper.volley.toolbox;


import com.oplay.nohelper.volley.ByteUtils;
import com.oplay.nohelper.volley.Cache;
import com.oplay.nohelper.volley.VolleyLog;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 *
 * Created by qin on 15-2-11.
 */
public class YMLruCache {

	private static final int DEFAULT_DISK_USAGE_BYTES = 5 * 1024 * 1024;
	private static final int CACHE_MAGIC = 0x20140623;
	private static File mRootDirectory;
	private final LinkedHashMap<String, Cache.Entry> map;
	/**
	 * Size of this cache in units. Not necessarily the number of elements.
	 */
	private int size;
	private int maxSize;
	private int putCount;
	private int createCount;
	private int evictionCount;
	private int hitCount;
	private int missCount;

	/**
	 * @param maxSize for caches that do not override {@link #sizeOf}, this is
	 *                the maximum number of entries in the cache. For all other caches,
	 *                this is the maximum sum of the sizes of the entries in this cache.
	 */
	public YMLruCache(File rootDirectory, int maxSize) {
		if (maxSize <= 0) {
			throw new IllegalArgumentException("maxSize <= 0");
		}
		this.maxSize = maxSize;
		this.map = new LinkedHashMap<String, Cache.Entry>(16, 0.75f, true);
		mRootDirectory = rootDirectory;
	}

	public YMLruCache(File rootDirectory) {
		this(rootDirectory, DEFAULT_DISK_USAGE_BYTES);
	}

	/**
	 * Sets the size of the cache.
	 *
	 * @param maxSize The new maximum size.
	 * @hide
	 */
	public void resize(int maxSize) {
		if (maxSize <= 0) {
			throw new IllegalArgumentException("maxSize <= 0");
		}

		synchronized (this) {
			this.maxSize = maxSize;
		}
		trimToSize(maxSize);
	}

	/**
	 * Returns the value for {@code key} if it exists in the cache or can be
	 * created by {@code #create}. If a value was returned, it is moved to the
	 * head of the queue. This returns null if a value is not cached and cannot
	 * be created.
	 */
	public final Cache.Entry get(String key) {
		VolleyLog.e("get(String key)");
		if (key == null) {
			throw new NullPointerException("key == null");
		}
		Cache.Entry mapValue;
		synchronized (this) {
			mapValue = map.get(key);
			if (mapValue != null && mapValue.get() != null) {
				hitCount++;
				return mapValue;
			} else if (mapValue != null && mapValue.get() == null) {
				removeOnMem(key);
			}
			missCount++;
		}

		/**
		 * 这里从文件中读取数据，所失败，要删掉对应的缓存，若成功，要把数据加载到LruCache中
		 */
		Cache.Entry createdValue = getFromFile(key);
		if (createdValue == null) {
			return createdValue;
		}

		synchronized (this) {
			createCount++;
			mapValue = map.put(key, createdValue);
			if (mapValue != null && mapValue.get() != null) {
				// There was a conflict so undo that last put
				//　这里可能是丢失了一个item ,所以可能会重新从缓存中拿到，所以要重新覆盖
				map.put(key, mapValue);
			} else {
				size += safeSizeOf(key, createdValue);
			}
		}
		if (mapValue != null && mapValue.get() != null) {
			entryRemoved(false, key, createdValue, mapValue);
			return mapValue;
		} else {
			trimToSize(maxSize);
			return createdValue;
		}
	}


	/**
	 * Initializes the DiskBasedCache by scanning for all files currently in the
	 * specified root directory. Creates the root directory if necessary.
	 */
	public synchronized void initialize() {
		VolleyLog.e("initialize()");
		if (!mRootDirectory.exists()) {
			if (!mRootDirectory.mkdirs()) {
				VolleyLog.e("Unable to create cache dir %s", mRootDirectory.getAbsolutePath());
			}
			return;
		}

		File[] files = mRootDirectory.listFiles();
		if (files == null) {
			return;
		}

		for (File file : files) {
			VolleyLog.d("缓存文件的名字:%s", file.getName());
			CountingInputStream cis = null;
			try {
				cis = new CountingInputStream(new FileInputStream(file));
				Cache.Entry entry = Cache.Entry.readMagic(cis);
				byte[] data = ByteUtils.streamToBytes(cis, (int) (file.length() - cis.bytesRead));
				if (data.length == 0 && data != null) {
					break;
				}
				entry.data = data;
				VolleyLog.d("从文件%s中烤出数据%s", file.getName(), entry.data.toString());
				put(entry.getKey(), entry, true);
			} catch (IOException e) {
				if (file != null) {
					file.delete();
				}
			} finally {
				try {
					if (cis != null) {
						cis.close();
					}
				} catch (IOException ignored) {

				}

			}

		}

		for (Map.Entry<String, Cache.Entry> entry : map.entrySet()) {
			VolleyLog.d("key:%s", entry.getKey());
			VolleyLog.d("Cache.Entry.data: %s", entry.getValue().data);
		}
	}


	public synchronized void clear() {
		VolleyLog.e("reset()");
		File[] files = mRootDirectory.listFiles();
		if (files != null) {
			for (File file : files) {
				file.delete();
			}
		}
		map.clear();
		size = 0;
		VolleyLog.d("Cache Clear");
	}

	/**
	 * Caches {@code value} for {@code key}. The value is moved to the head of
	 * the queue.
	 *
	 * @return the previous value mapped by {@code key}.
	 */
	public Cache.Entry put(String key, Cache.Entry value, boolean hadAdd) {
		VolleyLog.e("put(String key, Cache.Entry value,boolean hadAdd)");
		if (key == null || value == null) {
			throw new NullPointerException("key == null || value == null");
		}
		Cache.Entry previous;
		synchronized (this) {
			putCount++;
			size += safeSizeOf(key, value);
			previous = map.put(key, value);
			if (previous != null) {
				size -= safeSizeOf(key, previous);
			}
		}

		if (previous != null) {
			entryRemoved(false, key, previous, value);
		}

		trimToSize(maxSize);

		/**
		 * 添加到文件中
		 */
		if (!hadAdd) {
			addToFile(key, value);
		}
		return previous;
	}


	public Cache.Entry put(String key, Cache.Entry entry) {
		return put(key, entry, false);
	}

	public void addToFile(String key, Cache.Entry value) {
		VolleyLog.e("addtoFile(String key,Cache.Entry value)");
		File file = getFileForKey(key);
		FileOutputStream fos = null;
		try {
			if (!file.exists()) {
				file.createNewFile();
			}
			fos = new FileOutputStream(file);
			value.writeMagic(fos);
			fos.write(value.data);
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (fos != null) {
					fos.close();
				}
			} catch (IOException e) {
				VolleyLog.d("fileName is %s close failed", getFilenameForKey(key));
			}
		}
	}

	/**
	 * @param maxSize the maximum size of the cache before returning. May be -1
	 *                to evict even 0-sized elements.
	 */
	private void trimToSize(int maxSize) {
		while (true) {
			String key;
			Cache.Entry value;
			synchronized (this) {
				if (size < 0 || (map.isEmpty() && size != 0)) {
					throw new IllegalStateException(getClass().getName()
							+ ".sizeOf() is reporting inconsistent results!");
				}
				if (size <= maxSize) {
					break;
				}
				// BEGIN LAYOUTLIB CHANGE
				// get the last item in the linked list.
				// This is not efficient, the goal here is to minimize the changes
				// compared to the platform version.
				Map.Entry<String, Cache.Entry> toEvict = null;
				for (Map.Entry<String, Cache.Entry> entry : map.entrySet()) {
					toEvict = entry;
				}
				// END LAYOUTLIB CHANGE

				if (toEvict == null) {
					break;
				}
				key = toEvict.getKey();
				value = toEvict.getValue();
				map.remove(key);
				size -= safeSizeOf(key, value);
				evictionCount++;
			}
			entryRemoved(true, key, value, null);
		}
	}


	/**
	 * 仅删掉内存数据
	 *
	 * @param key
	 * @return
	 */
	public final Cache.Entry removeOnMem(String key) {
		VolleyLog.e("removeOnMen(String key)");
		return remove(key, false);

	}


	/**
	 * 用于删掉全部数据,包括内存缓存和文件缓存
	 *
	 * @param key
	 * @return
	 */
	public final Cache.Entry removeAll(String key) {
		VolleyLog.e("removeAll(String key)");
		return remove(key, true);
	}


	/**
	 * Removes the entry for {@code key} if it exists.
	 *
	 * @return the previous value mapped by {@code key}.
	 */
	public final Cache.Entry remove(String key, boolean removeFile) {
		VolleyLog.e("remove(String key,boolean removeFile)");
		if (key == null) {
			throw new NullPointerException("key == null");
		}
		/**
		 * 是否删掉文件
		 */
		if (removeFile) {
			File file = findFileFromDir(key);
			if (file != null) {
				boolean deleted = file.delete();
				if (!deleted) {
					if (VolleyLog.DEBUG) {
						VolleyLog.d("Could not delete cache entry for key=%s,filename =%s", key,
								getFilenameForKey(key));
					}
				}
			}
		}
		/**
		 * 从LRUCache 中删掉对应的key
		 */
		Cache.Entry previous;
		synchronized (this) {
			previous = map.remove(key);
			if (previous != null) {
				size -= safeSizeOf(key, previous);
			}
		}
		if (previous != null) {
			entryRemoved(false, key, previous, null);
		}
		return previous;
	}


	/**
	 * Creates a pseudo-unique filename for the specified cache key.
	 *
	 * @param key The key to generate a file name for.
	 * @return A pseudo-unique filename.
	 */
	private String getFilenameForKey(String key) {
		int firstHalfLength = key.length() / 2;
		String localFilename = String.valueOf(key.substring(0, firstHalfLength).hashCode());
		localFilename += String.valueOf(key.substring(firstHalfLength).hashCode());
		VolleyLog.d("CacheFileName" + localFilename);
		return localFilename;
	}

	/**
	 * Called for entries that have been evicted or removed. This method is
	 * invoked when a value is evicted to make space, removed by a call to
	 * {@link #remove}, or replaced by a call to {@link #put}. The default
	 * implementation does nothing.
	 * 当item 被回收或者被删掉时候使用，回收空间时被remove 使用，替换item 时被put 调用
	 * <p>The method is called without synchronization: other threads may
	 * access the cache while this method is executing.
	 *
	 * @param evicted  true if the entry is being removed to make space, false
	 *                 if the removal was caused by a {@link #put} or {@link #remove}.
	 * @param newValue the new value for {@code key}, if it exists. If non-null,
	 *                 this removal was caused by a {@link #put}. Otherwise it was caused by
	 *                 an eviction or a {@link #remove}.
	 */
	protected void entryRemoved(boolean evicted, String key, Cache.Entry oldValue, Cache.Entry newValue) {
	}

	/**
	 * 由于在查找过程中,
	 * Called after a cache miss to compute a value for the corresponding key.
	 * Returns the computed value or null if no value can be computed. The
	 * default implementation returns null.
	 * <p/>
	 * <p>The method is called without synchronization: other threads may
	 * access the cache while this method is executing.
	 * <p/>
	 * <p>If a value for {@code key} exists in the cache when this method
	 * returns, the created value will be released with {@link #entryRemoved}
	 * and discarded. This can occur when multiple threads request the same key
	 * at the same time (causing multiple values to be created), or when one
	 * thread calls {@link #put} while another is creating a value for the same
	 * key.
	 */
	protected synchronized Cache.Entry getFromFile(String key) {
		VolleyLog.e("create (String key)");
		File file = findFileFromDir(key);
		if (file != null) {
			CountingInputStream cis = null;
			try {
				cis = new CountingInputStream(new FileInputStream(file));
				Cache.Entry entry = Cache.Entry.readMagic(cis);
				byte[] data = ByteUtils.streamToBytes(cis, (int) (file.length() - cis.bytesRead));
				/**
				 * 若data的数据为0,表示没有数据嘛，抛出io异常嘛
				 *//*
				if (data.length == 0) {
                    throw new IOException();
                }*/
				entry.data = data;
				VolleyLog.d("Create data=%s from file=%s", entry.data.toString(), getFilenameForKey(key));
				return entry;
			} catch (IOException e) {
				VolleyLog.d("%s: %s", file.getAbsolutePath(), e.toString());
				remove(key, true);
				return null;
			} finally {
				if (cis != null) {
					try {
						cis.close();
					} catch (IOException e) {
						return null;
					}
				}
			}
		}
		return null;
	}

	/**
	 * 只有一个线程处理,不加锁
	 *
	 * @param key
	 * @return
	 */
	private File findFileFromDir(String key) {
		if (key != null) {
			File file = new File(mRootDirectory, getFilenameForKey(key));
			if (file.exists())
				return file;
			return null;
		}
		return null;
	}

	/**
	 * Returns a file object for the given cache key
	 */
	public File getFileForKey(String key) {
		File file = findFileFromDir(key);
		if (file == null) {
			file = new File(mRootDirectory, getFilenameForKey(key));
			try {
				if (!file.exists()) {
					file.createNewFile();
				}
			} catch (IOException e) {
				VolleyLog.d("the file %s created failed", getFilenameForKey(key));
			}
		}
		return file;
	}


	private int safeSizeOf(String key, Cache.Entry value) {
		int result = sizeOf(key, value);
		if (result < 0) {
			throw new IllegalStateException("Negative size: " + key + "=" + value);
		}
		return result;
	}

	/**
	 * Returns the size of the entry for {@code key} and {@code value} in
	 * user-defined units.  The default implementation returns 1 so that size
	 * is the number of entries and max size is the maximum number of entries.
	 * <p/>
	 * <p>An entry's size must not change while it is in the cache.
	 */
	protected int sizeOf(String key, Cache.Entry value) {
		if (value != null && value.get() != null) {
			return value.data.length;
		}
		return 0;
	}

	/**
	 * Clear the cache, calling {@link #entryRemoved} on each removed entry.
	 */
	public final void evictAll() {
		trimToSize(-1); // -1 will evict 0-sized elements
	}

	/**
	 * For caches that do not override {@link #sizeOf}, this returns the number
	 * of entries in the cache. For all other caches, this returns the sum of
	 * the sizes of the entries in this cache.
	 */
	public synchronized final int size() {
		return size;
	}

	/**
	 * For caches that do not override {@link #sizeOf}, this returns the maximum
	 * number of entries in the cache. For all other caches, this returns the
	 * maximum sum of the sizes of the entries in this cache.
	 */
	public synchronized final int maxSize() {
		return maxSize;
	}

	/**
	 * Returns the number of times {@link #get} returned a value that was
	 * already present in the cache.
	 */
	public synchronized final int hitCount() {
		return hitCount;
	}

	/**
	 * Returns the number of times {@link #get} returned null or required a new
	 * value to be created.
	 */
	public synchronized final int missCount() {
		return missCount;
	}

	/**
	 * Returns the number of times {@link #create(Object)} returned a value.
	 */
	public synchronized final int createCount() {
		return createCount;
	}

	/**
	 * Returns the number of times {@link #put} was called.
	 */
	public synchronized final int putCount() {
		return putCount;
	}

	/**
	 * Returns the number of values that have been evicted.
	 */
	public synchronized final int evictionCount() {
		return evictionCount;
	}

	/**
	 * Returns a copy of the current contents of the cache, ordered from least
	 * recently accessed to most recently accessed.
	 */
	public synchronized final Map<String, Cache.Entry> snapshot() {
		return new LinkedHashMap<String, Cache.Entry>(map);
	}

	@Override
	public synchronized final String toString() {
		int accesses = hitCount + missCount;
		int hitPercent = accesses != 0 ? (100 * hitCount / accesses) : 0;
		return String.format("LruCache[maxSize=%d,hits=%d,misses=%d,hitRate=%d%%]",
				maxSize, hitCount, missCount, hitPercent);
	}


	private static class CountingInputStream extends FilterInputStream {
		private int bytesRead = 0;

		private CountingInputStream(InputStream in) {
			super(in);
		}

		@Override
		public int read() throws IOException {
			int result = super.read();
			if (result != -1) {
				bytesRead++;
			}
			return result;
		}

		@Override
		public int read(byte[] buffer, int offset, int count) throws IOException {
			int result = super.read(buffer, offset, count);
			if (result != -1) {
				bytesRead += result;
			}
			return result;
		}
	}
}
