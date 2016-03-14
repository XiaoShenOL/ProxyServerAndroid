/*
 * Copyright (C) 2011 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.oplay.nohelper.volley;

import android.os.Process;

import com.oplay.nohelper.volley.cache.disc.DiskCache;
import com.oplay.nohelper.volley.cache.memory.MemoryCache;
import com.oplay.nohelper.volley.ext.VolleyConfiguration;
import com.oplay.nohelper.volley.toolbox.CountingInputStream;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.concurrent.BlockingQueue;

/**
 * Provides a thread for performing cache triage on a queue of requests.
 * <p/>
 * Requests added to the specified cache queue are resolved from cache.
 * Any deliverable response is posted back to the caller via a
 * {@link ResponseDelivery}.  Cache misses and responses that require
 * refresh are enqueued on the specified network queue for processing
 * by a {@link NetworkDispatcher}.
 */
public class CacheDispatcher extends Thread {

	private static final boolean DEBUG = VolleyLog.DEBUG;

	/**
	 * The queue of requests coming in for triage.
	 */
	private final BlockingQueue<Request<?>> mCacheQueue;

	/**
	 * The queue of requests going out to the network.
	 */
	private final BlockingQueue<Request<?>> mNetworkQueue;

	private final VolleyConfiguration configuration;

	private final MemoryCache memoryCache;

	private final DiskCache diskCache;

	/**
	 * For posting responses.
	 */
	private final ResponseDelivery mDelivery;

	/**
	 * Used for telling us to die.
	 */
	private volatile boolean mQuit = false;

	/**
	 * Creates a new cache triage dispatcher thread.  You must call {@link #start()}
	 * in order to begin processing.
	 *
	 * @param cacheQueue   Queue of incoming requests for triage
	 * @param networkQueue Queue to post requests that require network to
	 * @param cache        Cache interface to use for resolution
	 * @param delivery     Delivery interface to use for posting responses
	 */
	public CacheDispatcher(
			BlockingQueue<Request<?>> cacheQueue, BlockingQueue<Request<?>> networkQueue,
			VolleyConfiguration configuration, ResponseDelivery delivery) {
		mCacheQueue = cacheQueue;
		mNetworkQueue = networkQueue;
		this.configuration = configuration;
		this.memoryCache = this.configuration.memoryCache;
		this.diskCache = this.configuration.diskCache;
		mDelivery = delivery;
	}

	/**
	 * Forces this dispatcher to quit immediately.  If any requests are still in
	 * the queue, they are not guaranteed to be processed.
	 */
	public void quit() {
		mQuit = true;
		interrupt();
	}

	@Override
	public void run() {
		if (DEBUG) VolleyLog.v("start new dispatcher");
		Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);

		// Make a blocking call to initialize the cache.
		//mCache.initialize();

		while (true) {
			try {
				// Get a request from the cache triage queue, blocking until
				// at least one is available.
				final Request<?> request = mCacheQueue.take();
				request.addMarker("cache-queue-take");

				// If the request has been canceled, don't bother dispatching it.
				if (request.isCanceled()) {
					request.finish("cache-discard-canceled");
					continue;
				}

				/**
				 * 一旦重启请求,就直接忽略掉缓存
				 */
				if (request.isReset()) {
					request.addMarker("request_reset");
					// Cache miss; send off to the network dispatcher.
					mNetworkQueue.put(request);
					continue;
				}
				/**
				 * 先从缓存中拿出entry,没有就从文件中拿出entry;
				 */
				Cache.Entry entry = memoryCache.get(request.getCacheKey());

				//若memoryCache没有,从diskCache中取出,并且放到 memoryCache;
				if (entry == null) {
					entry = tryLoadValue(request);
					if (entry != null) {
						request.addMarker("cache-hit-disk");
						memoryCache.put(request.getCacheKey(), entry);
					} else if (entry == null) {
						request.addMarker("cache-miss");
						// Cache miss; send off to the network dispatcher.
						mNetworkQueue.put(request);
						continue;
					}
				} else {
					// We have a cache hit; parse its data for delivery back to the request.
					request.addMarker("cache-hit-memory");
				}

				request.addMarker("cache-hit");


				Response<?> response = request.parseNetworkResponse(
						new NetworkResponse(entry.data, entry.responseHeaders));
				request.addMarker("cache-hit-parsed");

				mDelivery.postResponse(request, response);

			} catch (InterruptedException e) {
				// We may have been interrupted because it was time to quit.
				if (mQuit) {
					return;
				}
				continue;
			}
		}
	}

	/**
	 * 尝试从diskCache 中解析出entry;
	 *
	 * @param request
	 * @return
	 */
	private Cache.Entry tryLoadValue(Request request) {
		Cache.Entry entry = null;
		File file = diskCache.get(request.getCacheKey());
		if (file != null) {
			CountingInputStream cis = null;
			try {
				cis = new CountingInputStream(new FileInputStream(file));
				entry = Cache.Entry.readMagic(cis);
				byte[] data = ByteUtils.streamToBytes(cis, (int) (file.length() - cis.bytesRead));
				entry.data = data;

				String json = new String(entry.data, "UTF-8");
				VolleyLog.d("Create data=%s from file=%s", json, diskCache.getFileName(request.getCacheKey()));

			} catch (IOException e) {
				VolleyLog.d("%s: %s", file.getAbsolutePath(), e.toString());
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
		return entry;
	}

}
