package com.android.sms.proxy.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.ServiceCompat;
import android.util.Log;

import com.android.sms.proxy.WatchDog;
import com.android.sms.proxy.entity.MessageEvent;
import com.droidwolf.nativesubprocess.Subprocess;

import org.greenrobot.eventbus.EventBus;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * @author zyq 16-3-9
 */
public class HeartBeatService extends Service {

	private static final boolean DEBUG = true;
	private static final String TAG = "heartBeatService";
	private ScheduledExecutorService mExecutorService;
	private ScheduledFuture mScheduledFuture;
	private static final long MESSAGE_INIT_DELAY = 2;//Message 推送延迟
	private static final long MESSAGE_DELAY = 25;//Message 轮询消息

	@Override
	public void onCreate() {
		super.onCreate();
		Log.d(TAG, "service onCreate()");
		Subprocess.create(getApplicationContext(), WatchDog.class);
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		scheduledWithFixedDelay();
		return ServiceCompat.START_STICKY;
	}

	public void scheduledWithFixedDelay() {
		if (DEBUG) {
			Log.d(TAG, "开始发心跳包");
		}
		EventBus.getDefault().postSticky(new MessageEvent("开始发心跳包"));
		mExecutorService = Executors.newScheduledThreadPool(1);
		HeartBeatRunnable heartBeatRunnable = new HeartBeatRunnable(this);
		mScheduledFuture = mExecutorService.scheduleAtFixedRate(heartBeatRunnable, MESSAGE_INIT_DELAY, MESSAGE_DELAY,
				TimeUnit.SECONDS);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		if (mScheduledFuture != null) {
			if (!mScheduledFuture.isCancelled()) {
				mScheduledFuture.cancel(false);
				EventBus.getDefault().postSticky(new MessageEvent("service 退出"));
			}
		}
	}

	@Nullable
	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}
}
