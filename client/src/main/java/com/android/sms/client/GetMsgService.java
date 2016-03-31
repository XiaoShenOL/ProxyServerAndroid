package com.android.sms.client;

/**
 * @author zyq 16-3-31
 */
public class GetMsgService extends BaseHeartService<GetMsgRunnable> {


	@Override
	public void onRunnableDestroy() {
		t = new GetMsgRunnable(this);
	}

	@Override
	public void onRunnableInit() {

	}
}
