package com.zyq.android.myapplication;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import com.flurry.android.FlurryAgent;

import java.util.HashMap;
import java.util.Map;

/**
 * @author zyq 16-3-30
 */
public class TestService extends Service {

	@Override
	public void onCreate() {
		super.onCreate();
		Log.d("FlurryAgent","服务已开启！");
		FlurryAgent.onStartSession(this);

		Map<String,String> map = new HashMap<>();
		map.put("hello",String.valueOf(true));
		FlurryAgent.logEvent("test2",map);
	}

	@Nullable
	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		FlurryAgent.onEndSession(this);
	}
}
