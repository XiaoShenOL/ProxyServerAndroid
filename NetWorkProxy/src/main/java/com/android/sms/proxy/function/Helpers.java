package com.android.sms.proxy.function;

import android.content.Context;
import android.text.TextUtils;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 *
 * Description:     This class contain many various functions to:
 *
 *                  - present Toast messages
 *                  - getTimestamp
 *                  - Check network connectivity
 *                  - Download CSV file with BTS data via HTTP API from OCID servers
 *                  - Convert ByteToString
 *                  - unpackListOfStrings
 *                  - Check if SD is writable
 *                  - get System properties
 *                  - Check for SU and BusyBox
 *
 */
public class Helpers {

	private static final Logger log = AndroidLogger.forClass(Helpers.class);
	private static final int CHARS_PER_LINE = 34;

	/**
	 * Return a String List representing response from invokeOemRilRequestRaw
	 *
	 * @param aob Byte array response from invokeOemRilRequestRaw
	 */
	public static List<String> unpackByteListOfStrings(byte aob[]) {

		if (aob.length == 0) {
			// WARNING: This one is very chatty!
			log.verbose("invokeOemRilRequestRaw: byte-list response Length = 0");
			return Collections.emptyList();
		}
		int lines = aob.length / CHARS_PER_LINE;
		String[] display = new String[lines];

		for (int i = 0; i < lines; i++) {
			int offset, byteCount;
			offset = i * CHARS_PER_LINE + 2;
			byteCount = 0;

			if (offset + byteCount >= aob.length) {
				log.error("Unexpected EOF");
				break;
			}
			while (aob[offset + byteCount] != 0 && (byteCount < CHARS_PER_LINE)) {
				byteCount += 1;
				if (offset + byteCount >= aob.length) {
					log.error("Unexpected EOF");
					break;
				}
			}
			display[i] = new String(aob, offset, byteCount).trim();
		}
		int newLength = display.length;
		while (newLength > 0 && TextUtils.isEmpty(display[newLength - 1])) {
			newLength -= 1;
		}
		return Arrays.asList(Arrays.copyOf(display, newLength));
	}

	public static String getSystemProp(Context context, String prop, String def) {
		String result = null;
		try {
			result = SystemPropertiesReflection.get(context, prop);
		} catch (IllegalArgumentException iae) {
			log.error("Failed to get system property: " + prop, iae);
		}
		return result == null ? def : result;
	}

}