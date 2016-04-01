package net.youmi.android.libs.common.network;

import android.content.Context;
import android.net.Uri;
import android.os.Build;

import net.youmi.android.libs.common.debug.Debug_SDK;
import net.youmi.android.libs.common.global.Global_Runtime_SystemInfo;

import org.apache.http.Header;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.ProtocolException;
import org.apache.http.client.RedirectHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.params.HttpClientParams;
import org.apache.http.conn.params.ConnManagerParams;
import org.apache.http.conn.params.ConnRoutePNames;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.HttpContext;

import java.net.URI;
import java.net.URL;

public class Util_Network_HttpUtil {

	private static String userAgent;

	/**
	 * 从连接池中取出连接的超时时间
	 */
	static final int GET_CONNECTION_TIMEOUT = 5000;
	/**
	 * 连接超时
	 */
	static final int CONNECTION_TIMEOUT = 30000;
	/**
	 * 请求超时
	 */
	static final int SOCKET_TIMEOUT = 30000;

	/**
	 * 获取UserAgent
	 * 
	 * @return
	 */
	public static String getUserAgent() {

		// AdLog.e(Build.class.getName());
		// AdLog.e("BOARD:"+Build.BOARD);
		// AdLog.e("BRAND:"+Build.BRAND);
		// AdLog.e("CPU_ABI:"+Build.CPU_ABI);
		// AdLog.e("DEVICE:"+Build.DEVICE);
		// AdLog.e("DISPLAY:"+Build.DISPLAY);
		// AdLog.e("FINGERPRINT:"+Build.FINGERPRINT);
		// AdLog.e("HOST:"+Build.HOST);
		// AdLog.e("ID:"+Build.ID);
		// AdLog.e("MANUFACTURER:"+Build.MANUFACTURER);
		// AdLog.e("MODEL:"+Build.MODEL);
		// AdLog.e("PRODUCT:"+Build.PRODUCT);
		// AdLog.e("TAGS:"+Build.TAGS);
		// AdLog.e("TYPE:"+Build.TYPE);
		// AdLog.e("USER:"+Build.USER);
		// AdLog.e("VERSION.CODENAME:"+Build.VERSION.CODENAME);
		// AdLog.e("VERSION.INCREMENTAL:"+Build.VERSION.INCREMENTAL);
		// AdLog.e("VERSION.RELEASE:"+Build.VERSION.RELEASE);
		// AdLog.e("VERSION.SDK:"+Build.VERSION.SDK);
		// AdLog.e("VERSION.SDK_INT:"+Build.VERSION.SDK_INT+"");
		// AdLog.e("VERSION_CODES.BASE:"+Build.VERSION_CODES.BASE+"");
		// AdLog.e("VERSION_CODES.BASE_1_1:"+Build.VERSION_CODES.BASE_1_1+"");
		// AdLog.e("VERSION_CODES.CUPCAKE:"+Build.VERSION_CODES.CUPCAKE+"");
		// AdLog.e("VERSION_CODES.CUR_DEVELOPMENT:"+Build.VERSION_CODES.CUR_DEVELOPMENT+"");
		// AdLog.e("Build.VERSION_CODES.DONUT:"+Build.VERSION_CODES.DONUT+"");

		if (userAgent == null) {

			try {

				StringBuilder sb = new StringBuilder(256);
				sb.append("Mozilla/5.0 (Linux; U; Android ");
				sb.append(Build.VERSION.RELEASE);
				sb.append("; ");
				sb.append(Global_Runtime_SystemInfo.getLocaleLanguage_Country().toLowerCase());
				sb.append("; ");
				sb.append(Global_Runtime_SystemInfo.getDeviceModel());
				sb.append(" Build/");
				sb.append(Build.ID);
				sb.append(") AppleWebkit/533.1 (KHTML, like Gecko) Version/4.0 Mobile Safari/533.1");

				userAgent = sb.toString();

			} catch (Throwable e) {
				if (Debug_SDK.isNetLog) {
					Debug_SDK.te(Debug_SDK.mNetTag, Util_Network_HttpUtil.class, e);
				}
				return "";
			}
		}
		return userAgent;
	}

