package com.android.sms.proxy.service;

import android.content.Intent;
import android.os.FileObserver;
import android.util.Log;

import com.android.sms.proxy.WatchDog;

import java.io.File;

/**
 * @author zyq 16-3-10
 */
public class ProcessWatcher {
	private static final String TAG = "processWatcher";
	private static final boolean DEBUG = true;
	private FileObserver mFileObserver;
	private final String mPath;
	private final File mFile;
	private final WatchDog mWatchDog;

	public ProcessWatcher(int pid, WatchDog watchDog) {
		mPath = "/proc/" + pid;
		mFile = new File(mPath);
		mWatchDog = watchDog;
	}

	public void start() {
		if (mFileObserver == null) {
			mFileObserver = new MyFileObserver(mPath, FileObserver.CLOSE_NOWRITE);
		}
		mFileObserver.startWatching();
	}

	private void doSomething() {
		//重新启动该service;
		mWatchDog.getContext().startService(new Intent(mWatchDog.getContext(), HeartBeatService.class));
	}

	public void stop() {
		if (mFileObserver != null) {
			mFileObserver.stopWatching();
		}
	}

	private final class MyFileObserver extends FileObserver {

		private final Object mWaiter = new Object();

		private MyFileObserver(String path, int mask) {
			super(path, mask);
		}

		@Override
		public void onEvent(int event, String path) {
			if ((event & FileObserver.CLOSE_NOWRITE) == FileObserver.CLOSE_NOWRITE) {
				try {
					synchronized (mWaiter) {
						mWaiter.wait(3000);
					}
				} catch (InterruptedException e) {
					if (DEBUG) {
						Log.e(TAG, e.fillInStackTrace().toString());
					}
				}
				if (!mFile.exists()) {
					doSomething();
					stopWatching();
					mWatchDog.exit();
				}
			}
		}
	}


}
