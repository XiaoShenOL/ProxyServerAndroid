package com.android.sms.proxy.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

/**
 * @author zyq 16-4-7
 */
public class ConnectivityBroadcastReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		if (!intent.getAction().equals(ConnectivityManager.CONNECTIVITY_ACTION)) return;
		// only switching profiles when needed
		ConnectivityManager manager = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo networkInfo = manager.getActiveNetworkInfo();

		if (networkInfo != null) {
			if (networkInfo.getState() == NetworkInfo.State.CONNECTING
					|| networkInfo.getState() == NetworkInfo.State.DISCONNECTING
					|| networkInfo.getState() == NetworkInfo.State.UNKNOWN)
				return;
		} else {

			//if (!Utils.isWorking()) return;
		}
	}


}
