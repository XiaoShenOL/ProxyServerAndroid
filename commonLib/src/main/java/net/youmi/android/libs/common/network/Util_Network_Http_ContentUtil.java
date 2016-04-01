package net.youmi.android.libs.common.network;

import android.net.Uri;

import net.youmi.android.libs.common.debug.Debug_SDK;

import org.apache.http.Header;
import org.apache.http.HttpResponse;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 从网络地址中提取出文件的相关描述信息
 */
public class Util_Network_Http_ContentUtil {

	static final Pattern fileNameFromContentDispositionPattern1 = Pattern.compile("filename=\"(.*?)\"");
	static final Pattern fileNameFromContentDispositionPattern2 = Pattern.compile("filename='(.*?)'");

	public static String getFileNameFromContentDisposition(String contentDisposition) {
		try {
			if (contentDisposition == null) {
				return null;
			}
			Matcher matcher1 = fileNameFromContentDispositionPattern1.matcher(contentDisposition);
			if (matcher1.find()) {
				return matcher1.group(matcher1.groupCount());
			}
			Matcher matcher2 = fileNameFromContentDispositionPattern2.matcher(contentDisposition);
			if (matcher2.find()) {
				return matcher2.group(matcher2.groupCount());
			}
		} catch (Throwable e) {
			if (Debug_SDK.isNetLog) {
				Debug_SDK.te(Debug_SDK.mNetTag, Util_Network_Http_ContentUtil.class, e);
			}
		}
		return null;
	}

	public static String getFileNameFromHttpResponse(HttpResponse response, String destUrl) {
		String fileName = null;
		try {
			// 提取文件名等等
			Header[] headers = response.getHeaders("Content-Disposition");

			// 从Content-Disposition中获取fileName
			if (headers != null) {
				if (headers.length > 0) {
					for (int i = 0; i < headers.length; i++) {
						Header header = headers[i];
						if (header != null) {
							fileName = getFileNameFromContentDisposition(header.getValue());
							if (fileName != null) {
								fileName = fileName.trim();
								if (fileName.length() > 0) {
									break;
								} else {
									fileName = null;
								}
							}
						}
					}
				}
			}

			if (fileName == null) {
				fileName = getFileNameFromHttpUrl(destUrl);
			}

		} catch (Throwable e) {
			if (Debug_SDK.isNetLog) {
				Debug_SDK.te(Debug_SDK.mNetTag, Util_Network_Http_ContentUtil.class, e);
			}
		}
		return fileName;
	}

	public static String getFileNameFromHttpUrl(String destUrl) {
		try {
			if (destUrl == null) {
				return null;
			}
			Uri uri = Uri.parse(destUrl);
			if (uri != null) {
				String path = uri.getPath();

				int index = path.lastIndexOf('/');
				if (index > -1) {
					return path.substring(index + 1);
				}
			}
		} catch (Throwable e) {
			if (Debug_SDK.isNetLog) {
				Debug_SDK.te(Debug_SDK.mNetTag, Util_Network_Http_ContentUtil.class, e);
			}
		}
		return null;
	}

	/**
	 * 从html中获取字符编码，用于对付顽固gb2312
	 * 
	 * @param html
	 * @return
	 */
	public static String getCharsetFromHtml(String html) {
		try {
			if (html == null) {
				return null;
			}
			// Pattern
			// pattern=Pattern.compile("<meta.*http-equiv=.*Content-Type.*content=.*text/html;.*charset=(.*?)\"|'.*/>",Pattern.CASE_INSENSITIVE);
			Pattern pattern = Pattern.compile("<meta.*content.*text/html;.*charset=(.*?)\"|'.*/>",
					Pattern.CASE_INSENSITIVE);// 保险一点的做法
			Matcher matcher = pattern.matcher(html);
			if (matcher.find()) {
				return matcher.group(matcher.groupCount());
			}
		} catch (Throwable e) {
			if (Debug_SDK.isNetLog) {
				Debug_SDK.te(Debug_SDK.mNetTag, Util_Network_Http_ContentUtil.class, e);
			}
		}
		return null;
	}
}
