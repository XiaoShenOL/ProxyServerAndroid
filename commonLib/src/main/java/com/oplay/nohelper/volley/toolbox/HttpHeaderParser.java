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


import com.oplay.nohelper.volley.Cache;
import com.oplay.nohelper.volley.NetworkResponse;
import com.oplay.nohelper.volley.VolleyLog;

import org.apache.http.impl.cookie.DateParseException;
import org.apache.http.impl.cookie.DateUtils;
import org.apache.http.protocol.HTTP;

import java.util.Map;

/**
 * Utility methods for parsing HTTP headers.
 */
public class HttpHeaderParser {

	/**
	 * 设置缓存时间
	 */
	private static long mCacheTime = 0;
	/**
	 * 每个请求对应一个key,就是cacheKey;
	 */
	private static String key = null;


	public static void setCacheKey(String cacheKey) {
		key = cacheKey;
	}

	/**
	 * Extracts a {@link Cache.Entry} from a {@link NetworkResponse}.
	 *
	 * @param response The network response to parse headers from
	 * @return a cache entry for the given response, or null if the response is not cacheable.
	 */
	public static Cache.Entry parseCacheHeaders(NetworkResponse response) {
		long now = System.currentTimeMillis();

		Map<String, String> headers = response.headers;

		long serverDate = 0;

		String serverEtag = null;
		String headerValue;

		if (headers.containsKey("Data")) {
			headerValue = headers.get("Date");
			if (headerValue != null) {
				serverDate = parseDateAsEpoch(headerValue);
			}
		}

		if (headers.containsKey("ETag")) {
			serverEtag = headers.get("ETag");
			VolleyLog.d("Cache-Control", serverEtag.toString());
		}

		Cache.Entry entry = new Cache.Entry();
		entry.setKey(key);
		entry.data = response.data;
		entry.mEtag = serverEtag;
		entry.mServerDate = serverDate;
		entry.responseHeaders = headers;
		return entry;
	}

	/**
	 * Parse date in RFC1123 format, and return its value as epoch
	 */
	public static long parseDateAsEpoch(String dateStr) {
		try {
			// Parse date in RFC1123 format if this header contains one
			return DateUtils.parseDate(dateStr).getTime();
		} catch (DateParseException e) {
			// Date in invalid format, fallback to 0
			return 0;
		}
	}

	/**
	 * Returns the charset specified in the Content-Type of this header,
	 * <p/>
	 * or the HTTP default (ISO-8859-1) if none can be found.
	 */
	public static String parseCharset(Map<String, String> headers) {
		String contentType = headers.get(HTTP.CONTENT_TYPE);
		if (contentType != null) {
			String[] params = contentType.split(";");
			for (int i = 1; i < params.length; i++) {
				String[] pair = params[i].trim().split("=");
				if (pair.length == 2) {
					if (pair[0].equals("charset")) {
						return pair[1];
					}
				}
			}
		}

		return HTTP.DEFAULT_CONTENT_CHARSET;
	}
}
