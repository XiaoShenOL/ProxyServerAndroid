package com.android.sms.client;

import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;

/**
 * @author zyq 16-3-22
 */
public class GetRemotePortService extends BaseHeartService<GetRemotePortRunnable> {

	@Override
	public void onCreate() {
		super.onCreate();
	}

	@Nullable
	@Override
	public IBinder onBind(Intent intent) {
		return super.onBind(intent);
	}

	@Override
	public void onRunnableInit() {
		t = new GetRemotePortRunnable(this);
	}

	@Override
	public void onRunnableDestroy() {

	}
}
