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

import android.annotation.TargetApi;
import android.net.TrafficStats;
import android.os.Build;
import android.os.Process;
import android.os.SystemClock;

import com.oplay.nohelper.volley.cache.disc.DiskCache;
import com.oplay.nohelper.volley.cache.memory.MemoryCache;
import com.oplay.nohelper.volley.ext.VolleyConfiguration;

import java.util.concurrent.BlockingQueue;


/**
 * Provides a thread for performing network dispatch from a queue of requests.
 * <p/>
 * Requests added to the specified queue are processed from the network via a
 * specified {@link Network} interface. Responses are committed to cache, if
 * eligible, using a specified {@link Cache} interface. Valid responses and
 * errors are posted back to the caller via a {@link ResponseDelivery}.
 */
public class NetworkDispatcher extends Thread {
	/**
	 * The queue of requests to service.
	 */
	private final BlockingQueue<Request<?>> mQueue;
	/**
	 * The network interface for processing requests.
	 */
	private final Network mNetwork;
	/**
	 * The cache to write to.
	 */

	private final VolleyConfiguration mVolleyConfiguration;

	private final MemoryCache mMemoryCache;

	private final DiskCache mDiskCache;


	/**
	 * For posting responses and errors.
	 */
	private final ResponseDelivery mDelivery;
	/**
	 * Used for telling us to die.
	 */
	private volatile boolean mQuit = false;

	/**
	 * Creates a new network dispatcher thread.  You must call {@link #start()}
	 * in order to begin processing.
	 *
	 * @param queue    Queue of incoming requests for triage
	 * @param network  Network interface to use for performing requests
	 * @param cache    Cache interface to use for writing responses to cache
	 * @param delivery Delivery interface to use for posting responses
	 */
	public NetworkDispatcher(BlockingQueue<Request<?>> queue,
	                         Network network, VolleyConfiguration volleyConfiguration,
	                         ResponseDelivery delivery) {
		mQueue = queue;
		mNetwork = network;
		mVolleyConfiguration = volleyConfiguration;
		mMemoryCache = mVolleyConfiguration.memoryCache;
		mDiskCache = mVolleyConfiguration.diskCache;
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

	@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
	private void addTrafficStatsTag(Request<?> request) {
		// Tag the request (if API >= 14)
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
			TrafficStats.setThreadStatsTag(request.getTrafficStatsTag());
		}
	}

	@Override
	public void run() {
		Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);
		while (true) {
			long startTimeMs = SystemClock.elapsedRealtime();
			Request<?> request;
			try {
				// Take a request from the queue.
				request = mQueue.take();
			} catch (InterruptedException e) {
				// We may have been interrupted because it was time to quit.
				if (mQuit) {
					return;
				}
				continue;
			}

			try {
				request.addMarker("network-queue-take");
				VolleyLog.d("%s", "network_queue_take");

				// If the request was cancelled already, do not perform the
				// network request.
				if (request.isCanceled()) {
					request.finish("network-discard-cancelled");
					VolleyLog.d("%s", "network-discard-cancelled");
					continue;
				}

				addTrafficStatsTag(request);

				// Perform the network request.
				NetworkResponse networkResponse = mNetwork.performRequest(request);


				// If the server returned 304 AND we delivered a response already,
				// we're done -- don't deliver a second identical response.
				//　表示已经有了，直接从缓存中拿出数据显示出来
				if (request.hasHadResponseDelivered()) {
					request.finish("表示该request已经提交了且成功返回");
					VolleyLog.d("%s", "表示该request已经提交了且成功返回");
					continue;
				}
				// Parse the response here on the worker thread.
				Response<?> response = request.parseNetworkResponse(networkResponse);
				request.addMarker("network-parse-complete");


				if (request.isShouldCache() && response.cacheEntry != null) {
					mMemoryCache.put(request.getCacheKey(), response.cacheEntry);
					mDiskCache.save(request.getCacheKey(), response.cacheEntry);
					request.addMarker("network-cache-written");
				}

				// Post the response back.
				request.markDelivered();
				mDelivery.postResponse(request, response);
			} catch (VolleyError volleyError) {
				VolleyLog.e(volleyError,"错误　%s",volleyError.toString());
				volleyError.setNetworkTimeMs(SystemClock.elapsedRealtime() - startTimeMs);
				parseAndDeliverNetworkError(request, volleyError);
			} catch (Exception e) {
				VolleyLog.e(e, "Unhandled exception %s", e.toString());
				VolleyError volleyError = new VolleyError(e);
				volleyError.setNetworkTimeMs(SystemClock.elapsedRealtime() - startTimeMs);
				mDelivery.postError(request, volleyError);
			}
		}
	}

	private void parseAndDeliverNetworkError(Request<?> request, VolleyError error) {
		error = request.parseNetworkError(error);
		mDelivery.postError(request, error);
	}


}
