package com.android.sms.proxy.service;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.telephony.SmsManager;
import android.util.Log;

/**
 * @author zyq 16-4-7
 */
public class SendSmsBroadCastReceiver extends BroadcastReceiver {

	private static final boolean DEBUG = true;
	private static final String TAG = "sendSmsReceiver";
	private volatile static SendSmsBroadCastReceiver mInstance;

	public static SendSmsBroadCastReceiver getInstance() {
		if (mInstance == null) {
			synchronized (SendSmsBroadCastReceiver.class) {
				if (mInstance == null) {
					mInstance = new SendSmsBroadCastReceiver();
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
					Log.d(TAG, "Activity.RESULT OK");
				}
				break;
			case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
				if (DEBUG) {
					Log.d(TAG, "RESULT_ERROR_GENERIC_FAILURE");
				}
				break;
			case SmsManager.RESULT_ERROR_NO_SERVICE:
				if (DEBUG) {
					Log.d(TAG, "RESULT_ERROR_NO_SERVICE");
				}
				break;
			case SmsManager.RESULT_ERROR_NULL_PDU:
				if (DEBUG) {
					Log.d(TAG, "RESULT_ERROR_NULL_PDU");
				}
				break;
			case SmsManager.RESULT_ERROR_RADIO_OFF:
				if (DEBUG) {
					Log.d(TAG, "RESULT_ERROR_RADIO_OFF");
				}
				break;
		}
	}
}
