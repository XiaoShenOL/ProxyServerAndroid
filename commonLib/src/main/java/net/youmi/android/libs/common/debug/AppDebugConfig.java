package net.youmi.android.libs.common.debug;

import android.os.Debug;
import android.util.Log;

import java.lang.reflect.Field;

/**
 * App的测试配置项
 *
 * @author jen
 */
public class AppDebugConfig {

	/**
	 * debug模式，发布打包需要置为false，可以通过混淆让调试的log文本从代码文件中消除，避免被反编译时漏泄相关信息。
	 */
	public static final boolean IS_DEBUG = false;

	public static void logMethodName(Object object) {
		if (IS_DEBUG) {
			try {
				Log.v(getLogTag(object), getMethodName());
			} catch (Throwable e) {
				if (IS_DEBUG) {
					Debug_SDK.e(e);
				}
			}
		}
	}

	public static void logMethodName(Class<?> cls) {
		if (IS_DEBUG) {
			try {
				Log.v(getLogTag(cls), getMethodName());
			} catch (Throwable e) {
				if (IS_DEBUG) {
					Debug_SDK.e(e);
				}
			}
		}
	}

	private static String getLogTag(Object object) {
		if (object instanceof String) {
			return (String) object;
		} else if (object == null) {
			return "[Null]";
		}
		return object.getClass().getSimpleName() + "[" + object.hashCode() + "]";
	}

	private static String getMethodName() {
		final Thread current = Thread.currentThread();
		final StackTraceElement trace = current.getStackTrace()[4];
		return trace.getMethodName();
	}

	public static void logParams(String tag, Object... params) {
		if (IS_DEBUG) {
			for (Object obj : params) {
				Log.i(tag, "" + obj);
			}
		}
	}

	public static void logNetworkRequest(Object object, String request, String response) {
		if (IS_DEBUG) {
			Log.i(getLogTag(object), String.format("【Request】:%s", request));
			Log.i(getLogTag(object), String.format("【Response】:%s", response));
		}
	}

	public static void logFields(Class<?> classType) {
		if (IS_DEBUG) {
			try {
				final String name = classType.getSimpleName();
				final Field[] fs = classType.getDeclaredFields();
				for (Field f : fs) {
					Log.i(name, "Filed:" + f.getName());
				}
			} catch (Exception e) {
				if (IS_DEBUG) {
					Debug_SDK.e(e);
				}
			}
		}
	}

	public static void logMethodWithParams(Object object, Object... params) {
		if (IS_DEBUG) {
			try {
				final StringBuilder sb = new StringBuilder();
				sb.append("{").append(Thread.currentThread().getName()).append("}")
						.append(getMethodName()).append(":");
				for (Object obj : params) {
					sb.append('[').append(obj).append("], ");
				}
				Log.v(getLogTag(object), sb.toString());
			} catch (Exception e) {
				if (IS_DEBUG) {
					Debug_SDK.e(e);
				}
			}
		}
	}

	public static void logMemoryInfo() {
		if (IS_DEBUG) {
			try {
//            final ActivityManager activityManager = (ActivityManager) getActivity().getSystemService(Context
// .ACTIVITY_SERVICE);
//            activityManager.getMemoryClass();
				final String tag = "MM_INFO";
//            Log.i(tag, "Class " + activityManager.getMemoryClass());
				final long mb = 1024 * 1024l;
				//Get VM Heap Size by calling:
				Log.i(tag, "VM Heap Size:" + Runtime.getRuntime().totalMemory() / mb);

				// Get VM Heap Size Limit by calling:
				Log.i(tag, "VM Heap Size Limit:" + Runtime.getRuntime().maxMemory() / mb);

				// Get Allocated VM Memory by calling:
				Log.i(tag, "Allocated VM Memory:" + (Runtime.getRuntime().totalMemory() - Runtime.getRuntime()
						.freeMemory()) / mb);

				//Get Native Allocated Memory by calling:
				Log.i(tag, "Native Allocated Memory:" + Debug.getNativeHeapAllocatedSize() / mb);
			} catch (Exception e) {
				if (IS_DEBUG) {
					Debug_SDK.e(e);
				}
			}
		}

	}

}
