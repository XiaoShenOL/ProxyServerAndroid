package net.youmi.android.libs.common.network;

import net.youmi.android.libs.common.debug.Debug_SDK;
import net.youmi.android.libs.common.dns.Message;
import net.youmi.android.libs.common.dns.SimpleResolver;
import net.youmi.android.libs.common.global.Global_Charsets;

import org.apache.http.Header;
import org.apache.http.HeaderElement;
import org.apache.http.NameValuePair;
import org.apache.http.ParseException;

import java.io.IOException;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * http请求的相关数据
 * 
 * @author jen created on 2013-5-19
 * @author zhitaocai edit on 2014-5-14
 * @author huangjianbin edit on 2014-7-10
 * @author zhitaocai edit on 2014-8-18 添加post 二进制数据
 * @category 数据模型
 */
public class Net_HttpRequester {

	/**
	 * 请求url
	 */
	protected String mRequestUrl;

	/**
	 * 是否在logcat上显示请求信息
	 */
	protected boolean mIsShowReqMsgInLog;

	/**
	 * post data ,如果为空，看看是不是需要二进制post，如果都不用就，就使用get方法请求
	 */
	protected List<NameValuePair> mPostData;

	/**
	 * post data , post二进制数据
	 */
	protected byte[] mPostDataByteArray;

	/**
	 * 请求包头
	 */
	protected List<Header> mRequestHeaders;

	protected String host;

	protected InetAddress addr;

	/**
	 * 编码格式
	 */
	private String mEncodingCharset;

	public boolean isShowReqMsgInLog() {
		return mIsShowReqMsgInLog;
	}

	public void setShowReqMsgInLog(boolean showReqMsgInLog) {
		mIsShowReqMsgInLog = showReqMsgInLog;
	}

	public List<NameValuePair> getPostDataNameValuePair() {
		return mPostData;
	}

	public void setPostDataNameValuePair(List<NameValuePair> postData) {
		this.mPostData = postData;
	}

	public List<Header> getRequestHeaders() {
		return mRequestHeaders;
	}

	public void setRequestHeaders(List<Header> requestHeaders) {
		mRequestHeaders = requestHeaders;
	}

	public String getEncodingCharset() {
		if (mEncodingCharset == null) {
			return Global_Charsets.UTF_8;
		}
		return mEncodingCharset;
	}

	public void setEncodingCharset(String encoding) {
		mEncodingCharset = encoding;
	}

	public String getRequestUrl() {
		return mRequestUrl;
	}

	public void setRequestUrl(String requestUrl) {
		mRequestUrl = requestUrl;
	}

	/**
	 * 通过请求获取服务器IP
	 * 
	 * @return
	 */
	public String getHostIp() {
		host = catchHost(mRequestUrl);
		Map<String, List<String>> map = new HashMap<String, List<String>>();
		String result = "";
		try {
			InetAddress[] ipArray = InetAddress.getAllByName(host);
			for (InetAddress ip : ipArray) {
				if (ip instanceof Inet4Address) {
					if (!(map.containsKey("ipv4"))) {
						List<String> list = new ArrayList<String>();
						map.put("ipv4", list);
					}
					map.get("ipv4").add(ip.getHostAddress());
				} else if (ip instanceof Inet6Address) {
					if (!(map.containsKey("ipv6"))) {
						List<String> list = new ArrayList<String>();
						map.put("ipv6", list);
					}
					map.get("ipv6").add(ip.getHostAddress());
				}
			}
			result = handlerHostIp(map);
		} catch (Exception e) {
			if (Debug_SDK.isNetLog) {
				Debug_SDK.te(Debug_SDK.mNetTag, this, e);
			}
			return null;
		}
		return result;
	}

	private String handlerHostIp(Map<String, List<String>> map) {
		StringBuilder result = new StringBuilder();
		if (map == null) {
			result.append("can not get host ip");
			return result.toString();
		}
		if (map.containsKey("ipv4")) {
			for (String ip : map.get("ipv4")) {
				result.append(ip + ";");
			}
		}
		if (map.containsKey("ipv6")) {
			for (String ip : map.get("ipv6")) {
				result.append(ip + ";");
			}
		}
		return result.toString();

	}

	private String catchHost(String req) {
		if (req != null) {
			String host = "";
			host = req.replace("http://", "");
			int end = host.indexOf("/");
			host = host.substring(0, end);
			return host;
		}
		return null;
	}

	/**
	 * 通过DNS解析服务器直接获取IP
	 */
	public boolean getByName() {
		try {
			SimpleResolver sr = new SimpleResolver();
			Message query = new Message(host);
			addr = null;
			try {
				addr = InetAddress.getByAddress(sr.send(query).getAddr());
				Header parcelHeader = new Header() {

					@Override
					public String getValue() {
						return host;
					}

					@Override
					public String getName() {
						return "host";
					}

					@Override
					public HeaderElement[] getElements() throws ParseException {
						return null;
					}
				};
				if (!mRequestHeaders.contains(parcelHeader)) {
					mRequestHeaders.add(parcelHeader);
				}
				if (addr != null) {
					String ip = addr.getHostAddress();
					mRequestUrl = mRequestUrl.replaceFirst(host, ip);
					if (Debug_SDK.isDebug && Debug_SDK.isNetLog) {
						Debug_SDK.td(Debug_SDK.mNetTag, this, mRequestUrl);
					}
				}
			} catch (IOException e) {
				if (Debug_SDK.isNetLog) {
					Debug_SDK.te(Debug_SDK.mNetTag, this, e);
				}
			}

		} catch (UnknownHostException e) {
			if (Debug_SDK.isNetLog) {
				Debug_SDK.te(Debug_SDK.mNetTag, this, e);
			}
		}
		if (addr != null)
			return true;
		return false;
	}

	/**
	 * 添加设置post二进制文件
	 * 
	 * @param bytes
	 */
	public void setPostDataByteArray(byte[] bytes) {
		mPostDataByteArray = bytes;
	}

	public byte[] getPostDataByteArray() {
		return mPostDataByteArray;
	}
}
