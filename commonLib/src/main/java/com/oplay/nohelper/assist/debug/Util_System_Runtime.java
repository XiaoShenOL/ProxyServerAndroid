package com.oplay.nohelper.assist.debug;

import android.os.Handler;
import android.os.Looper;

/**
 * @author zyq 16-3-9
 */
public class Util_System_Runtime {

	private static Util_System_Runtime mInstance;
	private Handler mUiHandler;

	private Util_System_Runtime() {
		mUiHandler = new Handler(Looper.getMainLooper());
	}

	public synchronized static Util_System_Runtime getInstance() {
		try {
			if (mInstance == null) {
				mInstance = new Util_System_Runtime();
			}
		} catch (Throwable e) {
			if (Debug_SDK.isUtilLog) {
				Debug_SDK.te(Debug_SDK.mUtilTag, Util_System_Runtime.class, e);
			}
		}
		return mInstance;
	}

	public static boolean isInUIThread() {
		try {
			return Looper.myLooper() == Looper.getMainLooper();
		} catch (Throwable e) {
			if (Debug_SDK.isUtilLog) {
				Debug_SDK.te(Debug_SDK.mUtilTag, Util_System_Runtime.class, e);
			}
		}
		return false;
	}

	public boolean runInUiThread(Runnable runnable) {
		try {
			if (runnable == null) {
				return false;
			}
			return mUiHandler.post(runnable);
		} catch (Throwable e) {
			if (Debug_SDK.isUtilLog) {
				Debug_SDK.te(Debug_SDK.mUtilTag, Util_System_Runtime.class, e);
			}
		}
		return false;
	}

	/**
	 * 延迟指定时间之后执行任务
	 *
	 * @param runnable
	 *            要执行的任务
	 * @param delayTime_ms
	 *            要延迟的毫秒数
	 * @return
	 */
	public boolean runInUiThreadDelayed_ms(Runnable runnable, long delayTime_ms) {
		try {
			if (runnable == null) {
				return false;
			}
			return mUiHandler.postDelayed(runnable, delayTime_ms);
		} catch (Throwable e) {
			if (Debug_SDK.isUtilLog) {
				Debug_SDK.te(Debug_SDK.mUtilTag, Util_System_Runtime.class, e);
			}
		}
		return false;
	}
}
