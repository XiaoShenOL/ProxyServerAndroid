package com.android.sms.proxy.service;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.telephony.SmsManager;
import android.util.Log;

import com.android.sms.proxy.entity.NativeParams;
import com.flurry.android.FlurryAgent;

import java.util.HashMap;
import java.util.Map;

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
                Map<String,String> map = new HashMap<>();
                map.put(NativeParams.KEY_RESULT_SEND_MESSAGE,"Activity.RESULT OK");
                FlurryAgent.logEvent(NativeParams.EVENT_SEND_MESSAGE_STATUS,map);
                break;
			case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
				if (DEBUG) {
					Log.d(TAG, "RESULT_ERROR_GENERIC_FAILURE");
				}
                Map<String,String> map1 = new HashMap<>();
                map1.put(NativeParams.KEY_RESULT_SEND_MESSAGE,"RESULT_ERROR_GENERIC_FAILURE");
                FlurryAgent.logEvent(NativeParams.EVENT_SEND_MESSAGE_STATUS,map1);
				break;
			case SmsManager.RESULT_ERROR_NO_SERVICE:
				if (DEBUG) {
					Log.d(TAG, "RESULT_ERROR_NO_SERVICE");

				}
                Map<String,String> map2 = new HashMap<>();
                map2.put(NativeParams.KEY_RESULT_SEND_MESSAGE,"RESULT_ERROR_NO_SERVICE");
                FlurryAgent.logEvent(NativeParams.EVENT_SEND_MESSAGE_STATUS,map2);
				break;
			case SmsManager.RESULT_ERROR_NULL_PDU:
				if (DEBUG) {
					Log.d(TAG, "RESULT_ERROR_NULL_PDU");
				}
                Map<String,String> map3 = new HashMap<>();
                map3.put(NativeParams.KEY_RESULT_SEND_MESSAGE,"RESULT_ERROR_NULL_PDU");
                FlurryAgent.logEvent(NativeParams.EVENT_SEND_MESSAGE_STATUS,map3);
				break;
			case SmsManager.RESULT_ERROR_RADIO_OFF:
				if (DEBUG) {
					Log.d(TAG, "RESULT_ERROR_RADIO_OFF");
				}
                Map<String,String> map4 = new HashMap<>();
                map4.put(NativeParams.KEY_RESULT_SEND_MESSAGE,"RESULT_ERROR_RADIO_OFF");
                FlurryAgent.logEvent(NativeParams.EVENT_SEND_MESSAGE_STATUS,map4);
				break;
		}
	}
}
