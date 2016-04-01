package net.youmi.android.libs.common.coder;

import net.youmi.android.libs.common.debug.Debug_SDK;

import java.net.URLDecoder;
import java.net.URLEncoder;

public class Coder_UrlCoder {

	/**
	 * urldecode
	 * 
	 * @param src
	 * @return
	 */
	public static String urlDecode(String src) {
		try {
			if (src.indexOf("%20") > -1) {
				src = src.replace("%20", "+");
			}
			String str = URLDecoder.decode(src, "UTF-8");
			return str;
		} catch (Throwable e) {
			if (Debug_SDK.isCoderLog) {
				Debug_SDK.te(Debug_SDK.mCoderTag, Coder_UrlCoder.class, e);
			}
			return "";
		}
	}

	/**
	 * urlencode
	 * 
	 * @param src
	 * @return
	 */
	public static String urlEncode(String src) {
		try {

			String str = URLEncoder.encode(src, "UTF-8");

			if (str.indexOf("+") > -1) {
				str = str.replace("+", "%20");
			}
			return str;
		} catch (Throwable e) {
			if (Debug_SDK.isCoderLog) {
				Debug_SDK.te(Debug_SDK.mCoderTag, Coder_UrlCoder.class, e);
			}
			return "";
		}
	}
}
