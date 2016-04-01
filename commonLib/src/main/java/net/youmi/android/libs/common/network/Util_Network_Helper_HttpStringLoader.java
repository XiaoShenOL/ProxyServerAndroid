package net.youmi.android.libs.common.network;

import android.content.Context;

import net.youmi.android.libs.common.debug.Debug_SDK;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.protocol.HTTP;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.zip.GZIPInputStream;

/**
 * 注意，使用该类请求的url返回值必须是Utf-8 String，否则会抛异常导致崩溃<br/>
 * 该类使用于自家协议
 */
public class Util_Network_Helper_HttpStringLoader {

	//    private final static String mTag = "network";
	private final static String mTag = Util_Network_Helper_HttpStringLoader.class.getSimpleName();

	public static String load_Http_String(Context context, String url, String encoding) {
		return load_Http_String(context, url, null, encoding);
	}

	public static String load_Http_String(Context context, String url, List<NameValuePair> postData, String encoding) {
		if (postData != null) {
			return loadHttp_String_post(context, url, postData, encoding, null);
		} else {
			return loadHttp_String_get(context, url, encoding);
		}
	}

	public static String load_Http_Utf8_String(Context context, String url, boolean isLogReqMsg) {
		return load_Http_Utf8_String(context, url, null, isLogReqMsg, null);
	}

	public static String load_Http_Utf8_String(Context context, String url, boolean isLogReqMsg, Header[] headers) {
		return load_Http_Utf8_String(context, url, null, isLogReqMsg, headers);
	}

	public static String load_Http_Utf8_String(Context context, String url) {
		return load_Http_Utf8_String(context, url, null);
	}

	public static String load_Http_Utf8_String(Context context, String url, List<NameValuePair> postData,
	                                           boolean isLogReqMsg, Header[] headers) {
		if (postData != null) {
			return loadHttp_String_post(context, url, postData, HTTP.UTF_8, headers);
		} else {
			return loadHttp_String_get(context, url, HTTP.UTF_8, isLogReqMsg, headers);
		}
	}

	public static String load_Http_Utf8_String(Context context, String url, List<NameValuePair> postData) {
		if (postData != null) {
			return loadHttp_String_post(context, url, postData, HTTP.UTF_8, null);
		} else {
			return loadHttp_String_get(context, url, HTTP.UTF_8);
		}
	}

	public static String load_Http_String(Context context, Net_HttpRequester requester) {
		try {
			if (context == null || requester == null || context.getApplicationContext() == null ||
					requester.getRequestUrl() == null) {
				return null;
			}

			Context applicationContext = context.getApplicationContext();

			List<NameValuePair> list = requester.getPostDataNameValuePair();

			String url = requester.getRequestUrl();

		} catch (Throwable e) {

		}
		return null;
	}

	/**
	 * 从网络加载字符串
	 *
	 * @param context
	 * @param url
	 * @param charset
	 * @return
	 */
	private static String loadHttp_String_get(Context context, String url, String charset) {
		return loadHttp_String_get(context, url, charset, false, null);
	}

	/**
	 * 从网络加载字符串
	 *
	 * @param context
	 * @param url
	 * @param charset
	 * @param isLogReqMsg 是否全程输出关键路径信息
	 * @return
	 */
	private static String loadHttp_String_get(final Context context, String url, String charset,
	                                          boolean isLogReqMsg, Header[] headers) {

		if (url == null) {
			if (isLogReqMsg) {
				Debug_SDK.di(mTag, "Request error , url is null");
			} else {
				if (Debug_SDK.isNetLog) {
					Debug_SDK.de(mTag, "Request error , url is null");
				}
			}
			return null;
		}

		if (isLogReqMsg) {
			Debug_SDK.di(mTag, "Request url : %s", url); // 输出url
		} else {
			if (Debug_SDK.isNetLog) {
				// 输出url
				Debug_SDK.dd(mTag, String.format("Request url : %s", url));
			}
		}

		if (Debug_SDK.isNetLog) {
			Debug_SDK.de(mTag, String.format("网络加载:%s", url));
		}

		try {
			final ArrayList<Header> headerArrayList = new ArrayList<Header>();
			if (headers != null && headers.length > 0) {
				for (int i = 0; i < headers.length; i++) {
					headerArrayList.add(headers[i]);
				}
			}

			final Net_HttpRequester requester = new Net_HttpRequester();
			requester.setRequestUrl(url);
			requester.setShowReqMsgInLog(false);
			requester.setRequestHeaders(headerArrayList);
			final Net_HttpStringRequestExecutor executor = new Net_HttpStringRequestExecutor(context, requester);
			final Net_HttpMonitor monitor = new Net_HttpMonitor();
			monitor.setIsNeedToCollectHeaders(true);
			executor.setMonitor(monitor);
			executor.execute();
			return executor.getResult();
		} catch (Throwable throwable) {
			if (Debug_SDK.isNetLog) {
				Debug_SDK.de(mTag, throwable);
			}
		}

		return null;
	}

