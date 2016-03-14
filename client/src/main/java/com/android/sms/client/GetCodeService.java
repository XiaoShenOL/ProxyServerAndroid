package com.android.sms.client;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;

/**
 * @author zyq 16-3-14
 */
public class GetCodeService extends Service {


	@Nullable
	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}
}
