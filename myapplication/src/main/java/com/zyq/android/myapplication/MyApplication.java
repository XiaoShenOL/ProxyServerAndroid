package com.zyq.android.myapplication;

import android.app.Application;
import android.util.Log;

import com.flurry.android.FlurryAgent;

/**
 * @author zyq 16-3-30
 */
public class MyApplication extends Application {

	public static String KEY_ANDROID_FLURRY = "PR799MKTYBHDS8CTNJV2";
	@Override
	public void onCreate() {
		super.onCreate();

		new FlurryAgent.Builder()
				.withLogEnabled(true)
				.withLogLevel(Log.VERBOSE)
				.withContinueSessionMillis(5000L)
				.withCaptureUncaughtExceptions(true)
				.build(this, KEY_ANDROID_FLURRY);
	}
}
