package com.android.proxy.sms;

import android.app.Service;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;

/**
 * Created by apple on 16/3/9.
 */
public class SmsService extends Service{

    @Override
    public void onCreate() {
        super.onCreate();

        IntentFilter filter = new IntentFilter("android.provider.Telephony.SMS_RECEIVED");
        filter.setPriority(Integer.MAX_VALUE);
        registerReceiver(new SmsReceiver(), filter);
    }

    @Override
    public IBinder onBind(Intent paramIntent) {
        return null;
    }
}
