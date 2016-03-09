package com.android.sms.proxy.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import com.droidwolf.libandmon;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;

/**
 * @author zyq 16-3-9
 */
public class HeartBeatService extends Service {

	private static final boolean DEBUG = true;
	private static final String TAG = "heartBeatService";
	private ScheduledExecutorService mExecutorService;
	private HeartBeatCallable mHeartBeatCallable;
	private ScheduledFuture mScheduledFuture;

	@Override
	public void onCreate() {
		super.onCreate();
		Log.d(TAG, "service onCreate()");
		new Thread(new Runnable() {
			@Override
			public void run() {
				libandmon.start(HeartBeatService.this,android.os.Process.myPid(),HeartBeatService.class,"",true);
			}
		}).start();
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		return super.onStartCommand(intent, flags, startId);
	}

	public void scheduledWithFixedDelay(){
		if(DEBUG){
			Log.d(TAG,"开始发心跳包");
		}
		mExecutorService = Executors.newScheduledThreadPool(1);
	}


	@Nullable
	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}
}
