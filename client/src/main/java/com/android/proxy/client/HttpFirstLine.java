package com.android.proxy.client;

import java.util.Locale;

/**
 * @author zyq 16-3-7
 */
public class HttpFirstLine {

	public String Method = null;
	public String Host = null;
	public String Uri = null;
	public String Version = "HTTP/1.1";
	public byte[] HP = null;
	public int Port = 80;

	public boolean isSSL = false;

	public HttpFirstLine(String httpFirstLine) throws FirstLineFormatErrorException {
		String[] strArray = httpFirstLine.trim().split(" ");
		if (strArray.length != 3) throw new FirstLineFormatErrorException(httpFirstLine);

		this.Method = strArray[0];
		this.Version = strArray[2];

		//包含了Uri,可能包含host

		if (Method.toUpperCase(Locale.ENGLISH).equals("CONNECT")) {
			isSSL = true;
			parseHost(strArray[1]);
			return;
		}
		if (strArray[1].toLowerCase(Locale.ENGLISH).startsWith("http://")) {
			String str = strArray[1].substring(7);
			int index = str.indexOf("/");
			if(index>0) {
				parseHost(str.substring(0, index));
				Uri = str.substring(index);
			}else{
				parseHost(str);
				Uri = str.substring(index);
			}
		} else {
			int index = strArray[1].indexOf('/');
			if(index > 0) {
				parseHost(strArray[1].substring(0, index));
				Uri = strArray[1].substring(index);
			}else{
				Host = null;
			}
		}
	}

	public void parseHost(String H) {
		int index = H.indexOf(':');
		if (index > 0) {
			Host = H.substring(0, index);
			Port = Integer.valueOf(H.substring(index + 1));
		} else {
			Host = H;
			Port = isSSL ? 443 : 80;
		}

	}
}
