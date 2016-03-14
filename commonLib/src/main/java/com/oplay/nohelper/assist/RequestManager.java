package com.oplay.nohelper.assist;

import android.content.Context;

import com.oplay.nohelper.volley.Request;
import com.oplay.nohelper.volley.RequestQueue;
import com.oplay.nohelper.volley.ext.VolleyConfiguration;
import com.oplay.nohelper.volley.toolbox.Volley;

/**
 * @author zyq 16-3-9
 */
public class RequestManager {
	private volatile static RequestManager instance;
	private RequestQueue mRequestQueue;
	private Context mContext;

	/**
	 * 使用双重检测方式,同时volatile保持原子性,而且,先不加锁,再加锁.
	 *
	 * @return
	 */
	public static RequestManager getInstance() {
		if (instance == null) {
			synchronized (RequestManager.class) {
				if (instance == null) {
					instance = new RequestManager();
				}
			}
		}
		return instance;
	}

	public void initConfiguration(Context context, VolleyConfiguration configuration) {
		mContext = context.getApplicationContext();
		mRequestQueue = Volley.newRequestQueue(mContext, configuration);
	}

	public RequestQueue getRequestQueue() {
		return mRequestQueue;
	}

	/**
	 * 对于每个请求,都有一个标志 tag,用于后面我们可以取消该request.
	 *
	 * @param request
	 * @param tag
	 * @param <T>
	 */
	public <T> void addRequest(Request<T> request, Object tag) {

		if (tag != null) {
			request.setTag(tag);
		}
		if (mRequestQueue != null) {
			mRequestQueue.add(request);
		}
	}

	public void cancelAll(Object tag) {
		mRequestQueue.cancelAll(tag);
	}

	public void removeDiskCache(String cacheKey) {
		if (mRequestQueue.getVolleyConfiguration() != null && mRequestQueue.getVolleyConfiguration().diskCache !=
				null) {
			mRequestQueue.getVolleyConfiguration().diskCache.remove(cacheKey);
		}
	}

	public void removeMemoryCache(String cacheKey) {
		if (mRequestQueue.getVolleyConfiguration() != null && mRequestQueue.getVolleyConfiguration().memoryCache !=
				null) {
			mRequestQueue.getVolleyConfiguration().memoryCache.remove(cacheKey);
		}

	}

	/**
	 * 在用户切换账号时候,需要清掉所有缓存.
	 */
	public void removeAllCache() {
		if (mRequestQueue.getVolleyConfiguration() != null && mRequestQueue.getVolleyConfiguration().diskCache !=
				null) {
			mRequestQueue.getVolleyConfiguration().diskCache.clear();
		}
		if (mRequestQueue.getVolleyConfiguration() != null && mRequestQueue.getVolleyConfiguration().memoryCache !=
				null) {
			mRequestQueue.getVolleyConfiguration().memoryCache.clear();
		}
	}
}
