package net.youmi.android.libs.common.debug;

public class Debug_TimeCounter {

	// public static final boolean isEnableCounter = false;
	//
	// private static final String tag = "SDKCounter";
	//
	// // private static long markTime = 0;
	//
	// private static Hashtable<String, Long> logTimes = new Hashtable<String, Long>();
	//
	// public static String markNowByTime() {
	//
	// try {
	//
	// long markTime = System.currentTimeMillis();
	//
	// String key = String.valueOf(markTime);
	//
	// markNowByKey(key);
	//
	// return key;
	//
	// } catch (Throwable e) {
	// // handle exception
	// }
	// return null;
	// }
	//
	// public static void markNowByKey(String key) {
	// try {
	// if (logTimes == null) {
	// logTimes = new Hashtable<String, Long>();
	// }
	//
	// long markTime = System.currentTimeMillis();
	//
	// logTimes.put(key, markTime);
	//
	// } catch (Throwable e) {
	// // handle exception
	// }
	// }
	//
	// public static void markNowByThreadID() {
	//
	// try {
	//
	// long tid = Thread.currentThread().getId();
	//
	// markNowByKey(String.valueOf(tid));
	//
	// // Log.i(tag, "ThreadID:" + tid);
	//
	// } catch (Throwable e) {
	// // handle exception
	// }
	//
	// }
	//
	// public static void showTimeCounterLogByKey(String key, String msg) {
	// try {
	// if (logTimes == null) {
	// logTimes = new Hashtable<String, Long>();
	// }
	//
	// long markTime = 0;
	//
	// if (logTimes.containsKey(key)) {
	// markTime = logTimes.get(key);
	// }
	//
	// if (isEnableCounter) {
	//
	// long span = System.currentTimeMillis() - markTime;
	//
	// double span_s = ((double) span) / 1000;
	//
	// Log.d(tag, "花费时间:" + span_s + "秒\t[" + msg + "]");
	// }
	//
	// } catch (Throwable e) {
	// // handle exception
	// }
	//
	// }
	//
	// public static void showLog(String msg) {
	// try {
	// if (isEnableCounter) {
	// Log.d(tag, msg);
	// }
	// } catch (Throwable e) {
	// // handle exception
	// }
	// }
	//
	// public static void showTimeCounterLogByThread(String msg) {
	//
	// try {
	//
	// long tid = Thread.currentThread().getId();
	//
	// String key = String.valueOf(tid);
	//
	// showTimeCounterLogByKey(key, msg);
	//
	// } catch (Throwable e) {
	// // handle exception
	// }
	// }
	//
	// public static String toCounterBytesLenght(long len) {
	// try {
	//
	// return ((len > 1024) ? ((((float) len) / 1024) + "KB")
	// : (len + "B"));
	//
	// } catch (Throwable e) {
	// // handle exception
	// }
	//
	// return "" + len;
	// }
	//
	// public static String toCounterTimesLenght(long len) {
	// try {
	//
	// return ((len > 1000) ? ((((double) len) / 1000) + "秒")
	// : (len + "毫秒"));
	//
	// } catch (Throwable e) {
	// // handle exception
	// }
	//
	// return "" + len;
	// }
	//
	// // static void showTimeCounterLogAndReMarkNow(String msg) {
	// //
	// // try {
	// //
	// // if (logTimes == null) {
	// // logTimes = new Hashtable<String, Long>();
	// // }
	// //
	// // long tid = Thread.currentThread().getId();
	// //
	// // String key = String.valueOf(tid);
	// //
	// // long markTime = 0;
	// //
	// // if (logTimes.containsKey(key)) {
	// // markTime = logTimes.get(key);
	// // }
	// //
	// // if (Util_Debug_AdLog.isDebug && isEnableCounter) {
	// // Log.d(tag,
	// // "[" + key + "]" + msg + " 花费时间:"
	// // + (System.currentTimeMillis() - markTime));
	// // }
	// //
	// // markTime = System.currentTimeMillis();
	// //
	// // logTimes.put(key, markTime);
	// //
	// // } catch (Throwable e) {
	// // // handle exception
	// // }
	// // }
}