	public static void setUserAgent(String ua) {
		try {
			if (ua != null) {
				ua = ua.trim();
				if (ua.length() > 0) {
					userAgent = ua;
				}
			}
		} catch (Throwable e) {
			if (Debug_SDK.isNetLog) {
				Debug_SDK.te(Debug_SDK.mNetTag, Util_Network_HttpUtil.class, e);
			}
		}
	}

	/**
	 * 创建http请求参数
	 * 
	 * @return
	 */
	public static HttpParams createHttpParams(Context context) {

		BasicHttpParams params = new BasicHttpParams();

		// 从连接池中取连接的超时时间(5s) add by caizhitao on 2014-5-15
		ConnManagerParams.setTimeout(params, 1000);

		// 设置http超时(30秒)
		HttpConnectionParams.setConnectionTimeout(params, CONNECTION_TIMEOUT);

		// 设置socket超时(15秒)->(30秒)-2013-05-14
		HttpConnectionParams.setSoTimeout(params, SOCKET_TIMEOUT);

		// 设置处理自动处理重定向
		HttpClientParams.setRedirecting(params, true);

		// 设置userAgent
		HttpProtocolParams.setUserAgent(params, getUserAgent());

		// 设置utf-8(待测试)
		// HttpProtocolParams.setContentCharset(params, "utf-8");

		// 设置utf-8(待测试)
		// HttpProtocolParams.setHttpElementCharset(params, "utf-8");

		// 为cmwap设置代理
		String apn = Util_Network_Status.getApn(context);
		if (Debug_SDK.isNetLog) {
			Debug_SDK.td(Debug_SDK.mNetTag, Util_Network_HttpUtil.class, "apn: %s", apn);
		}
		if (apn.equals(Util_Network_Status.APN_CMWAP)) {

			HttpHost proxy = new HttpHost("10.0.0.172", 80, null);
			params.setParameter(ConnRoutePNames.DEFAULT_PROXY, proxy);
		}

		return params;
	}

	public static DefaultHttpClient createHttpClient(Context context) {
		return createHttpClient(context, null);
	}

	/**
	 * 创建DefaultHttpClient
	 * 
	 * @param context
	 * @return
	 */
	public static DefaultHttpClient createHttpClient(Context context, final Net_UrlRedirectListener linstener) {

		DefaultHttpClient httpClient = new DefaultHttpClient(createHttpParams(context));

		// 设置重定向处理，目的是获得重定向后的地址
		httpClient.setRedirectHandler(new RedirectHandler() {

			@Override
			public boolean isRedirectRequested(HttpResponse response, HttpContext context) {
				int statusCode = response.getStatusLine().getStatusCode();
				if (Debug_SDK.isNetLog) {
					Debug_SDK.td(Debug_SDK.mNetTag, Util_Network_HttpUtil.class, "statucCode:%d", statusCode);
				}

				if ((statusCode == HttpStatus.SC_MOVED_PERMANENTLY) || (statusCode == HttpStatus.SC_MOVED_TEMPORARILY)
						|| (statusCode == HttpStatus.SC_SEE_OTHER) || (statusCode == HttpStatus.SC_TEMPORARY_REDIRECT)) {
					// 此处重定向处理
					return true;
				}
				return false;
			}

			@Override
			public URI getLocationURI(HttpResponse response, HttpContext context) throws ProtocolException {

				// headers are case-insenstive
				Header header = response.getFirstHeader("location");
				if (header == null) {
					return null;
				}

				String url = header.getValue();

				if (Debug_SDK.isNetLog) {
					Debug_SDK.td(Debug_SDK.mNetTag, Util_Network_HttpUtil.class, "loaction:%s", url);
				}

				if (url == null) {
					return null;
				}

				// 重定向链接
				if (linstener != null) {
					linstener.onUrlRedirect(url);
				}

				return URI.create(url);
			}
		});

		return httpClient;
	}

	public static boolean isHttpStatusOk(int httpCode) {
		if (httpCode >= 200 && httpCode < 300) {
			return true;
		}
		return false;
	}

