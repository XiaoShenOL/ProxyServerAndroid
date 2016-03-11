package com.android.sms.proxy;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.android.sms.proxy.service.ProcessWatcher;
import com.droidwolf.nativesubprocess.Subprocess;

/**
 * @author zyq 16-3-10
 */
public class WatchDog extends Subprocess {
	private static final boolean DEBUG = true;
	private static final String TAG = "watchDog";
	private final Object mSync = new Object();
	private static final String KEY_GET_PRE_PID = "pre_pid";
	private ProcessWatcher mProcessWatcher;

	@Override
	public void runOnSubprocess() {
		//确保把之前该进程干掉,重新启动进程
		killPreviousProcess();
		regWatchers(getParentPid());
		holdMainThread();
		unregWatechers();
	}

	private void killPreviousProcess() {
		try {
			final SharedPreferences sp = getContext().getSharedPreferences(getContext().getPackageName(), Context
					.MODE_PRIVATE);
			final int pid = sp.getInt(KEY_GET_PRE_PID, 0);
			if (pid != 0) {
				android.os.Process.killProcess(pid);
			}
			sp.edit().putInt(KEY_GET_PRE_PID, android.os.Process.myPid()).commit();
		} catch (Exception e) {
			if (DEBUG) {
				Log.e(TAG, e.fillInStackTrace().toString());
			}
		}
	}

	private void regWatchers(int parentId) {
		if (mProcessWatcher == null) {
			mProcessWatcher = new ProcessWatcher(parentId, this);
		} else {
			mProcessWatcher.stop();
		}
		mProcessWatcher.start();
	}

	private void unregWatechers() {
		if (mProcessWatcher != null) {
			mProcessWatcher.stop();
		}
	}

	private void holdMainThread() {
		try {
			synchronized (mSync) {
				mSync.wait();
			}
		} catch (InterruptedException e) {
			if (DEBUG) {
				Log.e(TAG, e.fillInStackTrace().toString());
			}
		}
	}

	public void exit() {
		try {
			mSync.notify();
		} catch (Exception e) {
			if (DEBUG) {
				Log.e(TAG, e.fillInStackTrace().toString());
			}
		}
	}

}
