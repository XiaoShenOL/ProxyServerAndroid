package com.android.sms.proxy.function;

import java.util.Calendar;

/**
 * @author zyq 16-3-24
 */
public class MiscUtils {

	/*
     * Converts logcat timstamp to SQL friendly timstamps
     * We use this to determine if an sms has already been found
     *
     * Converts a timstamp in this format:     06-17 22:06:05.988 D/dalvikvm(24747):
     * Returns a timestamp in this format:     20150617223311
     */
	public static String logcatTimeStampParser(String line) {
		String[] buffer = line.split(" ");

		line = String.valueOf(Calendar.getInstance().get(Calendar.YEAR)) + buffer[0] + buffer[1];
		//   We don't need the last 4 digits in timestamp ".988" or it is too accurate.
		return line.substring(0, line.length() - 4)
				.replace(":", "")
				.replace(".", "")
				.replace("-", "");
	}
}
