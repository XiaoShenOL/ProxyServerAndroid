package com.oplay.nohelper.volley;

import java.util.HashMap;
import java.util.Map;

/**
 * @author CsHeng
 * @Date 14-10-27
 * @Time 下午4:37
 */
public class RequestEntity<T> {

	private boolean mDelCache;
	private boolean mShouldCache;
	private final int mMethod;
	private final String mUrl;
	private final Class<T> mClassOfT;
	private final Map<String, String> mHeaders;
	private final Map<String, String> mPostParams;


	public RequestEntity(String url, Class<T> classOfT) {
		Map<String, String> headers = new HashMap<>();
		headers.put("Content-Type", "application/x-www-form-urlencoded");
		mUrl = url;
		mMethod = Request.Method.GET;
		mClassOfT = classOfT;
		mHeaders = headers;
		mPostParams = null;
		mShouldCache = false;
	}

	public RequestEntity(String url, int method, Class<T> classOfT) {
		Map<String, String> headers = new HashMap<>();
		headers.put("Content-Type", "application/x-www-form-urlencoded");
		mUrl = url;
		mMethod = method;
		mClassOfT = classOfT;
		mHeaders = headers;
		mPostParams = null;
		mShouldCache = false;
	}

	public RequestEntity(String url, Class<T> classOfT, boolean shouldCache) {
		Map<String, String> headers = new HashMap<>();
		headers.put("Content-Type", "application/x-www-form-urlencoded");
		mUrl = url;
		mMethod = Request.Method.GET;
		mClassOfT = classOfT;
		mHeaders = headers;
		mPostParams = null;
		mShouldCache = shouldCache;
	}

	public RequestEntity(String url, Class<T> classOfT, Map<String, String> postParams) {
		Map<String, String> headers = new HashMap<>();
		headers.put("Content-Type", "application/x-www-form-urlencoded");
		mUrl = url;
		mMethod = Request.Method.POST;
		mClassOfT = classOfT;
		mHeaders = headers;
		mPostParams = postParams;
		mShouldCache = false;
	}

	public RequestEntity(String url, int method, Class<T> classOfT, Map<String, String> headers,
	                     Map<String, String> postParams, boolean shouldCache) {
		mUrl = url;
		mMethod = method;
		mClassOfT = classOfT;
		mHeaders = headers;
		mPostParams = postParams;
		mShouldCache = shouldCache;
	}


	public boolean isDelCache() {
		return mDelCache;
	}

	public void setDelCache(boolean delCache) {
		mDelCache = delCache;
	}

	public void setShouldCache(boolean shouldCache) {
		mShouldCache = shouldCache;
	}

	public boolean isShouldCache() {
		return mShouldCache;
	}

	public String getUrl() {
		return mUrl;
	}

	public int getMethod() {
		return mMethod;
	}

	public Class<T> getClassOfT() {
		return mClassOfT;
	}

	public Map<String, String> getHeaders() {
		return mHeaders;
	}

	public Map<String, String> getPostParams() {
		return mPostParams;
	}
}
