package com.android.sms.client;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * @author zyq 16-3-22
 */
public abstract class BaseHeartService<T extends Runnable> extends Service {

	private static final boolean DEBUG = true;
	private static final String TAG = "heartBeatService";
	private ScheduledExecutorService mExecutorService;
	private ScheduledFuture mScheduledFuture;
	private static final long MESSAGE_INIT_DELAY = 2;//Message 推送延迟
	public static long MESSAGE_DELAY = 15;//Message 轮询消息
	protected T t = null;
	private String printMessage;


	@Override
	public void onCreate() {
        Log.d(TAG,"服务开始了");
		super.onCreate();
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		onRunnableInit();
		scheduledWithFixedDelay();
		return Service.START_STICKY;
	}

	@Nullable
	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}


	public void scheduledWithFixedDelay() {
		if(t == null) return;
		if (mScheduledFuture == null || mScheduledFuture.isCancelled()) {
			if (DEBUG) {
				Log.d(TAG, "开始发心跳包");
			}
		}
		if (mExecutorService == null) {
			mExecutorService = Executors.newScheduledThreadPool(2);
		}
		if (mScheduledFuture == null || mScheduledFuture.isCancelled()) {
			if(DEBUG){
				Log.d(TAG,"开始进行轮询操作．．．．．．．");
			}
			mScheduledFuture = mExecutorService.scheduleWithFixedDelay(t, MESSAGE_INIT_DELAY,
					MESSAGE_DELAY,
					TimeUnit.SECONDS);
		}
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		try {
			Log.d(TAG, "heartBeatService destroy()");
			onRunnableDestroy();
			if (mScheduledFuture != null) {
				cancelScheduledTasks();
			}
		} catch (Exception e) {
			if (DEBUG) {
				Log.e(TAG, e.fillInStackTrace().toString());
			}
		}
	}

	public void cancelScheduledTasks() {
		if (mScheduledFuture != null) {
			if (!mScheduledFuture.isCancelled()) {
				mScheduledFuture.cancel(true);
			}
		}
	}

	public abstract void onRunnableDestroy();

	public abstract void onRunnableInit();

}
