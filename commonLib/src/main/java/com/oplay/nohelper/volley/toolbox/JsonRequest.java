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

package com.oplay.nohelper.volley.toolbox;


import com.oplay.nohelper.volley.AuthFailureError;
import com.oplay.nohelper.volley.DefaultRetryPolicy;
import com.oplay.nohelper.volley.NetworkResponse;
import com.oplay.nohelper.volley.ParseError;
import com.oplay.nohelper.volley.Request;
import com.oplay.nohelper.volley.RequestEntity;
import com.oplay.nohelper.volley.Response;
import com.oplay.nohelper.volley.VolleyLog;

import org.apache.http.protocol.HTTP;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Iterator;
import java.util.Map;

/**
 * A request for retrieving a T type response body at a given URL that also
 * optionally sends along a JSON body in the request specified.
 *
 * @param <T> JSON type of response expected
 */
public abstract class JsonRequest<T> extends Request<T> {

	/**
	 * The default socket timeout in milliseconds
	 */
	public static final int DEFAULT_TIMEOUT_MS = 2500;

	private static final String PROTOCOL_CHARSET = "utf-8";
	private static final String PROTOCOL_CONTENT_TYPE =
			String.format("application/json; charset=%s", PROTOCOL_CHARSET);
	private final RequestEntity<T> mRequestEntity;
	private final Response.Listener<T> mListener;
	private boolean mIsCacheHit = false;

	public JsonRequest(RequestEntity<T> requestEntity, Response.Listener<T> listener,
	                   Response.ErrorListener errorListener) {
		super(requestEntity.getMethod(), requestEntity.getUrl(), errorListener, requestEntity.isShouldCache());
		this.mListener = listener;
		this.mRequestEntity = requestEntity;
		setRetryPolicy(new DefaultRetryPolicy(DEFAULT_TIMEOUT_MS, DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
				DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
	}

	public static String getParamsString(Map<String, String> params) {
		try {
			if (params == null || params.size() == 0) {
				return "";
			}
			final Iterator<String> keySet = params.keySet().iterator();
			final StringBuilder sb = new StringBuilder();
			while (keySet.hasNext()) {
				final String key = keySet.next();
				final String val = params.get(key);
				if (val != null) {
					sb.append(key).append("=").append(URLEncoder.encode(val, HTTP.UTF_8));
					if (keySet.hasNext()) {
						sb.append("&");
					}
				}
			}
			return sb.toString();
		} catch (Throwable e) {
		}
		return "";
	}

	public void addMarker(String tag) {
		super.addMarker(tag);
		if (tag.equals("cache-hit")) {
			mIsCacheHit = true;
		} else if (tag.equals("network-http-complete")) {
			mIsCacheHit = false;
		}
	}

	@Override
	public String getCacheKey() {
		String url = null;
		if (mRequestEntity.getMethod() == Method.POST && mRequestEntity.getPostParams() != null) {
			url = getUrl() + mRequestEntity.getPostParams();
			HttpHeaderParser.setCacheKey(url);
			return url;
		}
		url = super.getCacheKey();
		HttpHeaderParser.setCacheKey(url);
		return url;
	}

	@Override
	public Map<String, String> getHeaders() throws AuthFailureError {
		return mRequestEntity.getHeaders() != null ? mRequestEntity.getHeaders() : super.getHeaders();
	}

	@Override
	protected Map<String, String> getParams() throws AuthFailureError {
		return mRequestEntity.getPostParams() != null ? mRequestEntity.getPostParams() : super.getParams();
	}

	@Override
	public String getBodyContentType() {
		return PROTOCOL_CONTENT_TYPE;
	}

	@Override
	protected Response<T> parseNetworkResponse(NetworkResponse response) {
		try {
			String json = new String(response.data, "UTF-8");
			if (VolleyLog.DEBUG) {
			    /*VolleyLog.e("\n[Request]:"+ getUrl() + "\n[params]:" + getParamsString(getParams())
			    + "\n[class]:" + mRequestEntity.getClassOfT());*/
				VolleyLog.d("result is %s", json);
				VolleyLog.d("\nparams is %s", getParamsString(getParams()));
				VolleyLog.d("\nclass is %s", mRequestEntity.getClassOfT());
			}
			return Response.success(fromJson(json, mRequestEntity.getClassOfT()), HttpHeaderParser.parseCacheHeaders
					(response));
		} catch (UnsupportedEncodingException e) {
			if (VolleyLog.DEBUG) {
				VolleyLog.e("%s", e.toString());
			}
			return Response.error(new ParseError(e));
		} catch (Exception e) {
			if (VolleyLog.DEBUG) {
				VolleyLog.e("%s", e.toString());
			}
			return Response.error(new ParseError(e));
		}


	}

	@Override
	protected void deliverResponse(T response) {
		mListener.onResponse(response);
	}

	public boolean isCacheHit() {
		return mIsCacheHit;
	}

	/**
	 * construct object from json string
	 *
	 * @param json     json to be deserilize
	 * @param classOfT class to construct
	 * @return
	 */
	protected abstract T fromJson(String json, Class<T> classOfT);


}
