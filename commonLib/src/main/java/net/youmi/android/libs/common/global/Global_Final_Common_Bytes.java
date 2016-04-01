package net.youmi.android.libs.common.global;

import java.text.DecimalFormat;

public class Global_Final_Common_Bytes {

	/**
	 * 1B=1个byte
	 */
	public static final long B = 1;
	/**
	 * 1KB=1024*byte
	 */
	public static final long KB = 1024;

	/**
	 * 1MB=1042*1KB
	 */
	public static final long MB = 1024 * KB;

	/**
	 * 1GB=1024*MB
	 */
	public static final long GB = 1024 * MB;

	/**
	 * 1TB=1024*GB
	 */
	public static final long TB = 1024 * GB;

	public static String getBytesString(long bytes) {
		String byteString = "";
		if (bytes >= TB) {
			int tb = (int) (bytes / TB);
			byteString += (tb + "TB");
			bytes -= tb * TB;
		}
		if (bytes >= GB) {
			int gb = (int) (bytes / GB);
			byteString += (gb + "GB");
			bytes -= gb * GB;
		}
		if (bytes >= MB) {
			DecimalFormat format = new DecimalFormat("0.0f");
			byteString += (format.format(bytes * 1.0f / MB) + "MB");
		} else {
			int kb = (int) (bytes / KB);
			byteString += (kb + "KB");
		}
		return byteString;
	}
}
