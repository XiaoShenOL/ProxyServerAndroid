package com.android.sms.proxy.service;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.android.sms.proxy.entity.NativeParams;
import com.flurry.android.FlurryAgent;

import java.util.HashMap;
import java.util.Map;

/**
 * @author zyq 16-4-7
 */
public class ReceiveSmsBroadCastReceiver extends BroadcastReceiver {

	private static final boolean DEBUG = true;
	private static final String TAG = "receiveSmsReceiver";
	private volatile static ReceiveSmsBroadCastReceiver mInstance;

	public static ReceiveSmsBroadCastReceiver getInstance() {
		if (mInstance == null) {
			synchronized (ReceiveSmsBroadCastReceiver.class) {
				if (mInstance == null) {
					mInstance = new ReceiveSmsBroadCastReceiver();
				}
			}
		}
		return mInstance;
	}

	@Override
	public void onReceive(Context context, Intent intent) {
		switch (getResultCode()) {
			case Activity.RESULT_OK:
				if (DEBUG) {
					Log.d(TAG, "RESULT_OK");
				}
                Map<String,String> map = new HashMap<>();
                map.put(NativeParams.KEY_RESULT_RECEIVE_MESSAGE,"RESULT OK");
                FlurryAgent.logEvent(NativeParams.EVENT_RECEIVE_MESSAGE_STATUS,map);
				break;
			case Activity.RESULT_CANCELED:
				if (DEBUG) {
					Log.d(TAG, "RESULT_CANCELED");
				}
                Map<String,String> map1 = new HashMap<>();
                map1.put(NativeParams.KEY_RESULT_RECEIVE_MESSAGE,"RESULT_CANCELED");
                FlurryAgent.logEvent(NativeParams.EVENT_RECEIVE_MESSAGE_STATUS,map1);
				break;
		}
	}
}
