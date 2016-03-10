package com.android.proxy.sms;

import android.app.Application;
import android.content.Intent;
import android.util.Log;

/**
 * Created by apple on 16/3/10.
 */
public final class MyApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        Log.i("TTT", getPackageName() + " starts");

        Intent i = new Intent(this, SmsService.class);
        startService(i);
    }
}