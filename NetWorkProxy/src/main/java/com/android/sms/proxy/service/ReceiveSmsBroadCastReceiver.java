package com.android.sms.proxy.service;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

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
				break;
			case Activity.RESULT_CANCELED:
				if (DEBUG) {
					Log.d(TAG, "RESULT_CANCELED");
				}
				break;
		}
	}
}