	/**
	 * Post utf-8 String
	 *
	 * @param context
	 * @param url
	 * @param postData
	 * @return
	 */
	private static String loadHttp_String_post(final Context context, String url,
	                                           final List<NameValuePair> postData, String charset, Header[] headers) {

		if (url == null) {
			return null;
		}

		if (Debug_SDK.isNetLog) {
			Debug_SDK.de(mTag, url);
		}

		try {
			ArrayList<Header> headerArrayList = new ArrayList<Header>();
			if (headers != null && headers.length > 0) {
				for (int i = 0; i < headers.length; i++) {
					headerArrayList.add(headers[i]);
				}
			}

			final Net_HttpRequester requester = new Net_HttpRequester();
			requester.setRequestUrl(url);
			requester.setShowReqMsgInLog(false);
			requester.setPostDataNameValuePair(postData);
			requester.setRequestHeaders(headerArrayList);
			final Net_HttpStringRequestExecutor executor = new Net_HttpStringRequestExecutor(context, requester);
			final Net_HttpMonitor monitor = new Net_HttpMonitor();
			monitor.setIsNeedToCollectHeaders(true);
			executor.setMonitor(monitor);
			executor.execute();
			return executor.getResult();

		} catch (Throwable throwable) {
			if (Debug_SDK.isNetLog) {
				Debug_SDK.de(mTag, throwable);
			}
		}

		return null;
	}

	private static String getStringFromContentInputStream(Context context,
	                                                      HttpResponse response, String charset, boolean isLogReqMsg) {
		ByteArrayOutputStream baos = null;
		InputStream inputStream = null;
		try {
			if (response != null) {
				int httpCode = response.getStatusLine().getStatusCode();
				if (isLogReqMsg) {
					Debug_SDK.di(mTag, "Response http code is : %d", httpCode);
				} else {
					if (Debug_SDK.isNetLog) {
						Debug_SDK.dd(mTag, "Response http code is : %d", httpCode);
					}
				}

				if (httpCode >= 200 && httpCode < 300) {
					HttpEntity entity = response.getEntity();

					if (isLogReqMsg) {
						Debug_SDK.di(mTag, "Response ContentLength : %d , ContentType : %s", entity.getContentLength(),
								entity.getContentType());
					} else {
						if (Debug_SDK.isNetLog) {
							Debug_SDK.de(mTag, "网络加载:length:%d,type:%s", entity.getContentLength(),
									entity.getContentType());
						}
					}

					try {
						final Header header = entity.getContentEncoding();
						if (header != null) {

							if (header.getValue().toLowerCase(Locale.US).contains("gzip")) {
								inputStream = new GZIPInputStream(entity.getContent());

								if (Debug_SDK.isNetLog) {
									Debug_SDK.dd(mTag, "网络加载，已经使用了gzip进行解压");
								}
							}

							if (Debug_SDK.isNetLog) {
								Debug_SDK.de(mTag, "网络加载: ContentEncoding key-> %s ,value -> %s  ", header.getName(),
										header.getValue());
							}
						} else {
							if (Debug_SDK.isNetLog) {
								Debug_SDK.de(mTag, "网络加载: ContentEncoding is null ");
							}
						}

					} catch (Throwable e) {
						if (isLogReqMsg) {
							Debug_SDK.di(mTag, e);
						} else {
							if (Debug_SDK.isNetLog) {
								Debug_SDK.de(mTag, e);
							}
						}
					}

					if (inputStream == null) {
						// 表示没有使用gzip
						inputStream = entity.getContent();
					}

					if (inputStream == null) {
						if (isLogReqMsg) {
							Debug_SDK.di(mTag, "Response Content is null");
						} else {
							if (Debug_SDK.isNetLog) {
								Debug_SDK.de(mTag, "Response Content is null");
							}
						}
						return null;
					}

					baos = new ByteArrayOutputStream();

					byte[] buff = new byte[1024];

					int len = 0;

					while ((len = inputStream.read(buff)) > 0) {
						baos.write(buff, 0, len);
					}

					baos.flush();
					byte[] buffer = baos.toByteArray();

					String rspString = new String(buffer, charset);

					if (Debug_SDK.isNetLog) {
						Debug_SDK.de(mTag, "网络请求结果:length:%d,%s", buffer.length, rspString);
					}

					return rspString;
				}
			} else {
				if (isLogReqMsg) {
					Debug_SDK.di(mTag, "Request failed , response is null"); // 没有response
				} else {
					if (Debug_SDK.isNetLog) {
						Debug_SDK.de(mTag, "Request failed , response is null");
					}
				}
			}
		} catch (Throwable e) {
			if (isLogReqMsg) {
				Debug_SDK.de(mTag, e);// 输出debug信息
			} else {
				if (Debug_SDK.isNetLog) {
					Debug_SDK.de(mTag, e);
				}
			}
		} finally {
			try {
				if (baos != null) {
					baos.close();
				}
			} catch (Throwable e2) {
				if (Debug_SDK.isNetLog) {
					Debug_SDK.de(mTag, e2);
				}
			}
		}

		return null;
	}

}
