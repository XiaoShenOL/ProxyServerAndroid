package net.youmi.android.libs.common.debug;

import android.util.Log;

public class Debug_SDK_Log_Temp extends Debug_SDK_Log {

	private static String createDefaultTag() {
		return "Test";
	}

	public static final String mTag = createDefaultTag();// YoumiLib

	// //////////////////////////
	// i

	public static void i(String fmt, Object... args) {
		if (isDebug) {
			printDebugInfo(Log.INFO, mTag, null, fmt, args);
		}
	}

	public static void i(String msg, Throwable tr) {
		if (isDebug) {
			printDebugInfo(Log.INFO, mTag, tr, msg);
		}
	}

	public static void i(Throwable tr) {
		if (isDebug) {
			printDebugInfo(Log.INFO, mTag, tr, null);
		}
	}

	public static void di(String tag, String fmt, Object... args) {
		if (isDebug) {
			printDebugInfo(Log.INFO, tag, null, fmt, args);
		}
	}

	public static void di(String tag, String msg, Throwable tr) {
		if (isDebug) {
			printDebugInfo(Log.INFO, tag, tr, msg);
		}
	}

	public static void di(String tag, Throwable tr) {
		if (isDebug) {
			printDebugInfo(Log.INFO, tag, tr, null);
		}
	}

	// //////////////////////////
	// e
	public static void e(String fmt, Object... args) {
		if (isDebug) {
			printDebugInfo(Log.ERROR, mTag, null, fmt, args);
		}
	}

	public static void e(String msg, Throwable tr) {
		if (isDebug) {
			printDebugInfo(Log.ERROR, mTag, tr, msg);
		}
	}

	public static void e(Throwable tr) {
		if (isDebug) {
			printDebugInfo(Log.ERROR, mTag, tr, null);
		}
	}

	public static void de(String tag, String fmt, Object... args) {
		if (isDebug) {
			printDebugInfo(Log.ERROR, tag, null, fmt, args);
		}
	}

	public static void de(String tag, String msg, Throwable tr) {
		if (isDebug) {
			printDebugInfo(Log.ERROR, tag, tr, msg);
		}
	}

	public static void de(String tag, Throwable tr) {
		if (isDebug) {
			printDebugInfo(Log.ERROR, tag, tr, null);
		}
	}

	public static void de(String msg) {
		if (isDebug) {
			printDebugInfo(Log.ERROR, mTag, null, msg);
		}
	}

	public static void de(Throwable tr) {
		if (isDebug) {
			printDebugInfo(Log.ERROR, mTag, tr, null);
		}
	}

	// //////////////////////////
	// d
	public static void d(String fmt, Object... args) {
		if (isDebug) {
			printDebugInfo(Log.DEBUG, mTag, null, fmt, args);
		}
	}

	public static void d(String msg, Throwable tr) {
		if (isDebug) {
			printDebugInfo(Log.DEBUG, mTag, tr, msg);
		}
	}

	public static void d(Throwable tr) {
		if (isDebug) {
			printDebugInfo(Log.DEBUG, mTag, tr, null);
		}
	}

	public static void dd(String tag, String fmt, Object... args) {
		if (isDebug) {
			printDebugInfo(Log.DEBUG, tag, null, fmt, args);
		}
	}

	public static void dd(String tag, String msg, Throwable tr) {
		if (isDebug) {
			printDebugInfo(Log.DEBUG, tag, tr, msg);
		}
	}

	public static void dd(String tag, Throwable tr) {
		if (isDebug) {
			printDebugInfo(Log.DEBUG, tag, tr, null);
		}
	}

	// //////////////////////////
	// w
	public static void w(String fmt, Object... args) {
		if (isDebug) {
			printDebugInfo(Log.WARN, mTag, null, fmt, args);
		}
	}

	public static void w(String msg, Throwable tr) {
		if (isDebug) {
			printDebugInfo(Log.WARN, mTag, tr, msg);
		}
	}

	public static void w(Throwable tr) {
		if (isDebug) {
			printDebugInfo(Log.WARN, mTag, tr, null);
		}
	}

	public static void dw(String tag, String fmt, Object... args) {
		if (isDebug) {
			printDebugInfo(Log.WARN, tag, null, fmt, args);
		}
	}

	public static void dw(String tag, String msg, Throwable tr) {
		if (isDebug) {
			printDebugInfo(Log.WARN, tag, tr, msg);
		}
	}

	public static void dw(String tag, Throwable tr) {
		if (isDebug) {
			printDebugInfo(Log.WARN, tag, tr, null);
		}
	}

	// //////////////////////////
	// v
	public static void v(String fmt, Object... args) {
		if (isDebug) {
			printDebugInfo(Log.VERBOSE, mTag, null, fmt, args);
		}
	}

	public static void v(String msg, Throwable tr) {
		if (isDebug) {
			printDebugInfo(Log.VERBOSE, mTag, tr, msg);
		}
	}

	public static void v(Throwable tr) {
		if (isDebug) {
			printDebugInfo(Log.VERBOSE, mTag, tr, null);
		}
	}

	public static void dv(String tag, String fmt, Object... args) {
		if (isDebug) {
			printDebugInfo(Log.VERBOSE, tag, null, fmt, args);
		}
	}

	public static void dv(String tag, String msg, Throwable tr) {
		if (isDebug) {
			printDebugInfo(Log.VERBOSE, tag, tr, msg);
		}
	}

	public static void dv(String tag, Throwable tr) {
		if (isDebug) {
			printDebugInfo(Log.VERBOSE, tag, tr, null);
		}
	}

}