	/**
	 * 判断两个url的路径是否相同
	 * 
	 * @param urlA
	 * @param urlB
	 * @return
	 */
	public static boolean isUrlMatchWithPath(String urlA, String urlB) {
		try {

			if (urlA == null || urlB == null) {
				return false;
			}

			urlA = urlA.trim();
			urlB = urlB.trim();

			if (urlA.length() == 0 || urlB.length() == 0) {
				return false;
			}

			if (urlA.equalsIgnoreCase(urlB)) {
				return true;
			}

			// if(urlA.length()==urlB.length()){
			// return urlA.equalsIgnoreCase(urlB);
			// }

			URL uriA = new URL(urlA);
			URL uriB = new URL(urlB);

			// System.err.println(uriA.getPath());
			// System.err.println(uriB.getPath());

			String hostA = uriA.getHost();
			String hostB = uriB.getHost();
			// System.err.println(uriA.getHost());
			// System.err.println(uriB.getHost());
			//
			// System.err.println(uriA.getProtocol());
			// System.err.println(uriB.getProtocol());
			//
			// System.err.println(uriA.getPort());
			// System.err.println(uriB.getPort());

			if ((hostA.equals(hostB)) && (uriA.getProtocol().equals(uriB.getProtocol()))
					&& (uriA.getPort() == uriB.getPort())) {

				String pathA = uriA.getPath();
				String pathB = uriB.getPath();
				if (pathA.equalsIgnoreCase(pathB)) {
					return true;
				}

				if (pathA.length() == pathB.length()) {
					// 长度一致，但内容不一致
					return false;
				}

				pathA = pathA.replace('/', ' ').trim();
				pathB = pathB.replace('/', ' ').trim();

				if (pathA.equalsIgnoreCase(pathB)) {
					return true;
				}
			}

		} catch (Throwable e) {
			if (Debug_SDK.isNetLog) {
				Debug_SDK.te(Debug_SDK.mNetTag, Util_Network_HttpUtil.class, e);
			}
		}
		return false;
	}

	/**
	 * 判断指定的url是不是在某个url列表上面
	 * 
	 * @param urls
	 *            多个url的字符串，用逗号分隔
	 * @param url
	 *            指定的url，可带查询串，但最终判断时会去掉查询串
	 * @return
	 */
	public static boolean isUrlsContainsWithDestUrl(String urls, String url) {
		try {

			if (urls == null || url == null) {
				return false;
			}

			urls = urls.trim();
			url = url.trim();

			if (urls.length() == 0 || url.length() == 0) {
				return false;
			}

			if (urls.equalsIgnoreCase(url)) {
				return true;
			}

			Uri uriDestUrl = Uri.parse(url);

			String destUrlPath = uriDestUrl.getPath();

			if (urls.contains(destUrlPath) && urls.contains(uriDestUrl.getHost())) {
				return true;
			}

		} catch (Throwable e) {
			if (Debug_SDK.isNetLog) {
				Debug_SDK.te(Debug_SDK.mNetTag, Util_Network_HttpUtil.class, e);
			}
		}
		return false;
	}

	/**
	 * 获取目标文件的长度
	 * 
	 * @param context
	 * @param url
	 * @return
	 */
	public static long getContentLength(Context context, String url) {
		DefaultHttpClient client = null;
		HttpGet get = null;
		try {
			if (url == null) {
				return -1;
			}

			client = Util_Network_HttpUtil.createHttpClient(context);

			get = new HttpGet(url);

			HttpResponse response = client.execute(get);
			int code = response.getStatusLine().getStatusCode();
			if (code >= 200 && code < 300) {
				return response.getEntity().getContentLength();
			}

		} catch (Throwable e) {
			if (Debug_SDK.isNetLog) {
				Debug_SDK.te(Debug_SDK.mNetTag, Util_Network_HttpUtil.class, e);
			}
		} finally {
			try {
				if (get != null) {
					get.abort();
				}
			} catch (Throwable e) {
				if (Debug_SDK.isNetLog) {
					Debug_SDK.te(Debug_SDK.mNetTag, Util_Network_HttpUtil.class, e);
				}
			}

			try {
				if (client != null) {
					client.getConnectionManager().shutdown();
				}
			} catch (Throwable e) {
				if (Debug_SDK.isNetLog) {
					Debug_SDK.te(Debug_SDK.mNetTag, Util_Network_HttpUtil.class, e);
				}
			}
		}
		return -1;
	}

}
